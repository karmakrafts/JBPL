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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class CompoundExpr extends AbstractElementContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return TypeCommonizer.getCommonReturnType(getElements(), context).orElseThrow().resolveIfNeeded(context);
    }

    @Override
    public @NotNull CompoundExpr copy() {
        final var result = copyParentAndSourceTo(new CompoundExpr());
        result.addElements(getElements().stream().map(Element::copy).toList());
        return result;
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("{\n%s\n}", elements.stream()
            .map(Element::toString)
            .collect(Collectors.joining(", ")));
    } // @formatter:on
}
