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

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ExprContainer extends ElementContainer {
    void addExpressionVerbatim(final @NotNull Expr expr);

    void addExpression(final @NotNull Expr expr);

    void removeExpression(final @NotNull Expr expr);

    void clearExpressions();

    List<? extends Expr> getExpressions();

    default void addExpressionsVerbatim(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::addExpressionVerbatim);
    }

    default void addExpressions(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::addExpression);
    }

    default void removeExpressions(final @NotNull Iterable<? extends Expr> expressions) {
        expressions.forEach(this::removeExpression);
    }

    @Override
    default void clearElements() {
        clearExpressions();
    }

    @Override
    default void addElement(final @NotNull Element element) {
        if (!(element instanceof Expr expr)) {
            throw new IllegalArgumentException("Element is not an expression");
        }
        addExpression(expr);
    }

    @Override
    default void removeElement(final @NotNull Element element) {
        if (!(element instanceof Expr expr)) {
            throw new IllegalArgumentException("Element is not an expression");
        }
        removeExpression(expr);
    }

    @Override
    default @NotNull List<? extends Element> getElements() {
        return getExpressions();
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<? extends Expr> transformChildren(final @NotNull ElementVisitor visitor) {
        return (List<? extends Expr>) ElementContainer.super.transformChildren(visitor);
    }
}
