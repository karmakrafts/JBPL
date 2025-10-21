package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.source.SourceLocation;
import dev.karmakrafts.jbpl.assembler.model.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AssemblyFile extends AbstractElementContainer implements ScopeOwner, ReturnTarget {
    public final String path;
    public final List<Token> source;

    public AssemblyFile(final @NotNull String path, final @NotNull List<Token> source) {
        this.path = path;
        this.source = source;
    }

    public @NotNull SourceRange getSourceRange(final TokenRange range) {
        if (range.isUndefined() || range.isSynthetic()) {
            return new SourceRange(path, 0, 0, 0, 0);
        }
        final var firstToken = source.get(range.start());
        final var lastToken = source.get(range.end());
        return new SourceRange(path,
            firstToken.getLine(),
            firstToken.getCharPositionInLine(),
            lastToken.getLine(),
            lastToken.getCharPositionInLine());
    }

    public @NotNull SourceLocation getSourceLocation(final TokenRange range) {
        if (range.isUndefined() || range.isSynthetic()) {
            return new SourceLocation(path, 0, 0);
        }
        final var firstToken = source.get(range.start());
        return new SourceLocation(path, firstToken.getLine(), firstToken.getCharPositionInLine());
    }

    @Override
    public @NotNull TokenRange getTokenRange() {
        return new TokenRange(0, source.size() - 1);
    }

    @Override
    public void setTokenRange(final @NotNull TokenRange tokenRange) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable ElementContainer getParent() {
        return null;
    }

    @Override
    public void setParent(final @Nullable ElementContainer parent) {
        throw new UnsupportedOperationException();
    }
}
