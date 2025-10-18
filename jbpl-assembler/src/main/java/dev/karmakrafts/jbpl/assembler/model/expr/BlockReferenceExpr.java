package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class BlockReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public BlockReferenceExpr(final @NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.BLOCK;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        final var scope = context.getScope();
        final var block = context.blockResolver.resolve(scope, name);
        if (block == null) {
            throw new IllegalStateException(String.format("Could not find block '%s' in scope %s", name, scope));
        }
        return AnonBlockExpr.copyOf(block); // We evaluate these refs into an anon copy of the named block
    }
}
