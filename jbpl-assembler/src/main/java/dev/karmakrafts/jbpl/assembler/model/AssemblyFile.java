package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class AssemblyFile extends AbstractElementContainer implements ScopeOwner {
    public final String path;
    public final ArrayList<Token> source = new ArrayList<>();

    public AssemblyFile(final @NotNull String path) {
        this.path = path;
        tokenRange = null; // We lazily assign the token range for the file
    }

    public @NotNull List<Token> getTokens(final @NotNull TokenRange range) {
        if (range.isUndefined() || range.isSynthetic()) {
            return List.of();
        }
        return source.subList(range.start(), range.end() + 1); // subList toIndex is exclusive
    }

    public @NotNull SourceRange getSourceRange(final @NotNull TokenRange range) {
        if (range.isUndefined()) {
            return SourceRange.UNDEFINED;
        }
        if (range.isSynthetic()) {
            return SourceRange.SYNTHETIC;
        }
        final var startIndex = range.start();
        final var endIndex = range.end();
        final var firstToken = source.get(startIndex);
        if (startIndex == endIndex) {
            final var lineIndex = firstToken.getLine() - 1;
            final var startColumn = firstToken.getCharPositionInLine();
            final var endColumn = startColumn + (firstToken.getText().length() - 1);
            return new SourceRange(lineIndex, startColumn, lineIndex, endColumn);
        }
        final var lastToken = source.get(range.end());
        return new SourceRange(firstToken.getLine() - 1,
            firstToken.getCharPositionInLine(),
            lastToken.getLine() - 1,
            lastToken.getCharPositionInLine());
    }

    @Override
    public @NotNull TokenRange getTokenRange() {
        // Lazily create file token range when needed
        if (tokenRange == null) {
            tokenRange = new TokenRange(0, source.size() - 1);
        }
        return tokenRange;
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

    @Override
    public String toString() {
        return path;
    }
}
