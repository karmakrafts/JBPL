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

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ClassType(String name) implements Type {
    public ClassType(final @NotNull Class<?> type) {
        this(org.objectweb.asm.Type.getInternalName(type));
    }

    public static @NotNull Optional<ClassType> tryParse(final @Nullable String value) {
        if (value == null || !value.startsWith("<") || !value.endsWith(">")) {
            return Optional.empty();
        }
        final var name = value.substring(1, value.length() - 1);
        return Optional.of(new ClassType(name));
    }

    public @NotNull Class<?> loadClass() throws ClassNotFoundException {
        return Class.forName(name.replace('/', '.'));
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        return this;
    }

    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) {
        return TypeCategory.OBJECT;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("JVM class has no default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        return org.objectweb.asm.Type.getObjectType(name);
    }

    @Override
    public @NotNull String toString() {
        return String.format("<%s>", name);
    }
}
