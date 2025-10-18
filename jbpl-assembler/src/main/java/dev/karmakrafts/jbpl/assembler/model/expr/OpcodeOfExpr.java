package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class OpcodeOfExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;

    public OpcodeOfExpr(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.OPCODE;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        final var opcode = getValue().evaluateAsLiteral(context, Instruction.class).getOpcode(context);
        return LiteralExpr.of(opcode);
    }
}
