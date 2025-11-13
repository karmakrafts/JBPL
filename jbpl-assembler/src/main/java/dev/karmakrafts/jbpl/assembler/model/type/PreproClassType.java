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
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.PreproClassExpr;
import org.jetbrains.annotations.NotNull;

public record PreproClassType(@NotNull String name) implements Type {
    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.PREPROCESSOR;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException {
        final var scope = context.getScope();
        final var clazz = context.resolveByName(PreproClassDecl.class, name);
        if (clazz == null) {
            throw new IllegalStateException(String.format("Could not find preprocessor class '%s' in scope %s",
                name,
                scope));
        }
        final var result = new PreproClassExpr(this);
        for (final var entry : clazz.getFields().entrySet()) {
            final var name = entry.getKey().evaluateAsConst(context);
            final var value = entry.getValue().evaluateAs(context, Type.class).createDefaultValue(context);
            result.addArgument(name, value);
        }
        return result;
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("Preprocessor class types cannot be materialized");
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
