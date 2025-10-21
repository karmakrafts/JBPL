package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.statement.NoopStatement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A visitor pass to remove any {@link EmptyDecl} or {@link NoopStatement}
 * from the tree to save time when traversing it.
 */
public final class NoopRemovalLowering implements ElementVisitor {
    public static final NoopRemovalLowering INSTANCE = new NoopRemovalLowering();

    private NoopRemovalLowering() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        final var elements = (List<Element>) file.getElements();
        // @formatter:off
        final var filteredElements = elements.stream()
            .filter(e -> !(e instanceof EmptyDecl) && !(e instanceof NoopStatement))
            .toList();
        // @formatter:on
        elements.clear();
        elements.addAll(filteredElements);
        return file;
    }

    @Override
    public @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        final var statements = injectorDecl.getStatements();
        // @formatter:off
        final var filteredStatements = statements.stream()
            .filter(s -> !(s instanceof NoopStatement))
            .toList();
        // @formatter:on
        statements.clear();
        statements.addAll(filteredStatements);
        return injectorDecl;
    }

    @Override
    public @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        final var statements = functionDecl.getStatements();
        // @formatter:off
        final var filteredStatements = statements.stream()
            .filter(s -> !(s instanceof NoopStatement))
            .toList();
        // @formatter:on
        statements.clear();
        statements.addAll(filteredStatements);
        return functionDecl;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        final var elements = (List<Element>) macroDecl.getElements();
        // @formatter:off
        final var filteredElements = elements.stream()
            .filter(e -> !(e instanceof EmptyDecl) && !(e instanceof NoopStatement))
            .toList();
        // @formatter:on
        elements.clear();
        elements.addAll(filteredElements);
        return macroDecl;
    }
}
