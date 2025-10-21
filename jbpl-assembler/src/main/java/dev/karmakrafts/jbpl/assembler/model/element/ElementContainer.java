package dev.karmakrafts.jbpl.assembler.model.element;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ElementContainer extends Element {
    /**
     * Add the element to this container as is.
     *
     * @param element The element to add to this container.
     */
    void addElementVerbatim(final @NotNull Element element);

    /**
     * Add the element to this container and update its parent reference
     * to this container instance.
     *
     * @param element The element to add to this container.
     */
    void addElement(final @NotNull Element element);

    void removeElement(final @NotNull Element element);

    void clearElements();

    default void addElementsVerbatim(final @NotNull Iterable<? extends Element> elements) {
        elements.forEach(this::addElementVerbatim);
    }

    default void addElements(final @NotNull Iterable<? extends Element> elements) {
        elements.forEach(this::addElement);
    }

    default void removeElements(final @NotNull Iterable<? extends Element> elements) {
        elements.forEach(this::removeElement);
    }

    @NotNull List<? extends Element> getElements();

    default void acceptChildren(final @NotNull ElementVisitor visitor) {
        for (final var element : getElements()) {
            visitor.visitElement(element);
        }
    }

    default List<? extends Element> transformChildren(final @NotNull ElementVisitor visitor) {
        // @formatter:off
        return getElements().stream()
            .map(element -> element.transform(visitor))
            .toList();
        // @formatter:on
    }

    @Override
    default void evaluate(final @NotNull AssemblerContext context) {
        for (final var element : getElements()) {
            element.evaluate(context);
        }
    }
}
