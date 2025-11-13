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

package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PreproClassDecl extends AbstractExprContainer implements Declaration, NamedElement {
    public static final int NAME_INDEX = 0;

    public PreproClassDecl(final @NotNull Expr name) {
        addExpression(name);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        getName().setParent(null);
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public void addFields(final @NotNull Map<Expr, Expr> fields) {
        for (final var entry : fields.entrySet()) {
            addField(entry.getKey(), entry.getValue());
        }
    }

    public @NotNull LinkedHashMap<Expr, Expr> getFields() {
        final var fields = new LinkedHashMap<Expr, Expr>();
        for (var index = 0; index < getFieldCount(); index++) {
            fields.put(getFieldName(index), getFieldType(index));
        }
        return fields;
    }

    public void addField(final @NotNull Expr name, final @NotNull Expr type) {
        addExpression(name);
        addExpression(type);
    }

    public void addFields(final @NotNull Iterable<Pair<Expr, Expr>> fields) {
        for (final var pair : fields) {
            addField(pair.left(), pair.right());
        }
    }

    public @NotNull Expr getFieldName(final int index) {
        final var elementIndex = index << 1;
        if (elementIndex >= elements.size()) {
            throw new IllegalArgumentException(String.format("Field %d does not exist in PreproClassDecl",
                elementIndex));
        }
        return (Expr) elements.get(elementIndex);
    }

    public void setFieldName(final int index, final @NotNull Expr name) {
        final var elementIndex = index << 1;
        if (elementIndex >= elements.size()) {
            throw new IllegalArgumentException(String.format("Field %d does not exist in PreproClassDecl",
                elementIndex));
        }
        elements.set(elementIndex, name);
    }

    public @NotNull Expr getFieldType(final int index) {
        final var elementIndex = index << 1;
        if (elementIndex >= elements.size()) {
            throw new IllegalArgumentException(String.format("Field %d does not exist in PreproClassDecl",
                elementIndex));
        }
        return (Expr) elements.get(elementIndex);
    }

    public void setFieldType(final int index, final @NotNull Expr type) {
        final var elementIndex = index << 1;
        if (elementIndex >= elements.size()) {
            throw new IllegalArgumentException(String.format("Field %d does not exist in PreproClassDecl",
                elementIndex));
        }
        elements.set(elementIndex + 1, type);
    }

    public int getFieldCount() {
        return elements.size() >> 1;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAs(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
    }

    @Override
    public @NotNull PreproClassDecl copy() {
        final var clazz = copyParentAndSourceTo(new PreproClassDecl(getName().copy()));
        // @formatter:off
        clazz.addFields(getFields().entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey().copy(), entry.getValue().copy()))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
        // @formatter:on
        return clazz;
    }
}
