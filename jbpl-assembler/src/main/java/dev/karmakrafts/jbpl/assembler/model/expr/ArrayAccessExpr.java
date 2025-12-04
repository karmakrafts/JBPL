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
import dev.karmakrafts.jbpl.assembler.model.type.ArrayType;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.RangeType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class ArrayAccessExpr extends AbstractExprContainer implements Expr, Reference {
    public static final int REFERENCE_INDEX = 0;
    public static final int INDEX_INDEX = 1;

    public ArrayAccessExpr(final @NotNull Expr reference, final @NotNull Expr index) {
        addExpression(reference);
        addExpression(index);
    }

    public @NotNull Expr getReference() {
        return getExpressions().get(REFERENCE_INDEX);
    }

    public void setReference(final @NotNull Expr reference) {
        getExpressions().set(REFERENCE_INDEX, reference);
    }

    public @NotNull Expr getIndex() {
        return getExpressions().get(INDEX_INDEX);
    }

    public void setIndex(final @NotNull Expr index) {
        getExpressions().set(INDEX_INDEX, index);
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    @Override
    public @NotNull ConstExpr loadFromReference(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getReference().evaluateAs(context,
            Object.class); // This can either be an array ref or a string
        final var index = getIndex().evaluateAs(context, Object.class);
        final var isSlice = getIndex().getType(context).resolveIfNeeded(context) instanceof RangeType;
        if (value instanceof String stringValue) {
            if (isSlice) {
                final var range = (Integer[]) index;
                return ConstExpr.of(stringValue.substring(range[0], range[1]), getTokenRange());
            }
            // Get the character at the given index for strings
            return ConstExpr.of(stringValue.charAt((int) index), getTokenRange());
        }
        if (isSlice) {
            final var range = (Integer[]) index;
            final var componentType = value.getClass().getComponentType();
            final var length = Array.getLength(value);
            final var sliceLength = range[1] - range[0];
            if (sliceLength > length) {
                final var message = "Array range out of bounds";
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            final var slice = Array.newInstance(componentType, sliceLength);
            System.arraycopy(value, range[0], slice, 0, sliceLength);
            return ConstExpr.of(slice, getTokenRange());
        }
        final var length = Array.getLength(value);
        if ((int) index >= length) {
            final var message = String.format("Array index %d out of bounds for array of length %d",
                (int) index,
                length);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        return ConstExpr.of(Array.get(value, (int) index), getTokenRange());
    }

    @Override
    public void storeToReference(final @NotNull ConstExpr value,
                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var refExpr = getReference().evaluateAsConst(context);
        final int index = getIndex().evaluateAs(context, Integer.class);
        if (refExpr instanceof LiteralExpr literalExpr) {
            // For strings, we update the value in-place
            final var chars = literalExpr.value.toString().toCharArray();
            chars[index] = value.evaluateAs(context, Character.class);
            literalExpr.value = new String(chars);
            return;
        }
        final var ref = refExpr.getConstValue();
        final var length = Array.getLength(ref);
        if (index >= length) {
            final var message = String.format("Array index %d out of bounds for array of length %d", index, length);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        Array.set(ref, index, value.getConstValue());
        if (refExpr instanceof ArrayExpr arrayExpr) {
            arrayExpr.setValue(index, value); // Update value expression in actual array tree element
        }
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var isSlice = getIndex().getType(context).resolveIfNeeded(context) instanceof RangeType;
        final var type = getReference().getType(context).resolveIfNeeded(context);
        if (isSlice) {
            return type;
        }
        if (type == BuiltinType.STRING) {
            return BuiltinType.CHAR;
        }
        if (!(type instanceof ArrayType arrayType)) {
            throw new EvaluationException("Array access requires array reference type",
                SourceDiagnostic.from(this),
                context.createStackTrace());
        }
        return arrayType.elementType().resolveIfNeeded(context); // We unwrap one layer of array so return element type
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(loadFromReference(context));
    }

    @Override
    public @NotNull ArrayAccessExpr copy() {
        return copyParentAndSourceTo(new ArrayAccessExpr(getReference().copy(), getIndex().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s[%s]", getReference(), getIndex());
    }
}
