package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class NoopStatement extends AbstractElement implements Statement {
    private NoopStatement() {
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
    }
}
