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

package dev.karmakrafts.jbpl.assembler.model.element;

import dev.karmakrafts.jbpl.assembler.eval.Evaluable;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.util.Copyable;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

public interface Element extends SourceOwner, Evaluable, Copyable<Element> {
    @NotNull ElementAttributes getAttributes();

    default <E extends Element> @NotNull E copyParentAndSourceTo(final @NotNull E element) {
        element.setParent(getParent());
        return copySourcesTo(element);
    }

    @Nullable ElementContainer getParent();

    void setParent(final @Nullable ElementContainer parent);

    default void accept(final @NotNull ElementVisitor visitor) {
        visitor.visitElement(this);
    }

    default @NotNull Element transform(final @NotNull ElementVisitor visitor) {
        return visitor.visitElement(this);
    }

    @SuppressWarnings("unchecked")
    default <P> @NotNull Optional<P> findParent(final @NotNull Class<P> type, final @NotNull Predicate<P> filter) {
        final var parentStack = new Stack<Element>();
        parentStack.push(this);
        while (!parentStack.isEmpty()) {
            final var currentParent = parentStack.pop();
            if (type.isInstance(currentParent) && filter.test((P) currentParent)) {
                return Optional.of((P) currentParent);
            }
            final var grandParent = currentParent.getParent();
            if (grandParent == currentParent) {
                break; // Prevent endless recursion on tree roots
            }
            parentStack.push(grandParent);
        }
        return Optional.empty();
    }

    default @NotNull ScopeOwner getContainingScope() { // @formatter:off
        return findParent(ScopeOwner.class, owner -> true)
            .orElseThrow(() -> new IllegalStateException("Could not find parent scope for element"));
    } // @formatter:on

    default @NotNull AssemblyFile getContainingFile() { // @formatter:off
        return findParent(AssemblyFile.class, file -> true)
            .orElseThrow(() -> new IllegalStateException("Could not find parent file for element"));
    } // @formatter:on

    default @NotNull SourceRange getSourceRange() {
        return getContainingFile().getSourceRange(getTokenRange());
    }

    default @NotNull List<Token> getTokens() {
        return getContainingFile().getTokens(getTokenRange());
    }
}
