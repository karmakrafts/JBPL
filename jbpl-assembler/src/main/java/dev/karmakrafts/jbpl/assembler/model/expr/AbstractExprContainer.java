package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractExprContainer extends AbstractElementContainer implements ExprContainer {
    @Override
    public void addExpressionVerbatim(final @NotNull Expr expr) {
        elements.add(expr);
    }

    @Override
    public void addExpression(final @NotNull Expr expr) {
        expr.setParent(this);
        elements.add(expr);
    }

    @Override
    public void removeExpression(final @NotNull Expr expr) {
        elements.remove(expr);
        expr.setParent(null);
    }

    @Override
    public void clearExpressions() {
        for (final var expr : elements) {
            expr.setParent(null);
        }
        elements.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Expr> getExpressions() {
        return (List<Expr>) (Object) elements;
    }
}
