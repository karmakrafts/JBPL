package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.decl.BlockDecl;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class AnonBlockExpr extends AbstractStatementContainer implements Expr, ScopeOwner {
    public static @NotNull AnonBlockExpr copyOf(final @NotNull BlockDecl decl) {
        final var block = new AnonBlockExpr();
        block.addStatements(decl.getStatements());
        return block;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.BLOCK;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return this; // Anon blocks evaluate to themselves
    }
}
