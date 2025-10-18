package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.util.Pair;
import dev.karmakrafts.jbpl.assembler.resolver.DefineResolver;
import dev.karmakrafts.jbpl.assembler.resolver.TypeResolver;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AssemblerContext {
    public final AssemblyFile file;
    public final TypeResolver typeResolver;
    public final DefineResolver defineResolver;
    private final Stack<StackFrame> frameStack = new Stack<>();
    public AssemblerContext(final @NotNull AssemblyFile file) {
        this.file = file;
        typeResolver = TypeResolver.analyze(file);
        defineResolver = DefineResolver.analyze(file);
    }

    public void emit(final AbstractInsnNode instruction) {
        peekFrame().instructionBuffer.add(instruction);
    }

    public void emitAll(final InsnList instructions) {
        peekFrame().instructionBuffer.add(instructions);
    }

    public @NotNull StackFrame peekFrame() {
        return frameStack.peek();
    }

    public void pushFrame() {
        frameStack.push(new StackFrame(new InsnList()));
    }

    public void popFrame() {
        final var lastFrame = frameStack.pop();
        final var lastInstructionBuffer = lastFrame.instructionBuffer;
        if (lastInstructionBuffer.size() == 0) {
            return;
        }
        final var currentFrame = frameStack.peek();
        currentFrame.instructionBuffer.add(lastInstructionBuffer);
    }

    public @NotNull InsnList getInstructionBuffer() {
        return peekFrame().instructionBuffer;
    }

    public @NotNull Map<String, ClassNode> apply(final @NotNull Function<String, ClassNode> classProvider) {

    }

    public @NotNull Map<String, ClassNode> apply(final @NotNull Map<String, ClassNode> classes) {
        return apply(classes::get);
    }

    public @NotNull Map<String, ClassNode> apply(final @NotNull Collection<ClassNode> classes) { // @formatter:off
        return apply(classes.stream()
            .map(node -> new Pair<>(node.name, node))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
    } // @formatter:on

    public @NotNull Map<String, ClassNode> apply(final @NotNull ClassNode clazz) {
        return apply(Map.of(clazz.name, clazz));
    }

    /**
     * Holds the expression (evaluation) stack for the current scope,
     * as well as the instruction buffer of the current scope,
     * which is joined to the end of the parent scope when the frame is popped.
     *
     * @param instructionBuffer The instruction buffer of the current scope.
     */
    public record StackFrame(InsnList instructionBuffer) {
    }
}
