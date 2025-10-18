package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UnitExpr extends AbstractElement implements Expr {
    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return this; // Unit expression evaluate to themselves
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof UnitExpr;
    }

    @Override
    public String toString() {
        return "UnitExpr";
    }
}
