package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

public class AssemblerTest {
    @Test
    public void loadEmpty() {
        final var assembler = Assembler.createFromResources("");
        final var file = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test0.jbpl",
            n -> new ClassNode()).file);
    }

    @Test
    public void loadFromResource() {
        final var assembler = Assembler.createFromResources("");
        final var file = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test1.jbpl",
            n -> new ClassNode()).file);
        System.out.println(file.getElements());
    }

    @Test
    public void loadFromResourcesRecursively() {
        final var assembler = Assembler.createFromResources("");
        // This should also pull in the contents of test2.jbpl
        final var file = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test3.jbpl",
            n -> new ClassNode()).file);
        System.out.println(file.getElements());
    }
}
