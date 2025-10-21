package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractStatementContainer extends AbstractElementContainer implements StatementContainer {
    @Override
    public void addStatementVerbatim(final @NotNull Statement statement) {
        elements.add(statement);
    }

    @Override
    public void addStatement(final @NotNull Statement statement) {
        statement.setParent(this);
        elements.add(statement);
    }

    @Override
    public void removeStatement(@NotNull Statement statement) {
        elements.remove(statement);
        statement.setParent(null);
    }

    @Override
    public void clearStatements() {
        for (final var statement : elements) {
            statement.setParent(null);
        }
        elements.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull List<Statement> getStatements() {
        return (List<Statement>) (Object) elements;
    }
}
