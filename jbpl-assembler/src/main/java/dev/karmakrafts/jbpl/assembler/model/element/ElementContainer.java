package dev.karmakrafts.jbpl.assembler.model.element;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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

    @SuppressWarnings("unchecked")
    default <E extends Element> @NotNull Optional<E> findElement(final @NotNull Class<E> type, final @NotNull Predicate<E> filter) { // @formatter:off
        return getElements().stream()
            .filter(type::isInstance)
            .map(element -> (E) element)
            .filter(filter)
            .findFirst();
    } // @formatter:on

    @SuppressWarnings("unchecked")
    default <E extends Element> @NotNull Optional<E> findElement(final @NotNull Class<E> type) { // @formatter:off
        return getElements().stream()
            .filter(type::isInstance)
            .map(element -> (E)element)
            .findFirst();
    } // @formatter:on

    @SuppressWarnings("unchecked")
    default <E extends Element> @NotNull List<E> findElements(final @NotNull Class<E> type, final @NotNull Predicate<E> filter) { // @formatter:off
        return getElements().stream()
            .filter(type::isInstance)
            .map(element -> (E)element)
            .filter(filter)
            .toList();
    } // @formatter:on

    @SuppressWarnings("unchecked")
    default <E extends Element> @NotNull List<E> findElements(final @NotNull Class<E> type) { // @formatter:off
        return getElements().stream()
            .filter(type::isInstance)
            .map(element -> (E)element)
            .toList();
    } // @formatter:on

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
    default void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        var hasScope = false;
        if (this instanceof ScopeOwner scopeOwner) {
            context.pushFrame(scopeOwner);
            hasScope = true;
        }
        for (final var element : getElements()) {
            if (!element.isEvaluatedDirectly()) {
                continue;
            }
            element.evaluate(context);
            if(context.clearRet()) {
                break;
            }
        }
        if (hasScope) {
            context.popFrame();
        }
    }
}
