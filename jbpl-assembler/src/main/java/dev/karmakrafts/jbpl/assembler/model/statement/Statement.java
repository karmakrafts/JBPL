package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.jetbrains.annotations.NotNull;

public interface Statement extends Element {
    @Override
    @NotNull Statement copy();
}
