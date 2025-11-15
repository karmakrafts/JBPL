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

package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.ArrayType;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.RangeType;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.function.Function;

public final class ForStatement extends AbstractElementContainer implements Statement, ScopeOwner {
    private Expr variableName;
    private Expr value;

    public ForStatement(final @NotNull Expr variableName, final @NotNull Expr value) {
        setVariableName(variableName);
        setValue(value);
    }

    public @NotNull Expr getVariableName() {
        return variableName;
    }

    public void setVariableName(final @NotNull Expr variableName) {
        variableName.setParent(this);
        this.variableName = variableName;
    }

    public @NotNull Expr getValue() {
        return value;
    }

    public void setValue(final @NotNull Expr value) {
        value.setParent(this);
        this.value = value;
    }

    private byte performIteration(final @NotNull Expr variableValue,
                                  final @NotNull EvaluationContext context) throws EvaluationException {
        final var variableName = this.variableName.evaluateAs(context, String.class);
        context.pushFrame(this);
        final var frame = context.peekFrame();
        frame.resetLocalDefines();
        frame.namedLocalValues.put(variableName, variableValue);
        for (final var element : getElements()) {
            if (!element.isEvaluatedDirectly()) {
                continue;
            }
            element.evaluate(context);
            final var returnMask = context.getReturnMask();
            if (context.clearCnt() || context.clearBrk() || context.hasRet()) {
                context.popFrame();
                return returnMask;
            }
        }
        context.popFrame();
        return EvaluationContext.RET_MASK_NONE;
    }

    private void iterateArray(final @NotNull EvaluationContext context) throws EvaluationException {
        final var array = value.evaluateAs(context, Object.class);
        final var arrayLength = Array.getLength(array);
        for (var i = 0; i < arrayLength; i++) {
            final var value = Array.get(array, i);
            final var result = performIteration(ConstExpr.of(value, getTokenRange()), context);
            if ((result & EvaluationContext.RET_MASK_CONTINUE) != 0) {
                continue;
            }
            if ((result & EvaluationContext.RET_MASK_BREAK) != 0 || (result & EvaluationContext.RET_MASK_RETURN) != 0) {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> void iterateNumericRange(final @NotNull Class<T> type,
                                                               final @NotNull Function<T, T> inc,
                                                               final @NotNull EvaluationContext context) throws EvaluationException {
        final var values = getValue().evaluateAs(context, (Class<T[]>) type.arrayType());
        final var start = values[0];
        final var end = values[1];
        for (var value = start; value.compareTo(end) < 0; value = inc.apply(value)) {
            final var result = performIteration(ConstExpr.of(value, getTokenRange()), context);
            if ((result & EvaluationContext.RET_MASK_CONTINUE) != 0) {
                continue;
            }
            if ((result & EvaluationContext.RET_MASK_BREAK) != 0 || (result & EvaluationContext.RET_MASK_RETURN) != 0) {
                break;
            }
        }
    }

    private void iterateRange(final @NotNull EvaluationContext context,
                              final @NotNull RangeType rangeType) throws EvaluationException {
        final var valueType = rangeType.type();
        if (valueType instanceof BuiltinType builtinType) {
            switch (builtinType) {
                case I8 -> {
                    iterateNumericRange(Byte.class, x -> (byte) (x + 1), context);
                    return;
                }
                case I16 -> {
                    iterateNumericRange(Short.class, x -> (short) (x + 1), context);
                    return;
                }
                case I32 -> {
                    iterateNumericRange(Integer.class, x -> x + 1, context);
                    return;
                }
                case I64 -> {
                    iterateNumericRange(Long.class, x -> x + 1, context);
                    return;
                }
                case F32 -> {
                    iterateNumericRange(Float.class, x -> x + 1F, context);
                    return;
                }
                case F64 -> {
                    iterateNumericRange(Double.class, x -> x + 1D, context);
                    return;
                }
                case CHAR -> {
                    iterateNumericRange(Character.class, x -> (char) ((int) x + 1), context);
                    return;
                }
            }
        }
        throw new EvaluationException(String.format("Cannot iterate over range of type %s", valueType),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var valueType = value.getType(context);
        if (valueType instanceof ArrayType) {
            iterateArray(context);
            return;
        }
        else if (valueType instanceof RangeType rangeType) {
            iterateRange(context, rangeType);
            return;
        }
        throw new EvaluationException(String.format("Cannot use value of type %s in right hand side of for loop",
            valueType), SourceDiagnostic.from(this), context.createStackTrace());
    }

    @Override
    public @NotNull ForStatement copy() {
        final var forStatement = copyParentAndSourceTo(new ForStatement(getVariableName().copy(), getValue().copy()));
        forStatement.addElements(getElements().stream().map(Element::copy).toList());
        return forStatement;
    }
}
