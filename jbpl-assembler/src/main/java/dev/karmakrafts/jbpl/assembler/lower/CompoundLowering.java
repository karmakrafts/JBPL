package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.Element;
import dev.karmakrafts.jbpl.assembler.model.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.decl.Declaration;
import dev.karmakrafts.jbpl.assembler.model.decl.FunctionDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.InjectorDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.statement.CompoundStatement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Lifts all elements from a {@link CompoundStatement} into its
 * parent {@link ElementContainer}.
 * Mostly to reduce overhead when traversing the tree.
 */
public final class CompoundLowering implements ElementVisitor {
    public static final CompoundLowering INSTANCE = new CompoundLowering();

    private CompoundLowering() {
    }

    private <C extends ElementContainer> @NotNull C expandCompounds(final @NotNull C container) {
        final var newElements = new ArrayList<Element>();
        for (final var element : container.getElements()) {
            if (!(element instanceof CompoundStatement compound)) {
                newElements.add(element);
                continue;
            }
            newElements.addAll(compound.getElements());
        }
        container.clearElements();
        container.addElementsVerbatim(newElements); // We don't want to override the original parent to retain source info
        return container;
    }

    @Override
    public @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        return expandCompounds(file);
    }

    @Override
    public @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        return expandCompounds(functionDecl);
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        return expandCompounds(macroDecl);
    }

    @Override
    public @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        return expandCompounds(injectorDecl);
    }
}
