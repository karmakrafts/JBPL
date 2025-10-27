package dev.karmakrafts.jbpl.assembler.util;

public record Pair<L, R>(L left, R right) implements Copyable<Pair<L, R>> {
    @Override
    public Pair<L, R> copy() {
        return new Pair<>(Copyable.copyIfPossible(left), Copyable.copyIfPossible(right));
    }
}
