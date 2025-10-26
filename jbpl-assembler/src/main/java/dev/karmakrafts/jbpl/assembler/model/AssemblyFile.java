package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceLocation;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class AssemblyFile extends AbstractElementContainer implements ScopeOwner {
    public final String path;
    public final ArrayList<Token> source = new ArrayList<>();

    public AssemblyFile(final @NotNull String path) {
        this.path = path;
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
    public @NotNull ElementContainer getParent() {
        return this;
    }

    @Override
    public void setParent(final @Nullable ElementContainer parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssemblyFile copy() {
        final var result = new AssemblyFile(path); // Don't explicitly copy source tokens & range since it's hardcoded
        result.source.addAll(source); // We retain the same underlying token references
        result.addElements(getElements().stream().map(Element::copy).toList());
        return result;
    }
}
