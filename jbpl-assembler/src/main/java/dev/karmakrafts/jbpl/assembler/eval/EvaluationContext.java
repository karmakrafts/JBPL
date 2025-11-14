/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.eval;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EvaluationContext {
    // @formatter:off
    public static final byte RET_MASK_NONE      = 0b0000_0000;
    public static final byte RET_MASK_RETURN    = 0b0000_0001;
    public static final byte RET_MASK_CONTINUE  = 0b0000_0010;
    public static final byte RET_MASK_BREAK     = 0b0000_0100;
    // @formatter:on

    public final AssemblyFile file;
    public final Function<String, ClassNode> classResolver;
    public final Consumer<String> infoConsumer;
    public final Consumer<String> errorConsumer;
    public final HashMap<String, @Nullable ClassNode> output = new HashMap<>();
    private final Stack<StackFrame> frameStack = new Stack<>();
    public int bytecodeVersion = Opcodes.V17;
    public int bytecodeApi = Opcodes.ASM9;
    private byte returnMask = RET_MASK_NONE;

    public EvaluationContext(final @NotNull AssemblyFile file,
                             final @NotNull Function<String, ClassNode> classResolver,
                             final @NotNull Consumer<String> infoConsumer,
                             final @NotNull Consumer<String> errorConsumer) {
        this.file = file;
        this.classResolver = classResolver;
        this.infoConsumer = infoConsumer;
        this.errorConsumer = errorConsumer;
    }

    public @NotNull StackTrace createStackTrace() {
        return new StackTrace(frameStack.stream().map(StackFrame::copy).toList());
    }

    public <E extends NamedElement> @Nullable E resolveByName(final @NotNull Class<E> type,
                                                              final @NotNull String name,
                                                              final @NotNull Predicate<E> filter) {
        return peekFrame().scopeResolver.resolve(type,
            filter.and(ExceptionUtils.unsafePredicate(element -> element.getName(this).equals(name))));
    }

    public <E extends NamedElement> @Nullable E resolveByName(final @NotNull Class<E> type,
                                                              final @NotNull String name) {
        return resolveByName(type, name, element -> true);
    }

    public <E extends NamedElement> @NotNull List<E> resolveAllByName(final @NotNull Class<E> type,
                                                                      final @NotNull String name,
                                                                      final @NotNull Predicate<E> filter) {
        return peekFrame().scopeResolver.resolveAll(type,
            filter.and(ExceptionUtils.unsafePredicate(element -> element.getName(this).equals(name))));
    }

    public <E extends NamedElement> @NotNull List<E> resolveAllByName(final @NotNull Class<E> type,
                                                                      final @NotNull String name) {
        return resolveAllByName(type, name, element -> true);
    }

    public void clearStack() {
        peekFrame().valueStack.clear();
    }

    public byte getReturnMask() {
        return returnMask;
    }

    public boolean clearReturnMask() {
        final var result = hasRet();
        returnMask = RET_MASK_NONE;
        return result;
    }

    public boolean hasCnt() {
        return (returnMask & RET_MASK_CONTINUE) != 0;
    }

    public boolean clearCnt() {
        final var result = returnMask;
        returnMask &= ~RET_MASK_CONTINUE;
        return (result & RET_MASK_CONTINUE) != 0;
    }

    public void cnt() {
        returnMask |= RET_MASK_CONTINUE;
    }

    public boolean hasBrk() {
        return (returnMask & RET_MASK_BREAK) != 0;
    }

    public boolean clearBrk() {
        final var result = returnMask;
        returnMask &= ~RET_MASK_BREAK;
        return (result & RET_MASK_BREAK) != 0;
    }

    public void brk() {
        returnMask |= RET_MASK_BREAK;
    }

    public boolean hasRet() {
        return (returnMask & RET_MASK_RETURN) != 0;
    }

    public boolean clearRet() {
        final var result = returnMask;
        returnMask &= ~RET_MASK_RETURN;
        return (result & RET_MASK_RETURN) != 0;
    }

    public void ret() {
        returnMask |= RET_MASK_RETURN;
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
                .filter(method -> method.name.equals(name) && org.objectweb.asm.Type.getMethodType(method.desc).equals(type))
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
                               final @NotNull Type... paramTypes) throws EvaluationException {
        final var mReturnType = returnType.materialize(this);
        // @formatter:off
        final var mParamTypes = Arrays.stream(paramTypes)
            .map(ExceptionUtils.unsafeFunction(type -> type.materialize(this)))
            .toArray(org.objectweb.asm.Type[]::new);
        // @formatter:on
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
        final var newFrame = new StackFrame(new Scope(parentScope, owner));
        if (!frameStack.empty()) {
            // If we currently already have a frame, propagate named values scope-inwards
            newFrame.namedLocalValues.putAll(peekFrame().namedLocalValues);
        }
        frameStack.push(newFrame);
    }

    public void popFrame() {
        final var lastFrame = frameStack.pop();
        // If the popped frames owner doesn't request frame data to be merged, we return early
        if (!lastFrame.scope.owner().mergeFrameDataOnFrameExit()) {
            return;
        }
        // Otherwise we merge the value stack and instruction buffer; locals and labels are never merged
        final var currentFrame = frameStack.peek();
        currentFrame.instructionBuffer.add(lastFrame.instructionBuffer);
        currentFrame.valueStack.addAll(lastFrame.valueStack);
    }

    public @NotNull Scope getScope() {
        return peekFrame().scope;
    }

    public void pushValue(final @NotNull Expr value) {
        peekFrame().valueStack.push(value);
    }

    public void pushValues(final @NotNull Collection<Expr> values) {
        for (final var value : values) {
            pushValue(value);
        }
    }

    public @NotNull Expr popValue() {
        return peekFrame().valueStack.pop();
    }

    public @NotNull List<Expr> popValues(final int count) {
        final var values = new ArrayList<Expr>(count);
        for (var i = 0; i < count; ++i) {
            values.add(popValue());
        }
        Collections.reverse(values);
        return values;
    }

    public @NotNull Expr peekValue() {
        return peekFrame().valueStack.peek();
    }

    public @NotNull InsnList getInstructionBuffer() {
        return peekFrame().instructionBuffer;
    }

    public @NotNull InsnList copyInstructionBuffer() {
        final var list = new InsnList();
        list.add(getInstructionBuffer());
        return list;
    }
}
