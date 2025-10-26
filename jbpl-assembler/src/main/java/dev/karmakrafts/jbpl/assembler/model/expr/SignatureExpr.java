package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import org.jetbrains.annotations.NotNull;

public interface SignatureExpr extends Expr {
    @Override
    @NotNull SignatureExpr copy();

    @NotNull String evaluateAsConstDescriptor(final @NotNull EvaluationContext context) throws EvaluationException;
}
