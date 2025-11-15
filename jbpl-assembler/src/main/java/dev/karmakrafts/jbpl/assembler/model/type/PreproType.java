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

package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.decl.InjectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.model.instruction.OplessInstruction;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum PreproType implements Type {
    // @formatter:off
    TYPE                (Type.class),
    FIELD_SIGNATURE     (FieldSignatureExpr.class),
    FUNCTION_SIGNATURE  (FunctionSignatureExpr.class),
    OPCODE              (Opcode.class),
    INSTRUCTION         (Instruction.class),
    INJECTOR            (InjectorDecl.class);
    // @formatter:on

    public final Class<?> type;

    PreproType(final @NotNull Class<?> type) {
        this.type = type;
    }

    public static @NotNull Optional<PreproType> findByType(final @NotNull Class<?> type) {
        return Arrays.stream(values()).filter(t -> t.type.isAssignableFrom(type)).findFirst();
    }

    public static @NotNull Optional<PreproType> findByName(final @NotNull String name) {
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) {
        return TypeCategory.PREPROCESSOR;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        return switch (this) {
            case TYPE -> ConstExpr.of(BuiltinType.VOID);
            case OPCODE -> ConstExpr.of(Opcode.NOP);
            case INSTRUCTION -> ConstExpr.of(new OplessInstruction(Opcode.NOP));
            default -> throw new IllegalStateException(String.format("Type %s does not have a default value", this));
        };
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("Preprocessor types cannot be materialized");
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        return this;
    }

    @Override
    public boolean canCastTo(final @NotNull Type other, final @NotNull EvaluationContext context) {
        return switch (this) {
            case TYPE, OPCODE -> other == BuiltinType.STRING;
            default -> false;
        };
    }

    @Override
    public @NotNull Expr cast(final @NotNull Expr value,
                              final @NotNull EvaluationContext context) throws EvaluationException {
        if (value.getType(context) == this) {
            return value;
        }
        final var constValue = value.evaluateAs(context, Object.class);
        if (constValue instanceof String stringValue) {
            return switch (this) {
                case TYPE -> ConstExpr.of(Type.tryParse(stringValue).orElseThrow(() -> {
                    final var message = String.format("Cannot cast string '%s' to literal type", stringValue);
                    return new EvaluationException(message,
                        SourceDiagnostic.from(value, message),
                        context.createStackTrace());
                }), value.getTokenRange());
                case OPCODE -> ConstExpr.of(Opcode.findByName(stringValue).orElseThrow(() -> {
                    final var message = String.format("Cannot cast string '%s' to literal opcode", stringValue);
                    return new EvaluationException(message,
                        SourceDiagnostic.from(value, message),
                        context.createStackTrace());
                }), value.getTokenRange());
                default -> {
                    final var message = String.format("Cannot cast string '%s' to type %s", stringValue, this);
                    throw new EvaluationException(message,
                        SourceDiagnostic.from(value, message),
                        context.createStackTrace());
                }
            };
        }
        final var message = String.format("Cannot cast type %s to type %s", value.getType(context), this);
        throw new EvaluationException(message, SourceDiagnostic.from(value, message), context.createStackTrace());
    }

    @Override
    public String toString() {
        return switch (this) {
            case FIELD_SIGNATURE -> "signature(field)";
            case FUNCTION_SIGNATURE -> "signature(fun)";
            default -> name().toLowerCase(Locale.ROOT);
        };
    }
}