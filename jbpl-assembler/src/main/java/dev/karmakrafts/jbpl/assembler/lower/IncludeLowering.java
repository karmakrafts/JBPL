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

package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.Assembler;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.statement.CompoundStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.IncludeStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class IncludeLowering implements ElementVisitor {
    private final Assembler assembler;
    private final HashSet<String> alreadyIncluded = new HashSet<>();

    public IncludeLowering(final @NotNull Assembler assembler) {
        this.assembler = assembler;
    }

    private boolean shouldGetIncluded(final @NotNull Element element) {
        if (element instanceof IncludeVisibilityProvider visibilityProvider) {
            return visibilityProvider.shouldGetIncluded();
        }
        return true; // Other elements get included by default
    }

    @Override
    public @NotNull Statement visitInclude(final @NotNull IncludeStatement includeStatement) {
        final var includePath = includeStatement.path;
        if (alreadyIncluded.contains(includePath)) {
            return includeStatement;
        }
        final var includedFile = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseFile(includePath));
        final var statement = new CompoundStatement();
        // @formatter:off
        statement.addElementsVerbatim(includedFile.getElements().stream()
            .filter(this::shouldGetIncluded)
            .map(Element::copy)
            .toList());
        // @formatter:on
        alreadyIncluded.add(includePath);
        return statement;
    }
}
