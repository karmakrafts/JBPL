package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.ReturnTarget;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LabelStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.NamedResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.function.Function;

public final class AssemblerContext {
    public final AssemblyFile file;
    public final Function<String, ClassNode> classResolver;
    public final NamedResolver<PreproClassDecl> preproClassResolver;
    public final NamedResolver<DefineStatement> defineResolver;
    public final NamedResolver<SelectorDecl> selectorResolver;
    public final NamedResolver<MacroDecl> macroResolver;
    public final HashMap<String, @Nullable ClassNode> output = new HashMap<>();
    private final Stack<StackFrame> frameStack = new Stack<>();
    public int bytecodeVersion = Opcodes.ASM9; // Default is ASM 9.2 for Java 17

    public AssemblerContext(final @NotNull AssemblyFile file,
                            final @NotNull Function<String, ClassNode> classResolver) {
        this.file = file;
        this.classResolver = classResolver;
        pushFrame(file); // We always require a root frame for the file itself
        preproClassResolver = NamedResolver.analyze(file, PreproClassDecl.class, PreproClassDecl::getName);
        defineResolver = NamedResolver.analyze(file, DefineStatement.class, DefineStatement::getName);
        selectorResolver = NamedResolver.analyze(file, SelectorDecl.class, SelectorDecl::getName);
        macroResolver = NamedResolver.analyze(file, MacroDecl.class, MacroDecl::getName);
    }

    public void removeClass(final @NotNull String name) {
        output.put(name, null);
    }

    public void addClass(final @NotNull ClassNode classNode) {
        output.put(classNode.name, classNode);
    }

    public void transformClass(final @NotNull String name, final @NotNull Function<ClassNode, ClassNode> transform) {
        final var clazz = output.get(name);
        if (clazz != null) {
            output.put(name, transform.apply(clazz));
            return;
        }
        output.put(name, transform.apply(classResolver.apply(name)));
    }

    public void removeField(final @NotNull String className, final @NotNull String name) {
        transformClass(className, node -> {
            // @formatter:off
            final var target = node.fields.stream()
                .filter(field -> field.name.equals(name))
                .findFirst()
                .orElseThrow();
            // @formatter:on
            node.fields.remove(target);
            return node;
        });
    }

    public void removeFunction(final @NotNull String className,
                               final @NotNull String name,
                               final @NotNull org.objectweb.asm.Type type) {
        transformClass(className, node -> {
            // @formatter:off
            final var target = node.methods.stream()
                .filter(method -> org.objectweb.asm.Type.getMethodType(method.desc).equals(type))
                .findFirst()
                .orElseThrow();
            // @formatter:on
            node.methods.remove(target);
            return node;
        });
    }

    public void removeFunction(final @NotNull String className,
                               final @NotNull String name,
                               final @NotNull Type returnType,
                               final @NotNull Type... paramTypes) {
        final var mReturnType = returnType.materialize(this);
        final var mParamTypes = Arrays.stream(paramTypes).map(type -> type.materialize(this)).toArray(org.objectweb.asm.Type[]::new);
        removeFunction(className, name, org.objectweb.asm.Type.getMethodType(mReturnType, mParamTypes));
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
        public final HashMap<String, LabelStatement> labels = new HashMap<>();
        public ReturnTarget returnTarget;

        public StackFrame(final @NotNull Scope scope) {
            this.scope = scope;
        }
    }
}
