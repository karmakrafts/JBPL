package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractCallExpr extends AbstractReceiverExpr {
    public AbstractCallExpr(final @NotNull Expr receiver) {
        super(receiver);
    }

    public void clearArguments() {
        final var receiver = getReceiver();
        clearExpressions();
        addExpression(receiver);
    }

    public @NotNull Expr getArgument(final int index) {
        return getExpressions().get(index + 1);
    }

    public void setArgument(final int index, final @NotNull Expr argument) {
        argument.setParent(this);
        elements.set(index + 1, argument);
    }

    public void addArgument(final @NotNull Expr argument) {
        argument.setParent(this);
        elements.add(argument);
    }

    public void addArguments(final @NotNull Iterable<? extends Expr> arguments) {
        arguments.forEach(this::addArgument);
    }

    public void removeArgument(final @NotNull Expr argument) {
        elements.remove(argument);
        argument.setParent(null);
    }

    public void removeArguments(final @NotNull Iterable<? extends Expr> arguments) {
        arguments.forEach(this::removeArgument);
    }

    public @NotNull List<Expr> getArguments() {
        return getExpressions().subList(RECEIVER_INDEX + 1, elements.size());
    }
}
