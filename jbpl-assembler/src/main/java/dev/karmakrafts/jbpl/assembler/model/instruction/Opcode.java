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
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.util.*;

public enum Opcode {
    // @formatter:off
    // InsnNode (no operand)
    AALOAD          (Opcodes.AALOAD),
    AASTORE         (Opcodes.AASTORE),
    ACONST_NULL     (Opcodes.ACONST_NULL),
    ARETURN         (Opcodes.ARETURN),
    ARRAYLENGTH     (Opcodes.ARRAYLENGTH),
    ATHROW          (Opcodes.ATHROW),
    BALOAD          (Opcodes.BALOAD),
    BASTORE         (Opcodes.BASTORE),
    CALOAD          (Opcodes.CALOAD),
    CASTORE         (Opcodes.CASTORE),
    D2F             (Opcodes.D2F),
    D2I             (Opcodes.D2I),
    D2L             (Opcodes.D2L),
    DADD            (Opcodes.DADD),
    DALOAD          (Opcodes.DALOAD),
    DASTORE         (Opcodes.DASTORE),
    DCMPG           (Opcodes.DCMPG),
    DCMPL           (Opcodes.DCMPL),
    DCONST_0        (Opcodes.DCONST_0),
    DCONST_1        (Opcodes.DCONST_1),
    DDIV            (Opcodes.DDIV),
    DMUL            (Opcodes.DMUL),
    DNEG            (Opcodes.DNEG),
    DREM            (Opcodes.DREM),
    DRETURN         (Opcodes.DRETURN),
    DSUB            (Opcodes.DSUB),
    DUP             (Opcodes.DUP),
    DUP2            (Opcodes.DUP2),
    DUP2_X1         (Opcodes.DUP2_X1),
    DUP2_X2         (Opcodes.DUP2_X2),
    DUP_X1          (Opcodes.DUP_X1),
    DUP_X2          (Opcodes.DUP_X2),
    F2D             (Opcodes.F2D),
    F2I             (Opcodes.F2I),
    F2L             (Opcodes.F2L),
    FADD            (Opcodes.FADD),
    FALOAD          (Opcodes.FALOAD),
    FASTORE         (Opcodes.FASTORE),
    FCMPG           (Opcodes.FCMPG),
    FCMPL           (Opcodes.FCMPL),
    FCONST_0        (Opcodes.FCONST_0),
    FCONST_1        (Opcodes.FCONST_1),
    FCONST_2        (Opcodes.FCONST_2),
    FDIV            (Opcodes.FDIV),
    FMUL            (Opcodes.FMUL),
    FNEG            (Opcodes.FNEG),
    FREM            (Opcodes.FREM),
    FRETURN         (Opcodes.FRETURN),
    FSUB            (Opcodes.FSUB),
    I2B             (Opcodes.I2B),
    I2C             (Opcodes.I2C),
    I2D             (Opcodes.I2D),
    I2F             (Opcodes.I2F),
    I2L             (Opcodes.I2L),
    I2S             (Opcodes.I2S),
    IADD            (Opcodes.IADD),
    IALOAD          (Opcodes.IALOAD),
    IAND            (Opcodes.IAND),
    IASTORE         (Opcodes.IASTORE),
    ICONST_0        (Opcodes.ICONST_0),
    ICONST_1        (Opcodes.ICONST_1),
    ICONST_2        (Opcodes.ICONST_2),
    ICONST_3        (Opcodes.ICONST_3),
    ICONST_4        (Opcodes.ICONST_4),
    ICONST_5        (Opcodes.ICONST_5),
    ICONST_M1       (Opcodes.ICONST_M1),
    IDIV            (Opcodes.IDIV),
    IMUL            (Opcodes.IMUL),
    INEG            (Opcodes.INEG),
    IOR             (Opcodes.IOR),
    IREM            (Opcodes.IREM),
    IRETURN         (Opcodes.IRETURN),
    ISHL            (Opcodes.ISHL),
    ISHR            (Opcodes.ISHR),
    ISUB            (Opcodes.ISUB),
    IUSHR           (Opcodes.IUSHR),
    IXOR            (Opcodes.IXOR),
    L2D             (Opcodes.L2D),
    L2F             (Opcodes.L2F),
    L2I             (Opcodes.L2I),
    LADD            (Opcodes.LADD),
    LALOAD          (Opcodes.LALOAD),
    LAND            (Opcodes.LAND),
    LASTORE         (Opcodes.LASTORE),
    LCMP            (Opcodes.LCMP),
    LCONST_0        (Opcodes.LCONST_0),
    LCONST_1        (Opcodes.LCONST_1),
    LDIV            (Opcodes.LDIV),
    LMUL            (Opcodes.LMUL),
    LNEG            (Opcodes.LNEG),
    LOR             (Opcodes.LOR),
    LREM            (Opcodes.LREM),
    LRETURN         (Opcodes.LRETURN),
    LSHL            (Opcodes.LSHL),
    LSHR            (Opcodes.LSHR),
    LSUB            (Opcodes.LSUB),
    LUSHR           (Opcodes.LUSHR),
    LXOR            (Opcodes.LXOR),
    MONITORENTER    (Opcodes.MONITORENTER),
    MONITOREXIT     (Opcodes.MONITOREXIT),
    NOP             (Opcodes.NOP),
    POP             (Opcodes.POP),
    POP2            (Opcodes.POP2),
    RETURN          (Opcodes.RETURN),
    SALOAD          (Opcodes.SALOAD),
    SASTORE         (Opcodes.SASTORE),
    SWAP            (Opcodes.SWAP),
    // IntInsnNode
    BIPUSH          (Opcodes.BIPUSH),
    NEWARRAY        (Opcodes.NEWARRAY),
    SIPUSH          (Opcodes.SIPUSH),
    // VarInsnNode
    ALOAD           (Opcodes.ALOAD),
    ASTORE          (Opcodes.ASTORE),
    DLOAD           (Opcodes.DLOAD),
    DSTORE          (Opcodes.DSTORE),
    FLOAD           (Opcodes.FLOAD),
    FSTORE          (Opcodes.FSTORE),
    ILOAD           (Opcodes.ILOAD),
    ISTORE          (Opcodes.ISTORE),
    LLOAD           (Opcodes.LLOAD),
    LSTORE          (Opcodes.LSTORE),
    RET             (Opcodes.RET),
    // IincInsnNode
    IINC            (Opcodes.IINC),
    // TypeInsnNode
    ANEWARRAY       (Opcodes.ANEWARRAY),
    CHECKCAST       (Opcodes.CHECKCAST),
    INSTANCEOF      (Opcodes.INSTANCEOF),
    NEW             (Opcodes.NEW),
    // FieldInsnNode
    GETFIELD        (Opcodes.GETFIELD),
    GETSTATIC       (Opcodes.GETSTATIC),
    PUTFIELD        (Opcodes.PUTFIELD),
    PUTSTATIC       (Opcodes.PUTSTATIC),
    // MethodInsnNode
    INVOKEINTERFACE (Opcodes.INVOKEINTERFACE),
    INVOKESPECIAL   (Opcodes.INVOKESPECIAL),
    INVOKESTATIC    (Opcodes.INVOKESTATIC),
    INVOKEVIRTUAL   (Opcodes.INVOKEVIRTUAL),
    // InvokeDynamicInsnNode
    INVOKEDYNAMIC   (Opcodes.INVOKEDYNAMIC),
    // JumpInsnNode
    GOTO            (Opcodes.GOTO),
    IFEQ            (Opcodes.IFEQ),
    IFGE            (Opcodes.IFGE),
    IFGT            (Opcodes.IFGT),
    IFLE            (Opcodes.IFLE),
    IFLT            (Opcodes.IFLT),
    IFNE            (Opcodes.IFNE),
    IFNONNULL       (Opcodes.IFNONNULL),
    IFNULL          (Opcodes.IFNULL),
    IF_ACMPEQ       (Opcodes.IF_ACMPEQ),
    IF_ACMPNE       (Opcodes.IF_ACMPNE),
    IF_ICMPEQ       (Opcodes.IF_ICMPEQ),
    IF_ICMPNE       (Opcodes.IF_ICMPNE),
    IF_ICMPGE       (Opcodes.IF_ICMPGE),
    IF_ICMPGT       (Opcodes.IF_ICMPGT),
    IF_ICMPLE       (Opcodes.IF_ICMPLE),
    IF_ICMPLT       (Opcodes.IF_ICMPLT),
    JSR             (Opcodes.JSR),
    // LdcInsnNode
    LDC             (Opcodes.LDC),
    // TableSwitchInsnNode
    TABLESWITCH     (Opcodes.TABLESWITCH),
    // LookupSwitchInsnNode
    LOOKUPSWITCH    (Opcodes.LOOKUPSWITCH),
    // MultiANewArrayInsnNode
    MULTIANEWARRAY  (Opcodes.MULTIANEWARRAY);
    // @formatter:on

