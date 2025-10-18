package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import org.jetbrains.annotations.NotNull;

public final class BlockDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
    public String name;

    public BlockDecl(final @NotNull String name) {
        this.name = name;
    }
}
