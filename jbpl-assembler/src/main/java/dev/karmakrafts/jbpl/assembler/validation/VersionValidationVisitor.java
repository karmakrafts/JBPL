package dev.karmakrafts.jbpl.assembler.validation;

import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.statement.VersionStatement;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class VersionValidationVisitor implements ElementVisitor {
    public static final VersionValidationVisitor INSTANCE = new VersionValidationVisitor();
    private boolean isVersionSet = false;

    private VersionValidationVisitor() {}

    @Override
    public @NotNull Statement visitVersionStatement(final @NotNull VersionStatement versionStatement) {
        if (isVersionSet) {
            throw new RuntimeException(new ValidationException("Cannot set bytecode version more than once per file",
                SourceDiagnostic.from(versionStatement),
                null));
        }
        isVersionSet = true;
        return versionStatement;
    }
}
