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

package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DeclarationContainer extends ElementContainer {
    void addDeclaration(final @NotNull Declaration declaration);

    void removeDeclaration(final @NotNull Declaration declaration);

    void clearDeclarations();

    @NotNull List<? extends Declaration> getDeclarations();

    @Override
    default void clearElements() {
        clearDeclarations();
    }

    @Override
    default void addElement(final @NotNull Element element) {
        if (!(element instanceof Declaration declaration)) {
            throw new IllegalArgumentException("Element is not a declaration");
        }
        addDeclaration(declaration);
    }

    @Override
    default void removeElement(final @NotNull Element element) {
        if (!(element instanceof Declaration declaration)) {
            throw new IllegalArgumentException("Element is not a declaration");
        }
        removeDeclaration(declaration);
    }

    @Override
    default @NotNull List<? extends Element> getElements() {
        return getDeclarations();
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<? extends Declaration> transformChildren(final @NotNull ElementVisitor visitor) {
        return (List<? extends Declaration>) ElementContainer.super.transformChildren(visitor);
    }
}
