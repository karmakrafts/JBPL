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
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.XBiFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ArrayExpr extends AbstractExprContainer implements ConstExpr {
    public static final int TYPE_INDEX = 0;
    public static final int VALUES_INDEX = 1;

    public final boolean hasInferredType;
    private final XBiFunction<EvaluationContext, Optional<? extends Type>, Type, EvaluationException> elementTypeResolver;
    private boolean hasUnresolvedType = false; // Whether the type is an Expr or not
    private int valueIndex = 0;
    private Object arrayReference; // Cache array ref to allow mutability

    private ArrayExpr(final @NotNull XBiFunction<EvaluationContext, Optional<? extends Type>, Type, EvaluationException> elementTypeResolver,
                      final boolean hasInferredType) {
        this.elementTypeResolver = elementTypeResolver;
        this.hasInferredType = hasInferredType;
    }

    public ArrayExpr() {
        this((ctx, commonType) -> commonType.orElseThrow(), true);
    }

    public ArrayExpr(final @NotNull Expr type) {
        this((ctx, commonType) -> type.evaluateAs(ctx, Type.class), false);
        hasUnresolvedType = true;
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

    /**
     * The offset into the expression list until array values start
     */
    public int getValueOffset() {
        return hasUnresolvedType ? VALUES_INDEX : 0;
    }

    public boolean hasUnresolvedType() {
        return hasUnresolvedType;
    }

    public @NotNull Expr getType() {
        assert hasUnresolvedType;
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        assert hasUnresolvedType;
        getExpressions().set(TYPE_INDEX, type);
    }

    public void clearValues() {
        final var type = hasUnresolvedType ? getType() : null;
        clearExpressions();
        if (type != null) {
            addExpression(type);
        }
        valueIndex = 0;
    }

    public void addValue(final @NotNull Expr value) {
        if (hasUnresolvedType) {
            getExpressions().add(VALUES_INDEX + valueIndex++, value);
            return;
        }
        getExpressions().add(valueIndex++, value);
    }

    public void addValues(final @NotNull Collection<Expr> values) {
        if (hasUnresolvedType) {
            getExpressions().addAll(VALUES_INDEX + valueIndex, values);
            return;
        }
        getExpressions().addAll(valueIndex++, values);
        valueIndex += values.size();
    }

    public @NotNull Expr getValue(final int index) {
        if (hasUnresolvedType) {
            return getExpressions().get(VALUES_INDEX + index);
        }
        return getExpressions().get(index);
    }

    public @NotNull List<Expr> getValues() {
        final var expressions = getExpressions();
        if (hasUnresolvedType) {
            return expressions.subList(VALUES_INDEX, expressions.size());
        }
        return expressions;
    }

    @Override
    public void ensureLazyConstValue(final @NotNull EvaluationContext context) throws EvaluationException {
        if (arrayReference != null) { // Lazily evaluate array to runtime object and cache it
            return;
        }
        final var type = TypeMapper.map(getType(context));
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
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException { // @formatter:off
        return elementTypeResolver.apply(context, TypeCommonizer.getCommonType(getExpressions().stream()
            .map(ExceptionUtils.unsafeFunction(expr -> expr.getType(context)))
            .toList())).array();
    } // @formatter:on

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        ensureLazyConstValue(context);
        context.pushValue(this); // Arrays are ConstValue's under the hood
    }

    @Override
    public @NotNull ArrayExpr copy() {
        final var result = copyParentAndSourceTo(new ArrayExpr(elementTypeResolver, hasInferredType));
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
