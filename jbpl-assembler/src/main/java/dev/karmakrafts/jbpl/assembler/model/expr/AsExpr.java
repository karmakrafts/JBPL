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
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
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
        return getType().evaluateAs(context, Type.class);
    }

    private @NotNull ConstExpr castFromNumber(final @NotNull Type type,
                                              final @NotNull Number number,
                                              final @NotNull EvaluationContext context) throws EvaluationException {
        // If we have a number, we may cast into other numbers and chars/bools with special rules
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> ConstExpr.of(number.byteValue(), getTokenRange());
                case I16 -> ConstExpr.of(number.shortValue(), getTokenRange());
                case I32 -> ConstExpr.of(number.intValue(), getTokenRange());
                case I64 -> ConstExpr.of(number.longValue(), getTokenRange());
                case F32 -> ConstExpr.of(number.floatValue(), getTokenRange());
                case F64 -> ConstExpr.of(number.doubleValue(), getTokenRange());
                case CHAR -> ConstExpr.of((char) number.intValue(), getTokenRange());
                case BOOL -> ConstExpr.of(number.longValue() != 0, getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast numeric type into %s", type),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
            };
        }
        throw new EvaluationException(String.format("Cannot cast numeric type into %s", type),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    private @NotNull ConstExpr castFromBoolean(final @NotNull Type type,
                                               final @NotNull Boolean bool,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        // Booleans can be cast into any integer and floating point type
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> ConstExpr.of((byte) (bool ? 1 : 0), getTokenRange());
                case I16 -> ConstExpr.of((short) (bool ? 1 : 0), getTokenRange());
                case I32 -> ConstExpr.of(bool ? 1 : 0, getTokenRange());
                case I64 -> ConstExpr.of(bool ? 1L : 0L, getTokenRange());
                case F32 -> ConstExpr.of(bool ? 1F : 0F, getTokenRange());
                case F64 -> ConstExpr.of(bool ? 1.0 : 0.0, getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast boolean into %s", type),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
            };
        }
        throw new EvaluationException(String.format("Cannot cast boolean into %s", type),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    private @NotNull ConstExpr castFromString(final @NotNull Type type,
                                              final @NotNull String string,
                                              final @NotNull EvaluationContext context) throws EvaluationException {
        // Strings can be cast into any builtin type for parsing numerics and converting into chars/bools
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> ConstExpr.of(Byte.parseByte(string), getTokenRange());
                case I16 -> ConstExpr.of(Short.parseShort(string), getTokenRange());
                case I32 -> ConstExpr.of(Integer.parseInt(string), getTokenRange());
                case I64 -> ConstExpr.of(Long.parseLong(string), getTokenRange());
                case F32 -> ConstExpr.of(Float.parseFloat(string), getTokenRange());
                case F64 -> ConstExpr.of(Double.parseDouble(string), getTokenRange());
                case BOOL -> ConstExpr.of(Boolean.parseBoolean(string), getTokenRange());
                case CHAR -> ConstExpr.of(string.charAt(0), getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast string into %s", type),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
            };
        }
        else if (type instanceof PreproType preproType) {
            return switch (preproType) {
                case TYPE -> ConstExpr.of(Type.tryParse(string).orElseThrow(() -> new EvaluationException(
                    "Could not parse type from string",
                    SourceDiagnostic.from(this),
                    context.createStackTrace())), getTokenRange());
                case OPCODE -> ConstExpr.of(Opcode.findByName(string).orElseThrow(() -> new EvaluationException(
                    "Could not parse opcode from string",
                    SourceDiagnostic.from(this),
                    context.createStackTrace())), getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast string into %s", type),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
            };
        }
        throw new EvaluationException(String.format("Cannot cast string into %s", type),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    private @NotNull ConstExpr castFromChar(final @NotNull Type type,
                                            final @NotNull Character value,
                                            final @NotNull EvaluationContext context) throws EvaluationException {
        // Strings can be cast into any builtin type for parsing numerics and converting into chars/bools
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType) {
                case I8 -> ConstExpr.of(Byte.parseByte(value.toString()), getTokenRange());
                case I16 -> ConstExpr.of(Short.parseShort(value.toString()), getTokenRange());
                case I32 -> ConstExpr.of(Integer.parseInt(value.toString()), getTokenRange());
                case I64 -> ConstExpr.of(Long.parseLong(value.toString()), getTokenRange());
                case F32 -> ConstExpr.of(Float.parseFloat(value.toString()), getTokenRange());
                case F64 -> ConstExpr.of(Double.parseDouble(value.toString()), getTokenRange());
                case BOOL -> ConstExpr.of(Boolean.parseBoolean(value.toString()), getTokenRange());
                case CHAR -> ConstExpr.of(value.toString(), getTokenRange());
                default -> throw new EvaluationException(String.format("Cannot cast string into %s", type),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
            };
        }
        throw new EvaluationException(String.format("Cannot cast string into %s", type),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType(context);
        // Always return unit literal for void type so we can have proper generic evaluation
        if (type == BuiltinType.VOID) {
            context.pushValue(ConstExpr.unit(getTokenRange()));
            return;
        }
        final var value = getValue();
        final var valueType = value.getType(context);
        // Shortcut if the type is already the same, just evaluate the value directly
        if (type.equals(valueType)) {
            context.pushValue(value.evaluateAsConst(context));
            return;
        }
        final var constValue = value.evaluateAs(context, Object.class);
        // Anything may be cast to a string to allow string conversions
        if (type == BuiltinType.STRING) {
            context.pushValue(ConstExpr.of(constValue.toString(), getTokenRange()));
            return;
        }
        // We decide which conversions are possible based on the incoming type
        if (constValue instanceof Number number) {
            context.pushValue(castFromNumber(type, number, context));
            return;
        }
        else if (constValue instanceof Boolean bool) {
            context.pushValue(castFromBoolean(type, bool, context));
            return;
        }
        else if (constValue instanceof String string) {
            context.pushValue(castFromString(type, string, context));
            return;
        }
        else if (constValue instanceof Character character) {
            context.pushValue(castFromChar(type, character, context));
            return;
        }
        throw new EvaluationException(String.format("Cannot cast %s into %s", valueType, type),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    @Override
    public @NotNull AsExpr copy() {
        return copyParentAndSourceTo(new AsExpr(getValue().copy(), getType().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s as %s", getValue(), getType());
    }
}
