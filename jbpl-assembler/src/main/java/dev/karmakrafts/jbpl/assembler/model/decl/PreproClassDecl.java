package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreproClassDecl extends AbstractExprContainer implements Declaration {
    public String name;

    public PreproClassDecl(final @NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
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
    public void evaluate(final @NotNull AssemblerContext context) {
    }
}
