package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.jetbrains.annotations.NotNull;

public final class CompoundStatement extends AbstractElementContainer implements Statement {
    @Override
    public @NotNull CompoundStatement copy() {
        final var result = copyParentAndSourceTo(new CompoundStatement());
        result.addElements(getElements().stream().map(Element::copy).toList());
        return result;
    }
}
