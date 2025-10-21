package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import org.jetbrains.annotations.NotNull;

public final class CompoundStatement extends AbstractElementContainer implements Statement {
    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        for (final var element : elements) {
            if (element instanceof Statement statement) {
                statement.evaluate(context);
            }
        }
    }
}
