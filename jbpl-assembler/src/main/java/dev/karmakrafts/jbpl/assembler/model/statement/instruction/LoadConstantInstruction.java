package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public final class LoadConstantInstruction extends AbstractExprContainer implements Instruction {
    public static final int VALUE_INDEX = 0;
    public Opcode opcode;

    public LoadConstantInstruction(final @NotNull Opcode opcode, final @NotNull Expr value) {
        this.opcode = opcode;
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    private @NotNull AbstractInsnNode createConstantInt(final int value) {
        if (value == -1) {
            return new InsnNode(Opcodes.ICONST_M1);
        }
        else if (value >= 0 && value <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + value);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantLong(final long value) {
        if (value >= 0 && value <= 1) {
            return new InsnNode(Opcodes.LCONST_0 + (int) value);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantFloat(final float value) {
        if (value == 0F) {
            return new InsnNode(Opcodes.FCONST_0);
        }
        else if (value == 1F) {
            return new InsnNode(Opcodes.FCONST_1);
        }
        else if (value == 2) {
            return new InsnNode(Opcodes.FCONST_2);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantDouble(final double value) {
        if (value == 0.0) {
            return new InsnNode(Opcodes.DCONST_0);
        }
        else if (value == 1.0) {
            return new InsnNode(Opcodes.DCONST_1);
        }
        return new LdcInsnNode(value);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var value = getValue().evaluateAsConst(context, Number.class);
        if (value instanceof Integer intValue) {
            context.emit(createConstantInt(intValue));
            return;
        }
        else if (value instanceof Long longValue) {
            context.emit(createConstantLong(longValue));
            return;
        }
        else if (value instanceof Float floatValue) {
            context.emit(createConstantFloat(floatValue));
            return;
        }
        else if (value instanceof Double doubleValue) {
            context.emit(createConstantDouble(doubleValue));
            return;
        }
        switch (opcode) {
            case BIPUSH -> context.emit(new IntInsnNode(Opcodes.BIPUSH, value.byteValue()));
            case SIPUSH -> context.emit(new IntInsnNode(Opcodes.SIPUSH, value.shortValue()));
            default -> context.emit(new LdcInsnNode(value));
        }
    }
}
