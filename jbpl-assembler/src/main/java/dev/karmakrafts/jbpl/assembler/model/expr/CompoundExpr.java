package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.ReturnStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import org.jetbrains.annotations.NotNull;

public final class CompoundExpr extends AbstractElementContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return TypeCommonizer.getCommonType(this, context).orElseThrow();
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        final var elements = getElements();
        final var lastElement = elements.get(elements.size() - 1);
        if (lastElement instanceof ReturnStatement returnStatement) {
            return returnStatement.getValue().evaluateAsConst(context);
        }
        else if (lastElement instanceof Expr expr) {
            return expr.evaluateAsConst(context);
        }
        return LiteralExpr.unit();
    }
}
