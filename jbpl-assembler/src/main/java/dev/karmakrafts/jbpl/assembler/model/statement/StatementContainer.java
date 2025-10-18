package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.model.Element;
import dev.karmakrafts.jbpl.assembler.model.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface StatementContainer extends ElementContainer {
    void addStatementVerbatim(final @NotNull Statement statement);

    void addStatement(final @NotNull Statement statement);

    void removeStatement(final @NotNull Statement statement);

    void clearStatements();

    @NotNull List<? extends Statement> getStatements();

    default void addStatementsVerbatim(final @NotNull Iterable<? extends Statement> statements) {
        statements.forEach(this::addStatementVerbatim);
    }

    default void addStatements(final @NotNull Iterable<? extends Statement> statements) {
        statements.forEach(this::addStatement);
    }

    default void removeStataments(final @NotNull Iterable<? extends Statement> statements) {
        statements.forEach(this::removeStatement);
    }

    @Override
    default void clearElements() {
        clearStatements();
    }

    @Override
    default void addElementVerbatim(final @NotNull Element element) {
        if (!(element instanceof Statement statement)) {
            throw new IllegalArgumentException("Element is not a statement");
        }
        addStatementVerbatim(statement);
    }

    @Override
    default void addElement(final @NotNull Element element) {
        if (!(element instanceof Statement statement)) {
            throw new IllegalArgumentException("Element is not a statement");
        }
        addStatement(statement);
    }

    @Override
    default void removeElement(final @NotNull Element element) {
        if (!(element instanceof Statement statement)) {
            throw new IllegalArgumentException("Element is not a statement");
        }
        removeStatement(statement);
    }

    @Override
    default @NotNull List<? extends Element> getElements() {
        return getStatements();
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<? extends Statement> transformChildren(final @NotNull ElementVisitor visitor) {
        return (List<? extends Statement>) ElementContainer.super.transformChildren(visitor);
    }
}
