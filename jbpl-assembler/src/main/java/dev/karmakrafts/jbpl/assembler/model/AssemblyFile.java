/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
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
    public final ArrayList<Token> source = new ArrayList<>();
    public String path;

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
    public @Nullable ElementContainer getParent() {
        return null;
    }

    @Override
    public void setParent(final @Nullable ElementContainer parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void evaluate(@NotNull EvaluationContext context) throws EvaluationException {
        context.pushFrame(this);
        for (final var element : getElements()) {
            if (!element.isEvaluatedDirectly() || context.controlFlowState.clearCnt()) {
                continue;
            }
            element.evaluate(context);
            context.controlFlowState.clearReturnMask(); // Top level clears return mask completely
            context.clearStack(); // Clear of current frame, we don't care about any top level values
        }
        context.popFrame();
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
