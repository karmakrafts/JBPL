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
