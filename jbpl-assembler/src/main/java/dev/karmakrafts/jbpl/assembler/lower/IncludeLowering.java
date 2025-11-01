package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.Assembler;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.statement.CompoundStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.IncludeStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

public final class IncludeLowering implements ElementVisitor {
    private final Assembler assembler;

    public IncludeLowering(final @NotNull Assembler assembler) {
        this.assembler = assembler;
    }

    @Override
    public @NotNull Statement visitInclude(final @NotNull IncludeStatement includeStatement) {
        final var includedFile = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseFile(includeStatement.path));
        final var statement = new CompoundStatement();
        // @formatter:off
        statement.addElementsVerbatim(includedFile.getElements().stream()
            .map(Element::copy)
            .toList());
        // @formatter:on
        return statement;
    }
}
