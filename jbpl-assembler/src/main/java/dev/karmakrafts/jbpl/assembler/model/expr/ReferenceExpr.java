package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class ReferenceExpr extends AbstractReceiverExpr implements Expr, ExprContainer {
    public String name;

    public ReferenceExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }

    private @NotNull DefineStatement getDefine(final @NotNull AssemblerContext context) {
        final var scope = context.getScope();
        final var define = context.defineResolver.resolve(scope, name);
        if (define == null) {
            throw new IllegalStateException(String.format("Could not find define '%s' in scope %s", name, scope));
        }
        return define;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return getDefine(context).type;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return getDefine(context).getValue().evaluateAsConst(context);
    }
}
