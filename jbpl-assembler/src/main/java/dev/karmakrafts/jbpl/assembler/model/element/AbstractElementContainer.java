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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractElementContainer extends AbstractElement implements ElementContainer {
    protected final ArrayList<Element> elements = new ArrayList<>();

    @Override
    public void addElementVerbatim(final @NotNull Element element) {
        elements.add(element);
    }

    @Override
    public void addElement(final @NotNull Element element) {
        element.setParent(this);
        elements.add(element);
    }

    @Override
    public void removeElement(final @NotNull Element element) {
        elements.remove(element);
    }

    @Override
    public void clearElements() {
        elements.clear();
    }

    @Override
    public @NotNull List<? extends Element> getElements() {
        return elements;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (!(obj instanceof AbstractElementContainer abstractContainer)) {
            if (!(obj instanceof ElementContainer container)) {
                return false;
            }
            return elements.equals(container.getElements());
        }
        return elements.equals(abstractContainer.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
