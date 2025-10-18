package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class BlockReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public BlockReferenceExpr(final @NotNull String name) {
        this.name = name;
    }
}
