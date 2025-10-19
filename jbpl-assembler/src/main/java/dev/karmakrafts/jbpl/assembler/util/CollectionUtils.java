package dev.karmakrafts.jbpl.assembler.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T, C extends Collection<T>> @NotNull C intersect(final @NotNull C a,
                                                                    final @NotNull C b,
                                                                    final @NotNull Supplier<C> factory) {
        final var result = factory.get();
        for (final var valueA : a) {
            if (!b.contains(valueA)) {
                continue;
            }
            result.add(valueA);
        }
        return result;
    }
}
