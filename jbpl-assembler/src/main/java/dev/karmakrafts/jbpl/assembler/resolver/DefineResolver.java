package dev.karmakrafts.jbpl.assembler.resolver;

import dev.karmakrafts.jbpl.assembler.Scope;
import dev.karmakrafts.jbpl.assembler.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class DefineResolver extends ScopeAwareElementVisitor {
    public final HashMap<Scope, HashMap<String, DefineStatement>> defines = new HashMap<>();

    public static @NotNull DefineResolver analyze(final @NotNull AssemblyFile file) {
        final var analyzer = new DefineResolver();
        file.accept(analyzer);
        return analyzer;
    }

    @Override
    public @NotNull Statement visitDefine(final @NotNull DefineStatement defineStatement) {
        final var scope = getScope();
        final var scopeMap = defines.computeIfAbsent(scope, s -> new HashMap<>());
        scopeMap.put(defineStatement.name, defineStatement);
        return defineStatement;
    }

    public @Nullable DefineStatement resolve(final @NotNull Scope scope, final @NotNull String name) {
        return scope.find(currentScope -> {
            final var scopeMap = defines.get(currentScope);
            if (scopeMap == null) {
                return null;
            }
            return scopeMap.get(name);
        });
    }
}
