package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class SelectorReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public SelectorReferenceExpr(final @NotNull String name) {
        this.name = name;
    }
}
