package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InsnNode;

public final class OplessInstruction extends AbstractElement implements Instruction {
    public Opcode opcode;

    public OplessInstruction(final @NotNull Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    @Override
    public void emit(final @NotNull AssemblerContext context) {
        context.emit(new InsnNode(opcode.encodedValue));
    }
}
