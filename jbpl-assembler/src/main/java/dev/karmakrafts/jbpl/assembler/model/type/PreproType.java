package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.decl.InjectorDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.model.instruction.OplessInstruction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum PreproType implements Type {
    // @formatter:off
    TYPE                (Type.class),
    FIELD_SIGNATURE     (FieldSignatureExpr.class),
    FUNCTION_SIGNATURE  (FunctionSignatureExpr.class),
    SELECTOR            (SelectorDecl.class),
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

    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.PREPROCESSOR;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        return switch (this) {
            case TYPE -> LiteralExpr.of(BuiltinType.VOID);
            case OPCODE -> LiteralExpr.of(Opcode.NOP);
            case INSTRUCTION -> LiteralExpr.of(new OplessInstruction(Opcode.NOP));
            default -> throw new IllegalStateException(String.format("Type %s does not have a default value", this));
        };
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("Preprocessor types cannot be materialized");
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