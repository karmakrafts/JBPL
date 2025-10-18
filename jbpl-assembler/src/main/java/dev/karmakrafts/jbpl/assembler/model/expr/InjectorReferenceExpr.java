package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class InjectorReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public InjectorReferenceExpr(final @NotNull String name) {
        this.name = name;
    }
}
