package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCallExpr extends AbstractReceiverExpr implements Expr {
    private final ArrayList<Pair<@Nullable Expr, Expr>> arguments = new ArrayList<>();

    public AbstractCallExpr(final @NotNull Expr receiver) {
        super(receiver);
    }

    public void clearArguments() {
        arguments.clear();
    }

    public @NotNull Pair<@Nullable Expr, Expr> getArgument(final int index) {
        return arguments.get(index + 1);
    }

    public void addArgument(final @Nullable Expr name, final @NotNull Expr argument) {
        if (name != null) {
            name.setParent(this);
        }
        argument.setParent(this);
        arguments.add(new Pair<>(name, argument));
    }

    public void addArguments(final @NotNull Iterable<Pair<@Nullable Expr, Expr>> arguments) {
        for (final var pair : arguments) {
            addArgument(pair.left(), pair.right());
        }
    }

    public @NotNull List<Pair<@Nullable Expr, Expr>> getArguments() {
        return arguments;
    }
}
