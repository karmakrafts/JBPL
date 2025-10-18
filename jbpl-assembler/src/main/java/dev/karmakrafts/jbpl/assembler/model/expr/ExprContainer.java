package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.Element;
import dev.karmakrafts.jbpl.assembler.model.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ExprContainer extends ElementContainer {
    void addExpressionVerbatim(final @NotNull Expr expr);

    void addExpression(final @NotNull Expr expr);

    void removeExpression(final @NotNull Expr expr);

    void clearExpressions();

    List<? extends Expr> getExpressions();

    default void addExpressionsVerbatim(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::addExpressionVerbatim);
    }

    default void addExpressions(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::addExpression);
    }

    default void removeExpressions(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::removeExpression);
    }

    @Override
    default void clearElements() {
        clearExpressions();
    }

    @Override
    default void addElement(final @NotNull Element element) {
        if (!(element instanceof Expr expr)) {
            throw new IllegalArgumentException("Element is not an expression");
        }
        addExpression(expr);
    }

    @Override
    default void removeElement(final @NotNull Element element) {
        if (!(element instanceof Expr expr)) {
            throw new IllegalArgumentException("Element is not an expression");
        }
        removeExpression(expr);
    }

    @Override
    default @NotNull List<? extends Element> getElements() {
        return getExpressions();
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<? extends Expr> transformChildren(final @NotNull ElementVisitor visitor) {
        return (List<? extends Expr>) ElementContainer.super.transformChildren(visitor);
    }
}
