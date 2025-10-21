package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.ReturnStatement;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class CompoundExpr extends AbstractElementContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        final var elements = getElements();
        final var lastElement = elements.get(elements.size() - 1);
        if (lastElement instanceof ReturnStatement returnStatement) {
            return returnStatement.getValue().getType(context);
        }
        else if (lastElement instanceof Expr expr) {
            return expr.getType(context);
        }
        return BuiltinType.VOID; // Assume void for anything else
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {

    }
}
