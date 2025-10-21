package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.ReturnTarget;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.util.NamedResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.HashMap;
import java.util.Stack;

public final class AssemblerContext {
    public final AssemblyFile file;
    public final NamedResolver<PreproClassDecl> preproClassResolver;
    public final NamedResolver<DefineStatement> defineResolver;
    public final NamedResolver<SelectorDecl> selectorResolver;
    public final NamedResolver<MacroDecl> macroResolver;
    private final Stack<StackFrame> frameStack = new Stack<>();
    public int bytecodeVersion = Opcodes.ASM9; // Default is ASM 9.2 for Java 17

    public AssemblerContext(final @NotNull AssemblyFile file) {
        this.file = file;
        pushFrame(file); // We always require a root frame for the file itself
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

    public void pushFrame(final @NotNull ScopeOwner owner) {
        final var parentScope = frameStack.empty() ? null : frameStack.peek().scope;
        frameStack.push(new StackFrame(new Scope(parentScope, owner)));
    }

    public void popFrame() {
        // Merge the instruction buffer of the popped frame with the current frames instruction buffer
        final var lastFrame = frameStack.pop();
        final var lastInstructionBuffer = lastFrame.instructionBuffer;
        if (lastInstructionBuffer.size() == 0) {
            return;
        }
        final var currentFrame = frameStack.peek();
        currentFrame.instructionBuffer.add(lastInstructionBuffer);
    }

    public void pushValue(final @NotNull Expr value) {
        peekFrame().values.push(value);
    }

    public @NotNull Expr popValue() {
        return peekFrame().values.pop();
    }

    public @Nullable Expr peekValue() {
        final var stack = peekFrame().values;
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    public @NotNull Scope getScope() {
        return peekFrame().scope;
    }

    public void setReturnTarget() {
        final var scopeOwner = getScope().owner();
        if (!(scopeOwner instanceof ReturnTarget returnTarget)) {
            throw new IllegalStateException("Current scope owner must be a valid return target");
        }
        setReturnTarget(returnTarget);
    }

    public @Nullable ReturnTarget getReturnTarget() {
        if (frameStack.isEmpty()) {
            return null;
        }
        return peekFrame().returnTarget;
    }

    public void setReturnTarget(final @NotNull ReturnTarget returnTarget) {
        peekFrame().returnTarget = returnTarget;
    }

    public @NotNull InsnList getInstructionBuffer() {
        return peekFrame().instructionBuffer;
    }

    public static final class StackFrame {
        public final Scope scope;
        public final Stack<Expr> values = new Stack<>();
        public final InsnList instructionBuffer = new InsnList();
        public final HashMap<String, LocalStatement> locals = new HashMap<>();
        public ReturnTarget returnTarget;

        public StackFrame(final @NotNull Scope scope) {
            this.scope = scope;
        }
    }
}
