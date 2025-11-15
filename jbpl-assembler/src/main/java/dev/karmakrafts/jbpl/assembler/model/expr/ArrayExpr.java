/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import dev.karmakrafts.jbpl.assembler.model.type.TypeMapper;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArrayExpr extends AbstractExprContainer implements ConstExpr {
    public static final int TYPE_INDEX = 0;
    public static final int VALUES_INDEX = 1;

    private Object arrayReference; // Cache array ref to allow mutability

    public ArrayExpr() {
        addExpression(ConstExpr.unit()); // Placeholder for inferred type
    }

    public ArrayExpr(final @NotNull Expr type) {
        addExpression(type);
    }

    public static @NotNull ArrayExpr fromArrayRef(final @NotNull Object arrayRef,
                                                  final @NotNull TokenRange tokenRange) {
        final var arrayType = arrayRef.getClass();
        final var componentType = arrayType.getComponentType();
        final var elementType = TypeMapper.map(componentType, true);
        final var expr = new ArrayExpr(ConstExpr.of(elementType));
        final var length = Array.getLength(arrayRef);
        for (var i = 0; i < length; i++) {
            expr.addValue(ConstExpr.of(Array.get(arrayRef, i)));
        }
        expr.arrayReference = arrayRef; // Directly set array reference, avoid further evaluation
        expr.setTokenRange(tokenRange);
        return expr;
    }

    public static @NotNull ArrayExpr fromArrayRef(final @NotNull Object arrayRef) {
        return fromArrayRef(arrayRef, TokenRange.SYNTHETIC);
    }

    public boolean hasInferredType() {
        return getExpressions().get(TYPE_INDEX).isUnit();
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        getExpressions().set(TYPE_INDEX, type);
    }

    public void clearValues() {
        final var type = getType();
        clearExpressions();
        addExpression(type);
    }

    public void addValue(final @NotNull Expr value) {
        addExpression(value);
    }

    public void addValues(final @NotNull Collection<Expr> values) {
        addExpressions(values);
    }

    public void setValue(final int index, final @NotNull Expr value) {
        getExpressions().set(VALUES_INDEX + index, value);
    }

    public @NotNull Expr getValue(final int index) {
        return getExpressions().get(VALUES_INDEX + index);
    }

    public @NotNull List<Expr> getValues() {
        final var expressions = getExpressions();
        return expressions.subList(VALUES_INDEX, expressions.size());
    }

    @Override
    public void ensureLazyConstValue(final @NotNull EvaluationContext context) throws EvaluationException {
        if (arrayReference != null) { // Lazily evaluate array to runtime object and cache it
            return;
        }
        final var type = TypeMapper.map(getType(context), true);
        final var values = getValues();
        final var size = values.size();
        arrayReference = Array.newInstance(type.componentType(), size);
        for (var i = 0; i < size; i++) {
            Array.set(arrayReference, i, values.get(i).evaluateAs(context, Object.class));
        }
    }

    @Override
    public @NotNull Object getConstValue() {
        return Objects.requireNonNull(arrayReference);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType();
        if (type.isUnit()) { // We need to infer the array type from the element types
            // @formatter:off
            final var elementTypes = getValues().stream()
                .map(ExceptionUtils.unsafeFunction(expr -> expr.getType(context).resolveIfNeeded(context)))
                .toList();
            // @formatter:on
            return TypeCommonizer.getCommonType(elementTypes, context).orElseThrow(() -> {
                final var values = getValues().stream().map(Expr::toString).collect(Collectors.joining(", "));
                final var message = String.format("Could not find common type for array values %s", values);
                return new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }).array();
        }
        return type.evaluateAs(context, Type.class).resolveIfNeeded(context).array();
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        ensureLazyConstValue(context);
        context.pushValue(this); // Arrays are ConstValue's under the hood
    }

    @Override
    public @NotNull ArrayExpr copy() {
        final var result = copyParentAndSourceTo(new ArrayExpr(getType().copy()));
        result.addValues(getValues().stream().map(Expr::copy).toList());
        return result;
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("[%s]{%s}", getType(), getValues().stream()
            .map(Expr::toString)
            .collect(Collectors.joining(", ")));
    } // @formatter:on
}
