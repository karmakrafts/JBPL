package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class SelectorReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public SelectorReferenceExpr(final @NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.SELECTOR;
    }

    private @NotNull SelectorDecl getSelector(final @NotNull AssemblerContext context) {
        final var scope = context.getScope();
        final var define = context.selectorResolver.resolve(scope, name);
        if (define == null) {
            throw new IllegalStateException(String.format("Could not find selector '%s' in scope %s", name, scope));
        }
        return define;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return LiteralExpr.of(getSelector(context));
    }
}
