package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class VersionStatement extends AbstractExprContainer implements Statement {
    public static final int VERSION_INDEX = 0;

    public VersionStatement(final @NotNull Expr version) {
        addExpression(version);
    }

    public @NotNull Expr getVersion() {
        return getExpressions().get(VERSION_INDEX);
    }

    public void setVersion(final @NotNull Expr version) {
        getExpressions().set(VERSION_INDEX, version);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.bytecodeVersion = getVersion().evaluateAsConst(context, Integer.class);
    }

    @Override
    public @NotNull VersionStatement copy() {
        return copyParentAndSourceTo(new VersionStatement(getVersion().copy()));
    }
}
