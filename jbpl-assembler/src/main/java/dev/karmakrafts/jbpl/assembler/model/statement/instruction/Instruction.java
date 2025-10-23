package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import org.jetbrains.annotations.NotNull;

public interface Instruction extends Statement {
    @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) throws EvaluationException;
}
