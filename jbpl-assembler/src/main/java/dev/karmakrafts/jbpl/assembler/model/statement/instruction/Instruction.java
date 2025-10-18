package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import org.jetbrains.annotations.NotNull;

public interface Instruction extends Statement {
    @NotNull Opcode getOpcode(final @NotNull AssemblerContext context);

    void emit(final @NotNull AssemblerContext context);

    @Override
    default @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        emit(context);
        return new UnitExpr();
    }
}
