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
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class AsExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;
    public static final int TYPE_INDEX = 1;

    public AsExpr(final @NotNull Expr value, final @NotNull Expr type) {
        addExpression(value);
        addExpression(type);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        getExpressions().set(TYPE_INDEX, type);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getType().evaluateAsConst(context, Type.class);
    }

    private @NotNull LiteralExpr castFromNumber(final @NotNull Type type,
                                                final @NotNull Number number) throws EvaluationException {
        // If we have a number, we may cast into other numbers and chars/bools with special rules
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> LiteralExpr.of(number.byteValue(), getTokenRange());
                case I16 -> LiteralExpr.of(number.shortValue(), getTokenRange());
                case I32 -> LiteralExpr.of(number.intValue(), getTokenRange());
                case I64 -> LiteralExpr.of(number.longValue(), getTokenRange());
                case F32 -> LiteralExpr.of(number.floatValue(), getTokenRange());
                case F64 -> LiteralExpr.of(number.doubleValue(), getTokenRange());
                case CHAR -> LiteralExpr.of((char) number.intValue(), getTokenRange());
                case BOOL -> LiteralExpr.of(number.longValue() != 0, getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast numeric type into %s", type), this);
            };
        }
        throw new EvaluationException(String.format("Cannot cast numeric type into %s", type), this);
    }

    private @NotNull LiteralExpr castFromBoolean(final @NotNull Type type,
                                                 final @NotNull Boolean bool) throws EvaluationException {
        // Booleans can be cast into any integer and floating point type
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> LiteralExpr.of((byte) (bool ? 1 : 0), getTokenRange());
                case I16 -> LiteralExpr.of((short) (bool ? 1 : 0), getTokenRange());
                case I32 -> LiteralExpr.of(bool ? 1 : 0, getTokenRange());
                case I64 -> LiteralExpr.of(bool ? 1L : 0L, getTokenRange());
                case F32 -> LiteralExpr.of(bool ? 1F : 0F, getTokenRange());
                case F64 -> LiteralExpr.of(bool ? 1.0 : 0.0, getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast boolean into %s", type), this);
            };
        }
        throw new EvaluationException(String.format("Cannot cast boolean into %s", type), this);
    }

    private @NotNull LiteralExpr castFromString(final @NotNull Type type,
                                                final @NotNull String string) throws EvaluationException {
        // Strings can be cast into any builtin type for parsing numerics and converting into chars/bools
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> LiteralExpr.of(Byte.parseByte(string), getTokenRange());
                case I16 -> LiteralExpr.of(Short.parseShort(string), getTokenRange());
                case I32 -> LiteralExpr.of(Integer.parseInt(string), getTokenRange());
                case I64 -> LiteralExpr.of(Long.parseLong(string), getTokenRange());
                case F32 -> LiteralExpr.of(Float.parseFloat(string), getTokenRange());
                case F64 -> LiteralExpr.of(Double.parseDouble(string), getTokenRange());
                case BOOL -> LiteralExpr.of(Boolean.parseBoolean(string), getTokenRange());
                case CHAR -> LiteralExpr.of(string.charAt(0), getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast string into %s", type), this);
            };
        }
        throw new EvaluationException(String.format("Cannot cast string into %s", type), this);
    }

    private @NotNull LiteralExpr castFromChar(final @NotNull Type type,
                                              final @NotNull Character value) throws EvaluationException {
        // Strings can be cast into any builtin type for parsing numerics and converting into chars/bools
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> LiteralExpr.of(Byte.parseByte(value.toString()), getTokenRange());
                case I16 -> LiteralExpr.of(Short.parseShort(value.toString()), getTokenRange());
                case I32 -> LiteralExpr.of(Integer.parseInt(value.toString()), getTokenRange());
                case I64 -> LiteralExpr.of(Long.parseLong(value.toString()), getTokenRange());
                case F32 -> LiteralExpr.of(Float.parseFloat(value.toString()), getTokenRange());
                case F64 -> LiteralExpr.of(Double.parseDouble(value.toString()), getTokenRange());
                case BOOL -> LiteralExpr.of(Boolean.parseBoolean(value.toString()), getTokenRange());
                case CHAR -> LiteralExpr.of(value.toString(), getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast string into %s", type), this);
            };
        }
        throw new EvaluationException(String.format("Cannot cast string into %s", type), this);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType(context);
        // Always return unit literal for void type so we can have proper generic evaluation
        if (type == BuiltinType.VOID) {
            context.pushValue(LiteralExpr.unit(getTokenRange()));
            return;
        }
        final var value = getValue();
        final var valueType = value.getType(context);
        // Shortcut if the type is already the same, just evaluate the value directly
        if (type.equals(valueType)) {
            context.pushValue(value.evaluateAsConst(context));
            return;
        }
        final var constValue = value.evaluateAsConst(context, Object.class);
        // Anything may be cast to a string to allow string conversions
        if (type == BuiltinType.STRING) {
            context.pushValue(LiteralExpr.of(constValue.toString(), getTokenRange()));
            return;
        }
        // We decide which conversions are possible based on the incoming type
        if (constValue instanceof Number number) {
            context.pushValue(castFromNumber(type, number));
            return;
        }
        else if (constValue instanceof Boolean bool) {
            context.pushValue(castFromBoolean(type, bool));
            return;
        }
        else if (constValue instanceof String string) {
            context.pushValue(castFromString(type, string));
            return;
        }
        else if (constValue instanceof Character character) {
            context.pushValue(castFromChar(type, character));
            return;
        }
        throw new EvaluationException(String.format("Cannot cast %s into %s", valueType, type), this);
    }

    @Override
    public @NotNull AsExpr copy() {
        return copyParentAndSourceTo(new AsExpr(getValue().copy(), getType().copy()));
    }
}
