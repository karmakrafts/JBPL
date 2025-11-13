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
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.RangeType;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.function.Function;

public final class RangeExpr extends AbstractExprContainer implements Expr {
    public static final int START_INDEX = 0;
    public static final int END_INDEX = 1;

    public final boolean isInclusive;

    public RangeExpr(final @NotNull Expr start, final @NotNull Expr end, final boolean isInclusive) {
        addExpression(start);
        addExpression(end);
        this.isInclusive = isInclusive;
    }

    public @NotNull Expr getStart() {
        return getExpressions().get(START_INDEX);
    }

    public void setStart(final @NotNull Expr start) {
        start.setParent(this);
        getExpressions().set(START_INDEX, start);
    }

    public @NotNull Expr getEnd() {
        return getExpressions().get(END_INDEX);
    }

    public void setEnd(final @NotNull Expr end) {
        end.setParent(this);
        getExpressions().set(END_INDEX, end);
    }

    @Override
    public @NotNull RangeType getType(@NotNull EvaluationContext context) throws EvaluationException {
        final var startType = getStart().getType(context);
        final var endType = getEnd().getType(context);
        final var commonType = TypeCommonizer.getCommonType(startType,
            endType).orElseThrow(() -> new EvaluationException("Cannot find common type for range",
            SourceDiagnostic.from(this, "Cannot find common type for range"),
            null));
        return new RangeType(commonType);
    }

    // Numeric ranges are evaluated to const arrays (pairs) of start and adjusted end
    private <T> void evaluateNumericRange(final @NotNull Class<T> type,
                                          final @NotNull Function<T, T> inc,
                                          final @NotNull EvaluationContext context) throws EvaluationException {
        var start = getStart().evaluateAs(context, type);
        var end = getEnd().evaluateAs(context, type);
        if (isInclusive) {
            end = inc.apply(end);
        }
        final var array = Array.newInstance(type, 2);
        Array.set(array, 0, start);
        Array.set(array, 1, end);
        context.pushValue(ConstExpr.of(array, getTokenRange()));
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType(context).type();
        if (type instanceof BuiltinType builtinType) {
            switch (builtinType) {
                case I8 -> {
                    evaluateNumericRange(Byte.class, x -> (byte) (x + 1), context);
                    return;
                }
                case I16 -> {
                    evaluateNumericRange(Short.class, x -> (short) (x + 1), context);
                    return;
                }
                case I32 -> {
                    evaluateNumericRange(Integer.class, x -> x + 1, context);
                    return;
                }
                case I64 -> {
                    evaluateNumericRange(Long.class, x -> x + 1, context);
                    return;
                }
                case F32 -> {
                    evaluateNumericRange(Float.class, x -> x + 1F, context);
                    return;
                }
                case F64 -> {
                    evaluateNumericRange(Double.class, x -> x + 1D, context);
                    return;
                }
                case CHAR -> {
                    evaluateNumericRange(Character.class, x -> (char) ((int) x + 1), context);
                    return;
                }
            }
        }
        final var message = String.format("Unsupported type %s for range", type);
        throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
    }

    @Override
    public @NotNull RangeExpr copy() {
        return copyParentAndSourceTo(new RangeExpr(getStart().copy(), getEnd().copy(), isInclusive));
    }

    @Override
    public String toString() {
        if (isInclusive) {
            return String.format("%s..%s", getStart(), getEnd());
        }
        return String.format("%s..<%s", getStart(), getEnd());
    }
}
