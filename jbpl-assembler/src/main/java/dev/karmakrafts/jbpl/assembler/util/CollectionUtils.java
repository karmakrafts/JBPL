package dev.karmakrafts.jbpl.assembler.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> int indexOf(final @NotNull List<T> elements, final @NotNull Predicate<T> predicate) {
        for (var i = 0; i < elements.size(); i++) {
            if (!predicate.test(elements.get(i))) {
                continue;
            }
            return i;
        }
        return -1;
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

    @SafeVarargs
    public static <E extends Enum<E>> @NotNull EnumSet<E> enumSetOf(final @NotNull Class<E> type,
                                                                    final @NotNull Collection<E>... values) {
        final var set = EnumSet.noneOf(type);
        for (final var slice : values) {
            set.addAll(slice);
        }
        return set;
    }
}
