package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import org.jetbrains.annotations.NotNull;

public interface SignatureExpr extends Expr {
    @NotNull String evaluateAsConstDescriptor(final @NotNull AssemblerContext context) throws EvaluationException;
}
