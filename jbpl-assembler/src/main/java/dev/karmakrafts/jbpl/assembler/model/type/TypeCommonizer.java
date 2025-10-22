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

package dev.karmakrafts.jbpl.assembler.model.type;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TypeCommonizer {
    private TypeCommonizer() {
    }

    public static @NotNull Optional<? extends Type> commonize(final @NotNull Collection<? extends Type> types) {
        final var categories = types.stream().map(Type::getCategory).collect(Collectors.toSet());
        if (categories.size() != 1) {
            return Optional.empty(); // If we found more than one type category, no commonization can occur
        }
        final var category = categories.stream().findFirst().orElseThrow();
        final var uniqueTypes = new HashSet<>(types);
        return switch(category) { // @formatter:off
            case INTEGER, FLOAT -> uniqueTypes.stream()
                .filter(BuiltinType.class::isInstance)
                .map(BuiltinType.class::cast)
                .max(BuiltinType::compareTo); // Ints and floats are ordered by size in ascending order in the enum
            default -> uniqueTypes.stream().findFirst();
        }; // @formatter:on
    }

    public static @NotNull Optional<? extends Type> commonize(final @NotNull Type... types) {
        return commonize(List.of(types));
    }
}
