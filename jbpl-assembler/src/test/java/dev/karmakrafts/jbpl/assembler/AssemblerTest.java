package dev.karmakrafts.jbpl.assembler;

import org.junit.jupiter.api.Test;

public class AssemblerTest {
    @Test
    public void loadEmpty() {
        final var assembler = Assembler.createFromResources("");
        final var file = assembler.getOrParseAndLowerFile("test0.jbpl").file;
    }

    @Test
    public void loadFromResource() {
        final var assembler = Assembler.createFromResources("");
        final var file = assembler.getOrParseAndLowerFile("test1.jbpl").file;
        System.out.println(file.getElements());
    }

    @Test
    public void loadFromResourcesRecursively() {
        final var assembler = Assembler.createFromResources("");
        // This should also pull in the contents of test2.jbpl
        final var file = assembler.getOrParseAndLowerFile("test3.jbpl").file;
        System.out.println(file.getElements());
    }
}
