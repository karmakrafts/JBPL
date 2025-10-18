package dev.karmakrafts.jbpl.assembler.resolver;

import dev.karmakrafts.jbpl.assembler.Scope;
import dev.karmakrafts.jbpl.assembler.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.Declaration;
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class TypeResolver extends ScopeAwareElementVisitor {
    public final HashMap<Scope, HashMap<String, PreproClassDecl>> preproClasses = new HashMap<>();

    public static @NotNull TypeResolver analyze(final @NotNull AssemblyFile file) {
        final var analyzer = new TypeResolver();
        file.accept(analyzer);
        return analyzer;
    }

    @Override
    public @NotNull Declaration visitPreproClass(final @NotNull PreproClassDecl preproClassDecl) {
        final var scope = getScope();
        final var scopeMap = preproClasses.computeIfAbsent(scope, s -> new HashMap<>());
        scopeMap.put(preproClassDecl.name, preproClassDecl);
        return preproClassDecl;
    }

    public @Nullable PreproClassDecl resolvePreproClass(final @NotNull Scope scope, final @NotNull String name) {
        return scope.find(currentScope -> {
            final var scopeMap = preproClasses.get(currentScope);
            if (scopeMap == null) {
                return null;
            }
            return scopeMap.get(name);
        });
    }
}
