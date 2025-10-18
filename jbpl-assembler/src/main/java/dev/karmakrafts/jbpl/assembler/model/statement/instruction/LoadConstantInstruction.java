package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.LdcInsnNode;

public final class LoadConstantInstruction extends AbstractExprContainer implements Instruction {
    public LoadConstantInstruction(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(0);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        // We automatically optimize the constant load by evaluating the value once and peek at the actual type
        final var value = getValue().evaluateAsLiteral(context, Object.class);
        if (value instanceof Byte) {
            return Opcode.BIPUSH;
        }
        else if (value instanceof Short) {
            return Opcode.SIPUSH;
        }
        return Opcode.LDC;
    }

    @Override
    public void emit(final @NotNull AssemblerContext context) {
        final var value = getValue().evaluateAsLiteral(context, Object.class);
        context.emit(new LdcInsnNode(value));
    }
}
