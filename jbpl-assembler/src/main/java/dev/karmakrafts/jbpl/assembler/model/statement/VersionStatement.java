package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class VersionStatement extends AbstractElement implements Statement {
    public Expr version; // TODO: handle this via AbstractExprContainer

    public VersionStatement(final @NotNull Expr version) {
        this.version = version;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.bytecodeVersion = version.evaluateAsConst(context, Integer.class);
    }

    @Override
    public @NotNull VersionStatement copy() {
        return copyParentAndSourceTo(new VersionStatement(version.copy()));
    }
}
