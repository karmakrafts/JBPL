package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
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

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return null; // TODO: implement this
    }
}
