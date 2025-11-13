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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractExprContainer extends AbstractElementContainer implements ExprContainer {
    @Override
    public void addExpressionVerbatim(final @NotNull Expr expr) {
        elements.add(expr);
    }

    @Override
    public void addExpression(final @NotNull Expr expr) {
        expr.setParent(this);
        elements.add(expr);
    }

    @Override
    public void removeExpression(final @NotNull Expr expr) {
        elements.remove(expr);
    }

    @Override
    public void clearExpressions() {
        elements.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Expr> getExpressions() {
        return (List<Expr>) (Object) elements;
    }
}
