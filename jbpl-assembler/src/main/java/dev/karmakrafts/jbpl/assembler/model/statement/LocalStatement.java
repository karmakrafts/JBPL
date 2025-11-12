package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class LocalStatement extends AbstractExprContainer implements Statement, NamedElement {
    public static final int NAME_INDEX = 0;
    public static final int INDEX_INDEX = 1;

    public LocalStatement(final @NotNull Expr name, final @NotNull Expr index) {
        addExpression(name);
        addExpression(index);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        getName().setParent(null);
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull Expr getIndex() {
        return getExpressions().get(INDEX_INDEX);
    }

    public void setIndex(final @NotNull Expr index) {
        getIndex().setParent(null);
        index.setParent(this);
        getExpressions().set(INDEX_INDEX, index);
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAs(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        // Make this local available in the current frame during further evaluation, no forward refs
        final var name = getName().evaluateAs(context, String.class);
        context.peekFrame().locals.put(name, this);
    }

    @Override
    public @NotNull LocalStatement copy() {
        return copyParentAndSourceTo(new LocalStatement(getName().copy(), getIndex().copy()));
    }
}
