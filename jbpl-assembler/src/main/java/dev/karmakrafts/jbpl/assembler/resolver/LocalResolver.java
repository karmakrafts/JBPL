package dev.karmakrafts.jbpl.assembler.resolver;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.Scope;
import dev.karmakrafts.jbpl.assembler.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class LocalResolver extends ScopeAwareElementVisitor {
    private final HashMap<Scope, HashMap<Expr, LocalStatement>> unresolvedLocals = new HashMap<>();
    private final HashMap<Scope, HashMap<String, LocalStatement>> resolvedLocals = new HashMap<>();

    public static @NotNull LocalResolver analyze(final @NotNull AssemblyFile file) {
        final var analyzer = new LocalResolver();
        file.accept(analyzer);
        return analyzer;
    }

    public void resolveLocalNames(final @NotNull AssemblerContext context) {
        for (final var scopeEntry : unresolvedLocals.entrySet()) {
            final var scope = scopeEntry.getKey();
            final var resolvedScopeMap = resolvedLocals.computeIfAbsent(scope, s -> new HashMap<>());
            for (final var unresolvedEntry : scopeEntry.getValue().entrySet()) {
                final var name = unresolvedEntry.getKey().evaluateAsLiteral(context, String.class);
                resolvedScopeMap.put(name, unresolvedEntry.getValue());
            }
        }
    }

    @Override
    public @NotNull Statement visitLocal(final @NotNull LocalStatement localStatement) {
        final var scopeMap = unresolvedLocals.computeIfAbsent(getScope(), s -> new HashMap<>());
        scopeMap.put(localStatement.name, localStatement);
        return super.visitLocal(localStatement);
    }

    public @Nullable LocalStatement resolve(final @NotNull Scope scope, final @NotNull String name) {
        return scope.find(currentScope -> {
            final var scopeMap = resolvedLocals.get(currentScope);
            if (scopeMap == null) {
                return null;
            }
            return scopeMap.get(name);
        });
    }
}
