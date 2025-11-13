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
    PUBLIC   (Opcodes.ACC_PUBLIC),
    PROTECTED(Opcodes.ACC_PROTECTED),
    PRIVATE  (Opcodes.ACC_PRIVATE),
    STATIC   (Opcodes.ACC_STATIC),
    FINAL    (Opcodes.ACC_FINAL),
    SYNC     (Opcodes.ACC_SYNCHRONIZED);
    // @formatter:on

    public final int encodedValue;

    AccessModifier(final int encodedValue) {
        this.encodedValue = encodedValue;
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
