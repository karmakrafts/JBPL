package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDeclarationContainer extends AbstractElementContainer implements DeclarationContainer {
    private final ArrayList<Declaration> declarations = new ArrayList<>();

    @Override
    public void addDeclaration(final @NotNull Declaration declaration) {
        declaration.setParent(this);
        declarations.add(declaration);
    }

    @Override
    public void removeDeclaration(final @NotNull Declaration declaration) {
        declarations.remove(declaration);
        declaration.setParent(null);
    }

    @Override
    public void clearDeclarations() {
        for (final var decl : declarations) {
            decl.setParent(null);
        }
        declarations.clear();
    }

    @Override
    public @NotNull List<Declaration> getDeclarations() {
        return declarations;
    }
}
