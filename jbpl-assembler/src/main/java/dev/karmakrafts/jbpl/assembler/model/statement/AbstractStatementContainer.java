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

package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractStatementContainer extends AbstractElementContainer implements StatementContainer {
    @Override
    public void addStatementVerbatim(final @NotNull Statement statement) {
        elements.add(statement);
    }

    @Override
    public void addStatement(final @NotNull Statement statement) {
        statement.setParent(this);
        elements.add(statement);
    }

    @Override
    public void removeStatement(@NotNull Statement statement) {
        elements.remove(statement);
    }

    @Override
    public void clearStatements() {
        elements.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull List<Statement> getStatements() {
        return (List<Statement>) (Object) elements;
    }
}
