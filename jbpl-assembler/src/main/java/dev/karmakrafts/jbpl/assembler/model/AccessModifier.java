/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.model;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Locale;

public enum AccessModifier {
    // @formatter:off
    PUBLIC   (true,  true,  true,  Opcodes.ACC_PUBLIC),
    PROTECTED(true,  true,  true,  Opcodes.ACC_PROTECTED),
    PRIVATE  (true,  true,  true,  Opcodes.ACC_PRIVATE),
    STATIC   (true,  true,  true,  Opcodes.ACC_STATIC),
    FINAL    (true,  true,  true,  Opcodes.ACC_FINAL),
    SYNC     (true,  false, false, Opcodes.ACC_SYNCHRONIZED),
    TRANSIENT(false, true,  false, Opcodes.ACC_TRANSIENT),
    VOLATILE (false, true,  false, Opcodes.ACC_VOLATILE);
    // @formatter:on

    public final int encodedValue;
    public final boolean applicableToFunction;
    public final boolean applicableToField;
    public final boolean applicableToClass;

    AccessModifier(final boolean applicableToFunction,
                   final boolean applicableToField,
                   final boolean applicableToClass,
                   final int encodedValue) {
        this.encodedValue = encodedValue;
        this.applicableToFunction = applicableToFunction;
        this.applicableToField = applicableToField;
        this.applicableToClass = applicableToClass;
    }

    public static int combine(final @NotNull Collection<AccessModifier> modifiers) {
        var result = 0;
        for (var modifier : modifiers) {
            result |= modifier.encodedValue;
        }
        return result;
    }

    @Override
    public @NotNull String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
