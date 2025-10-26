package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class LabelStatement extends AbstractExprContainer implements Statement, NamedElement {
    public static final int NAME_INDEX = 0;

    public LabelStatement(final @NotNull Expr name) {
        addExpression(name);
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName().evaluateAsConst(context, String.class);
        context.peekFrame().getOrCreateLabelNode(name); // Pre-allocate label
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        getExpressions().set(NAME_INDEX, name);
    }

    @Override
    public @NotNull LabelStatement copy() {
        return copyParentAndSourceTo(new LabelStatement(getName().copy()));
    }
}
