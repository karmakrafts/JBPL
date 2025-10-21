package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class EmptyDecl extends AbstractElement implements Declaration {
    private EmptyDecl() {
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {

    }
}
