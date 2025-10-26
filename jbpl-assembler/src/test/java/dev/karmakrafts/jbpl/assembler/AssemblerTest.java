package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

public final class AssemblerTest {
    @Test
    public void loadEmpty() {
        final var assembler = Assembler.createFromResources("");
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test0.jbpl",
            n -> new ClassNode()));
        try {
            context.file.evaluate(context);
        }
        catch (Throwable error) {
            Assertions.fail(error);
        }
    }

    @Test
    public void loadFromResource() {
        final var assembler = Assembler.createFromResources("");
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test1.jbpl",
            n -> new ClassNode()));
        try {
            context.file.evaluate(context);
        }
        catch (Throwable error) {
            Assertions.fail(error);
        }
    }

    @Test
    public void loadFromResourcesRecursively() {
        final var assembler = Assembler.createFromResources("");
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test3.jbpl",
            n -> new ClassNode()));
        try {
            context.file.evaluate(context);
        }
        catch (Throwable error) {
            Assertions.fail(error);
        }
    }
}