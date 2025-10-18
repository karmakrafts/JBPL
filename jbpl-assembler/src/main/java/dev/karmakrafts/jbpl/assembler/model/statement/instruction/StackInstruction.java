package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;

public final class StackInstruction extends AbstractExprContainer implements Instruction {
    public static final int SLOT_INDEX = 0;
    public Opcode opcode;

    /**
     * @param opcode The wanted opcode
     * @param slot   Either a constant index or the name of a previously defined local.
     */
    public StackInstruction(final @NotNull Opcode opcode, final @NotNull Expr slot) {
        this.opcode = opcode;
        addExpression(slot);
    }

    public @NotNull Expr getSlot() {
        return getExpressions().get(SLOT_INDEX);
    }

    public void setSlot(final @NotNull Expr slot) {
        getExpressions().set(SLOT_INDEX, slot);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    @Override
    public void emit(final @NotNull AssemblerContext context) {
        final var encodedOpcode = getOpcode(context).encodedValue;
        final var slotIdObject = getSlot().evaluateAsLiteral(context, Object.class);
        if (slotIdObject instanceof Integer slotId) {
            context.emit(new VarInsnNode(encodedOpcode, slotId));
        }
        // If we fall through the above case, we need to resolve the local using pre-defined locals in the sack frame
        // TODO: context.emit(new VarInsnNode(encodedOpcode, index));
    }
}
