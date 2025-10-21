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
        element.setParent(null);
    }

    @Override
    public void clearElements() {
        for (final var element : elements) {
            element.setParent(null);
        }
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
