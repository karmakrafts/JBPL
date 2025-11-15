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
import dev.karmakrafts.jbpl.assembler.model.statement.TypeAliasStatement;
import org.jetbrains.annotations.NotNull;

public record UnresolvedType(Expr name) implements Type {
    @Override
    public boolean isResolved() {
        return false;
    }

    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return name.evaluateAs(context, String.class);
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName(context);
        final var typeAlias = context.resolveByName(TypeAliasStatement.class, name);
        if (typeAlias != null) {
            // Type aliases can shadow prepro class definitions
            return typeAlias.resolve(context);
        }
        return new PreproClassType(name); // TODO: cache this?
    }

    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) throws EvaluationException {
        return resolve(context).getCategory(context);
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException {
        return resolve(context).createDefaultValue(context);
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException {
        return resolve(context).materialize(context);
    }
}
