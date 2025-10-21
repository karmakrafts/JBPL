package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class MacroCallExpr extends AbstractCallExpr implements Expr {
    public String name;

    public MacroCallExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }

    private @NotNull MacroDecl getMacro(final @NotNull AssemblerContext context) {
        final var scope = context.getScope();
        final var macro = context.macroResolver.resolve(scope, name);
        if (macro == null) {
            throw new IllegalStateException(String.format("Could not find macro '%s' in current scope %s",
                name,
                scope));
        }
        return macro;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return getMacro(context).returnType.evaluateAsConst(context, Type.class);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        //TODO:
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return LiteralExpr.unit();
    }
}
