package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractReceiverExpr extends AbstractExprContainer {
    public static final int RECEIVER_INDEX = 0;

    public AbstractReceiverExpr(final @NotNull Expr receiver) {
        addExpression(receiver);
    }

    public @NotNull Expr getReceiver() {
        return getExpressions().get(RECEIVER_INDEX);
    }

    public void setReceiver(final @NotNull Expr receiver) {
        receiver.setParent(this);
        if (elements.isEmpty()) {
            elements.add(receiver);
        }
        elements.set(RECEIVER_INDEX, receiver);
    }
}
