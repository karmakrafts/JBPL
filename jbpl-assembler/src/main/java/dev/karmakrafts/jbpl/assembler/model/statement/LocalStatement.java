package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class LocalStatement extends AbstractElement implements Statement, NamedElement {
    public Expr name; // TODO: handle this via AbstractExprContainer

    public LocalStatement(final @NotNull Expr name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return name.evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        // Make this local available in the current frame during further evaluation
        final var name = this.name.evaluateAsConst(context, String.class);
        context.peekFrame().locals.put(name, this);
    }

    @Override
    public @NotNull LocalStatement copy() {
        return copyParentAndSourceTo(new LocalStatement(name.copy()));
    }
}
