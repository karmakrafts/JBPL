package dev.karmakrafts.jbpl.assembler.model;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public enum AccessModifier {
    // @formatter:off
    PUBLIC(Opcodes.ACC_PUBLIC),
    PROTECTED(Opcodes.ACC_PROTECTED),
    PRIVATE(Opcodes.ACC_PRIVATE),
    STATIC(Opcodes.ACC_STATIC),
    FINAL(Opcodes.ACC_FINAL),
    SYNC(Opcodes.ACC_SYNCHRONIZED);
    // @formatter:on

    public final int encodedValue;

    AccessModifier(final int encodedValue) {
        this.encodedValue = encodedValue;
    }

    public static int combine(final @NotNull Collection<AccessModifier> modifiers) {
        var result = 0;
        for(var modifier: modifiers) {
            result |= modifier.encodedValue;
        }

        return result;
    }
}
