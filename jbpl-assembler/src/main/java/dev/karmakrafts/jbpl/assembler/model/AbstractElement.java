package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractElement implements Element {
    public ElementContainer parent;
    public TokenRange tokenRange = TokenRange.UNDEFINED;

    protected AbstractElement() {
    }

    @Override
    public @Nullable ElementContainer getParent() {
        return parent;
    }

    @Override
    public void setParent(final @Nullable ElementContainer parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull TokenRange getTokenRange() {
        return tokenRange;
    }

    @Override
    public void setTokenRange(final @NotNull TokenRange tokenRange) {
        this.tokenRange = tokenRange;
    }
}
