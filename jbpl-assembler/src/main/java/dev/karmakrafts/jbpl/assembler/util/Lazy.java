package dev.karmakrafts.jbpl.assembler.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class Lazy<T> {
    private final Supplier<T> factory;
    private boolean isInitialized = false;
    private T value;

    public Lazy(final @NotNull Supplier<T> factory) {
        this.factory = factory;
    }

    public void reset() {
        value = null;
        isInitialized = false;
    }

    public void set(final T value) {
        this.value = value;
        isInitialized = true;
    }

    public T get() {
        if (!isInitialized) {
            value = factory.get();
            isInitialized = true;
        }
        return value;
    }
}
