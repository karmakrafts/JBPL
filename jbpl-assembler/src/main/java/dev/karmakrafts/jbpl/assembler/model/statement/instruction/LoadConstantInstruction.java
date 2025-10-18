package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public final class LoadConstantInstruction extends AbstractExprContainer implements Instruction {
    public static final int VALUE_INDEX = 0;
    public Opcode opcode;

    public LoadConstantInstruction(final @NotNull Opcode opcode, final @NotNull Expr value) {
        this.opcode = opcode;
        addExpression(value);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    @Override
    public void emit(final @NotNull AssemblerContext context) {
        final var value = getValue().evaluateAsLiteral(context, Number.class);
        switch (opcode) {
            case BIPUSH -> context.emit(new IntInsnNode(Opcodes.BIPUSH, value.byteValue()));
            case SIPUSH -> context.emit(new IntInsnNode(Opcodes.SIPUSH, value.shortValue()));
            default -> context.emit(new LdcInsnNode(value));
        }
    }
}