    // @formatter:off
    public static final EnumSet<Opcode> STACK_LOAD = EnumSet.of(ILOAD, LLOAD, FLOAD, DLOAD, ALOAD);
    public static final EnumSet<Opcode> STACK_STORE = EnumSet.of(ISTORE, LSTORE, FSTORE, DSTORE, ASTORE);
    public static final EnumSet<Opcode> ARRAY_LOAD = EnumSet.of(BALOAD, CALOAD, SALOAD, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD);
    public static final EnumSet<Opcode> ARRAY_STORE = EnumSet.of(BASTORE, CASTORE, SASTORE, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE);
    public static final EnumSet<Opcode> FIELD_LOAD = EnumSet.of(GETFIELD, GETSTATIC);
    public static final EnumSet<Opcode> FIELD_STORE = EnumSet.of(PUTFIELD, PUTSTATIC);
    public static final EnumSet<Opcode> CONSTANT = EnumSet.of(LDC, BIPUSH, SIPUSH);
    public static final EnumSet<Opcode> INVOKE = EnumSet.of(INVOKESTATIC, INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKEDYNAMIC);
    public static final EnumSet<Opcode> IF = EnumSet.of(IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE);
    public static final EnumSet<Opcode> IF_ICMP = EnumSet.of(IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE);
    public static final EnumSet<Opcode> IF_ACMP = EnumSet.of(IF_ACMPEQ, IF_ACMPNE);
    public static final EnumSet<Opcode> JUMP = CollectionUtils.enumSetOf(Opcode.class, IF, IF_ICMP, IF_ACMP, Set.of(Opcode.GOTO, Opcode.IFNULL, Opcode.IFNONNULL, Opcode.JSR));
    public static final EnumSet<Opcode> I_CONV = EnumSet.of(I2B, I2S, I2C, I2L, I2F, I2D);
    public static final EnumSet<Opcode> L_CONV = EnumSet.of(L2I, L2F, L2D);
    public static final EnumSet<Opcode> F_CONV = EnumSet.of(F2I, F2L, F2D);
    public static final EnumSet<Opcode> D_CONV = EnumSet.of(D2I, D2L, D2F);
    public static final EnumSet<Opcode> CONV = CollectionUtils.enumSetOf(Opcode.class, I_CONV, L_CONV, F_CONV, D_CONV);
    // @formatter:on

    public final int encodedValue;

    Opcode(final int encodedValue) {
        this.encodedValue = encodedValue;
    }

    public static @NotNull Optional<Opcode> findByName(final @NotNull String name) {
        return Arrays.stream(values()).filter(op -> op.name().equalsIgnoreCase(name)).findFirst();
    }

    public static @NotNull Optional<Opcode> findByEncodedValue(final int value) {
        return Arrays.stream(values()).filter(op -> op.encodedValue == value).findFirst();
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

    @Override
    public @NotNull String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
