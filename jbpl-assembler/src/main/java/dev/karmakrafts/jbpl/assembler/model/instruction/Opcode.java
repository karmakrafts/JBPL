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

package dev.karmakrafts.jbpl.assembler.model.instruction;

import dev.karmakrafts.jbpl.assembler.util.CollectionUtils;
import org.objectweb.asm.Opcodes;

import java.util.EnumSet;
import java.util.Set;

public enum Opcode {
    // @formatter:off
    NOP             (Opcodes.NOP),
    LDC             (Opcodes.LDC),
    SIPUSH          (Opcodes.SIPUSH),
    BIPUSH          (Opcodes.BIPUSH),
    ICONST_M1       (Opcodes.ICONST_M1),
    ICONST_0        (Opcodes.ICONST_0),
    ICONST_1        (Opcodes.ICONST_1),
    ICONST_2        (Opcodes.ICONST_2),
    ICONST_3        (Opcodes.ICONST_3),
    ICONST_4        (Opcodes.ICONST_4),
    ICONST_5        (Opcodes.ICONST_5),
    LCONST_0        (Opcodes.LCONST_0),
    LCONST_1        (Opcodes.LCONST_1),
    FCONST_0        (Opcodes.FCONST_0),
    FCONST_1        (Opcodes.FCONST_1),
    FCONST_2        (Opcodes.FCONST_2),
    DCONST_0        (Opcodes.DCONST_0),
    DCONST_1        (Opcodes.DCONST_1),
    ILOAD           (Opcodes.ILOAD),
    LLOAD           (Opcodes.LLOAD),
    FLOAD           (Opcodes.FLOAD),
    DLOAD           (Opcodes.DLOAD),
    ALOAD           (Opcodes.ALOAD),
    BALOAD          (Opcodes.BALOAD),
    SALOAD          (Opcodes.SALOAD),
    IALOAD          (Opcodes.IALOAD),
    LALOAD          (Opcodes.LALOAD),
    FALOAD          (Opcodes.FALOAD),
    DALOAD          (Opcodes.DALOAD),
    CALOAD          (Opcodes.CALOAD),
    AALOAD          (Opcodes.AALOAD),
    ISTORE          (Opcodes.ISTORE),
    LSTORE          (Opcodes.LSTORE),
    FSTORE          (Opcodes.FSTORE),
    DSTORE          (Opcodes.DSTORE),
    ASTORE          (Opcodes.ASTORE),
    BASTORE         (Opcodes.BASTORE),
    SASTORE         (Opcodes.SASTORE),
    IASTORE         (Opcodes.IASTORE),
    LASTORE         (Opcodes.LASTORE),
    FASTORE         (Opcodes.FASTORE),
    DASTORE         (Opcodes.DASTORE),
    CASTORE         (Opcodes.CASTORE),
    AASTORE         (Opcodes.AASTORE),
    PUTFIELD        (Opcodes.PUTFIELD),
    PUTSTATIC       (Opcodes.PUTSTATIC),
    GETFIELD        (Opcodes.GETFIELD),
    GETSTATIC       (Opcodes.GETSTATIC),
    RETURN          (Opcodes.RETURN),
    IRETURN         (Opcodes.IRETURN),
    LRETURN         (Opcodes.LRETURN),
    FRETURN         (Opcodes.FRETURN),
    DRETURN         (Opcodes.DRETURN),
    ARETURN         (Opcodes.ARETURN),
    INVOKESTATIC    (Opcodes.INVOKESTATIC),
    INVOKEVIRTUAL   (Opcodes.INVOKEVIRTUAL),
    INVOKESPECIAL   (Opcodes.INVOKESPECIAL),
    INVOKEINTERFACE (Opcodes.INVOKEINTERFACE),
    INVOKEDYNAMIC   (Opcodes.INVOKEDYNAMIC),
    IFEQ            (Opcodes.IFEQ),
    IFNE            (Opcodes.IFNE),
    IFLT            (Opcodes.IFLT),
    IFGE            (Opcodes.IFGE),
    IFGT            (Opcodes.IFGT),
    IFLE            (Opcodes.IFLE),
    IF_ICMPEQ       (Opcodes.IF_ICMPEQ),
    IF_ICMPNE       (Opcodes.IF_ICMPNE),
    IF_ICMPLT       (Opcodes.IF_ICMPLT),
    IF_ICMPGE       (Opcodes.IF_ICMPGE),
    IF_ICMPGT       (Opcodes.IF_ICMPGT),
    IF_ICMPLE       (Opcodes.IF_ICMPLE),
    IF_ACMPEQ       (Opcodes.IF_ACMPEQ),
    IF_ACMPNE       (Opcodes.IF_ACMPNE),
    GOTO            (Opcodes.GOTO);
    // @formatter:on

    // @formatter:off
    public static final EnumSet<Opcode> STACK_LOAD = EnumSet.of(ILOAD, LLOAD, FLOAD, DLOAD, ALOAD);
    public static final EnumSet<Opcode> STACK_STORE = EnumSet.of(ISTORE, LSTORE, FSTORE, DSTORE, ASTORE);
    public static final EnumSet<Opcode> ARRAY_LOAD = EnumSet.of(BALOAD, SALOAD, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD);
    public static final EnumSet<Opcode> ARRAY_STORE = EnumSet.of(BASTORE, SASTORE, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE);
    public static final EnumSet<Opcode> FIELD_LOAD = EnumSet.of(GETFIELD, GETSTATIC);
    public static final EnumSet<Opcode> FIELD_STORE = EnumSet.of(PUTFIELD, PUTSTATIC);
    public static final EnumSet<Opcode> CONSTANT = EnumSet.of(LDC, BIPUSH, SIPUSH);
    public static final EnumSet<Opcode> INVOKE = EnumSet.of(INVOKESTATIC, INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKEDYNAMIC);
    public static final EnumSet<Opcode> IF = EnumSet.of(IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE);
    public static final EnumSet<Opcode> IF_ICMP = EnumSet.of(IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE);
    public static final EnumSet<Opcode> IF_ACMP = EnumSet.of(IF_ACMPEQ, IF_ACMPNE);
    public static final EnumSet<Opcode> JUMP = CollectionUtils.enumSetOf(Opcode.class, IF, IF_ICMP, IF_ACMP, Set.of(Opcode.GOTO));
    // @formatter:on

    public final int encodedValue;

    Opcode(final int encodedValue) {
        this.encodedValue = encodedValue;
    }

    public boolean isConstant() {
        return CONSTANT.contains(this);
    }

    public boolean isFieldInstruction() {
        return FIELD_LOAD.contains(this) || FIELD_STORE.contains(this);
    }

    public boolean isStackInstruction() {
        return STACK_LOAD.contains(this) || STACK_STORE.contains(this);
    }

    public boolean isArrayInstruction() {
        return ARRAY_LOAD.contains(this) || ARRAY_STORE.contains(this);
    }

    public boolean isLoad() {
        return STACK_LOAD.contains(this) || ARRAY_LOAD.contains(this) || FIELD_LOAD.contains(this);
    }

    public boolean isStore() {
        return STACK_STORE.contains(this) || ARRAY_STORE.contains(this) || FIELD_STORE.contains(this);
    }

    public boolean isInvoke() {
        return INVOKE.contains(this);
    }

    public boolean isJump() {
        return JUMP.contains(this);
    }
}
