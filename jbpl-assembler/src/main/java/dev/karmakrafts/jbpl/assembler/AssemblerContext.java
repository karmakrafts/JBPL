package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.model.util.Pair;
import dev.karmakrafts.jbpl.assembler.util.NamedResolver;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AssemblerContext {
    public final AssemblyFile file;
    public final NamedResolver<PreproClassDecl> preproClassResolver;
    public final NamedResolver<DefineStatement> defineResolver;
    public final NamedResolver<SelectorDecl> selectorResolver;
    public final NamedResolver<MacroDecl> macroResolver;
    private final Stack<StackFrame> frameStack = new Stack<>();

    public AssemblerContext(final @NotNull AssemblyFile file) {
        this.file = file;
        preproClassResolver = NamedResolver.analyze(file, PreproClassDecl.class, PreproClassDecl::getName);
        defineResolver = NamedResolver.analyze(file, DefineStatement.class, DefineStatement::getName);
        selectorResolver = NamedResolver.analyze(file, SelectorDecl.class, SelectorDecl::getName);
        macroResolver = NamedResolver.analyze(file, MacroDecl.class, MacroDecl::getName);
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
        frameStack.push(new StackFrame(new InsnList(), new HashMap<>()));
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
        return Map.of(); // TODO: implement this
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
     * @param locals            All locals defined in the current scope at this point in evaluation.
     */
    public record StackFrame(InsnList instructionBuffer, HashMap<String, LocalStatement> locals) {
    }
}
