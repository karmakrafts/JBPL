package dev.karmakrafts.jbpl.assembler.util;

import dev.karmakrafts.jbpl.assembler.Scope;
import dev.karmakrafts.jbpl.assembler.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class NamedResolver<T, X extends Throwable> extends ScopeAwareElementVisitor {
    private final Class<T> type;
    private final XFunction<T, String, X> nameGetter;
    private final HashMap<Scope, HashMap<String, T>> elements = new HashMap<>();

    private NamedResolver(final @NotNull Class<T> type, final @NotNull XFunction<T, String, X> nameGetter) {
        this.type = type;
        this.nameGetter = nameGetter;
    }

    public static <T, X extends Throwable> @NotNull NamedResolver<T, X> analyze(final @NotNull AssemblyFile file,
                                                                                final @NotNull Class<T> type,
                                                                                final @NotNull XFunction<T, String, X> nameGetter) {
        final var analyzer = new NamedResolver<>(type, nameGetter);
        file.accept(analyzer);
        return analyzer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Element visitElement(final @NotNull Element element) {
        if (!type.isAssignableFrom(element.getClass())) {
            return super.visitElement(element);
        }
        final var scope = getScope();
        final var scopeMap = elements.computeIfAbsent(scope, s -> new HashMap<>());
        final var typedElement = (T) element;
        scopeMap.put(ExceptionUtils.propagateUnchecked(nameGetter).apply(typedElement), typedElement);
        return element;
    }

    public void inject(final @NotNull Scope scope, final @NotNull T value) throws X {
        elements.computeIfAbsent(scope, s -> new HashMap<>()).put(nameGetter.apply(value), value);
    }

    public @Nullable T resolve(final @NotNull Scope scope, final @NotNull String name) {
        return scope.find(currentScope -> {
            final var scopeMap = elements.get(currentScope);
            if (scopeMap == null) {
                return null;
            }
            return scopeMap.get(name);
        });
    }
}
