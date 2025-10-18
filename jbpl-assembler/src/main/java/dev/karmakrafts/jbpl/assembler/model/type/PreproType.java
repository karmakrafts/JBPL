package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

public enum PreproType implements Type {
    // @formatter:off
    TYPE,
    FIELD_SIGNATURE,
    FUNCTION_SIGNATURE,
    SELECTOR,
    OPCODE,
    INSTRUCTION,
    INJECTOR;
    // @formatter:on

    @Override
    public boolean isMaterializable() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("Preprocessor types cannot be materialized");
    }
}