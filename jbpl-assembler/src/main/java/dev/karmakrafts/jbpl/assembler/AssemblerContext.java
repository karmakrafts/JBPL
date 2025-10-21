package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.PreproClassDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LabelStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.NamedResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

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

    public @NotNull LabelNode getOrCreateLabelNode(final @NotNull String name) {
        return peekFrame().getOrCreateLabelNode(name);
    }

    public void removeClass(final @NotNull String name) {
        output.put(name, null);
    }

    public void addClass(final @NotNull ClassNode classNode) {
        output.put(classNode.name, classNode);
    }

    public void addField(final @NotNull String className, final @NotNull FieldNode fieldNode) {
        transformClass(className, node -> {
            node.fields.add(fieldNode);
            return node;
        });
    }

    public void addFunction(final @NotNull String className, final @NotNull MethodNode methodNode) {
        transformClass(className, node -> {
            node.methods.add(methodNode);
            return node;
        });
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
        final var lastFrame = frameStack.pop();
        // If the popped frames owner doesn't request frame data to be merged, we return early
        if (!lastFrame.scope.owner().mergeLocalFrameDataOnFrameExit()) {
            return;
        }
        // Otherwise we merge the instruction buffer; locals and labels are never merged
        final var lastInstructionBuffer = lastFrame.instructionBuffer;
        if (lastInstructionBuffer.size() == 0) {
            return;
        }
        final var currentFrame = frameStack.peek();
        currentFrame.instructionBuffer.add(lastInstructionBuffer);
    }

    public @NotNull Scope getScope() {
        return peekFrame().scope;
    }

    public @Nullable ScopeOwner getReturnTarget() {
        if (frameStack.isEmpty()) {
            return null;
        }
        return peekFrame().scope.owner();
    }

    public @NotNull Expr getReturnValue() {
        final var value = peekFrame().returnValue;
        return value != null ? value : LiteralExpr.unit();
    }

    public void setReturnValue(final @NotNull Expr returnValue) {
        peekFrame().returnValue = returnValue;
    }

    public @NotNull InsnList getInstructionBuffer() {
        return peekFrame().instructionBuffer;
    }

    public final class StackFrame {
        public final Scope scope;
        public final InsnList instructionBuffer = new InsnList();
        public final HashMap<String, LocalStatement> locals = new HashMap<>();
        private final HashMap<String, LabelStatement> labels = new HashMap<>();
        private final HashMap<String, LabelNode> labelNodes = new HashMap<>();
        public Expr returnValue;

        public StackFrame(final @NotNull Scope scope) {
            this.scope = scope;
        }

        public @Nullable LabelStatement findLabel(final @NotNull String name) {
            return labels.get(name);
        }

        public @NotNull LabelNode getOrCreateLabelNode(final @NotNull String name) {
            return labelNodes.computeIfAbsent(name, n -> new LabelNode());
        }

        public void addLabel(final @NotNull LabelStatement statement) {
            final var name = statement.getName().evaluateAsConst(AssemblerContext.this, String.class);
            labels.put(name, statement);
            labelNodes.put(name, new LabelNode());
        }
    }
}
