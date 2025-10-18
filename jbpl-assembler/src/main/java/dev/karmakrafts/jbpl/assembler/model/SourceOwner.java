package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import org.jetbrains.annotations.NotNull;

public interface SourceOwner {
    @NotNull TokenRange getTokenRange();

    void setTokenRange(final @NotNull TokenRange tokenRange);
}
