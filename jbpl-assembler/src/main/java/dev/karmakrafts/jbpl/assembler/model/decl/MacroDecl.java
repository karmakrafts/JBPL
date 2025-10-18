package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.model.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public final class MacroDecl extends AbstractElementContainer implements Declaration, ScopeOwner {
    public final LinkedHashMap<Expr, Expr> parameterTypes = new LinkedHashMap<>();
    public String name;

    public MacroDecl(final @NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }
}
