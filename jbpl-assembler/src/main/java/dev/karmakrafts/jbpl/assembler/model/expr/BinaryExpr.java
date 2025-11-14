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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.*;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class BinaryExpr extends AbstractExprContainer implements Expr {
    public static final int LHS_INDEX = 0;
    public static final int RHS_INDEX = 1;
    public Op op;

    public BinaryExpr(final @NotNull Expr lhs, final @NotNull Expr rhs, final @NotNull Op op) {
        addExpression(lhs);
        addExpression(rhs);
        this.op = op;
    }

    public @NotNull Expr getLhs() {
        return getExpressions().get(LHS_INDEX);
    }

    public void setLhs(final @NotNull Expr lhs) {
        getExpressions().set(LHS_INDEX, lhs);
    }

    public @NotNull Expr getRhs() {
        return getExpressions().get(RHS_INDEX);
    }

    public void setRhs(final @NotNull Expr rhs) {
        getExpressions().set(RHS_INDEX, rhs);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // For all comparisons, we always evaluate to booleans except for spaceship
            case EQ, NE, LT, LE, GT, GE -> BuiltinType.BOOL;
            // Spaceship always evaluates to i32
            case CMP -> BuiltinType.I32;
            // All other binary expressions evaluate to their left hand side type
            default -> getLhs().getType(context);
        };
    }

    // Use number logic from regular arithmetic operators as pre-assignment step
    private @NotNull ConstExpr evaluatePreAssignmentForNumber(final @NotNull Number lhsNumber,
                                                              final @NotNull Number rhsNumber,
                                                              final @NotNull BuiltinType type,
                                                              final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (type) {
            case I8 -> evaluateForByte(lhsNumber.byteValue(), rhsNumber, op.discardAssign(), context);
            case I16 -> evaluateForShort(lhsNumber.shortValue(), rhsNumber, op.discardAssign(), context);
            case I32 -> evaluateForInteger(lhsNumber.intValue(), rhsNumber, op.discardAssign(), context);
            case I64 -> evaluateForLong(lhsNumber.longValue(), rhsNumber, op.discardAssign(), context);
            case F32 -> evaluateForFloat(lhsNumber.floatValue(), rhsNumber, op.discardAssign(), context);
            case F64 -> evaluateForDouble(lhsNumber.doubleValue(), rhsNumber, op.discardAssign(), context);
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsNumber,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull ConstExpr evaluatePreAssignment(final @NotNull EvaluationContext context,
                                                     final @NotNull ConstExpr oldValue,
                                                     final @NotNull ConstExpr operand) throws EvaluationException {
        if (op == Op.ASSIGN) {
            return operand; // On regular assignments, we just forward the value as is
        }
        final var type = oldValue.getType(context);
        if (!(type instanceof BuiltinType builtinType)) {
            final var message = String.format("Cannot perform re-assignment with operator %s on type %s", op, type);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var oldValueRef = oldValue.getConstValue();
        final var operandRef = operand.getConstValue();
        if (oldValueRef instanceof Number lhsNumber && operandRef instanceof Number rhsNumber) {
            return evaluatePreAssignmentForNumber(lhsNumber, rhsNumber, builtinType, context);
        }
        else if (oldValueRef instanceof Boolean lhsBool && operandRef instanceof Boolean rhsBool) {
            return evaluateForBool(lhsBool, rhsBool, op.discardAssign(), context);
        }
        final var message = String.format("Cannot perform re-assignment with operator %s on type %s", op, type);
        throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
    }

    private void evaluateAssignment(final @NotNull EvaluationContext context) throws EvaluationException {
        final var lhs = getLhs();
        if (!(lhs instanceof Reference reference)) {
            final var message = "Left-hand side of assignment must be a reference";
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var expectedType = getType(context);
        final var value = getRhs();
        final var valueType = value.getType(context);
        if (!expectedType.isAssignableFrom(valueType)) {
            final var message = String.format("Cannot assign value of type %s to %s", valueType, expectedType);
            throw new EvaluationException(message,
                SourceDiagnostic.from(this, value, message),
                context.createStackTrace());
        }
        final var oldValue = reference.loadFromReference(context);
        final var newValue = evaluatePreAssignment(context, oldValue, value.evaluateAsConst(context));
        reference.storeToReference(newValue, context);
        context.pushValue(newValue);
    }

    private @NotNull ConstExpr evaluateForType(final @NotNull Type lhs,
                                               final @NotNull Type rhs,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case ADD -> ConstExpr.of(IntersectionType.unfold(List.of(lhs, rhs)), getTokenRange());
            case SUB -> {
                if (lhs instanceof IntersectionType lhsIntersectionType) {
                    final var newType = lhsIntersectionType.unfold();
                    final var alternatives = newType.alternatives();
                    alternatives.remove(rhs);
                    if (alternatives.size() == 1) {
                        yield ConstExpr.of(alternatives.get(0),
                            getTokenRange()); // Unwrap single type from intersection type
                    }
                    yield ConstExpr.of(newType, getTokenRange());
                }
                final var message = "Left hand side type must be an intersection type for subtraction operation!";
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            case AND -> { // @formatter:off
                final var lhsAlternatives = lhs instanceof IntersectionType lhsIntersectionType
                    ? lhsIntersectionType.alternatives()
                    : List.of(lhs);
                final var rhsAlternatives = rhs instanceof IntersectionType rhsIntersectionType
                    ? rhsIntersectionType.alternatives()
                    : List.of(rhs);
                yield ConstExpr.of(IntersectionType.unfold(
                    CollectionUtils.intersect(lhsAlternatives, rhsAlternatives, ArrayList::new)), getTokenRange());
            } // @formatter:on
            default -> {
                final var message = String.format("Unsupported type binary expression: %s %s %s", lhs, op, rhs);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForBool(final @NotNull Boolean lhsBool,
                                               final @NotNull Boolean rhsBool,
                                               final @NotNull Op op,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Boolean equality
            case EQ -> ConstExpr.of(lhsBool == rhsBool, getTokenRange());
            case NE -> ConstExpr.of(lhsBool != rhsBool, getTokenRange());
            // Boolean logic
            case AND -> ConstExpr.of(lhsBool & rhsBool, getTokenRange());
            case OR -> ConstExpr.of(lhsBool | rhsBool, getTokenRange());
            case XOR -> ConstExpr.of(lhsBool ^ rhsBool, getTokenRange());
            case SC_AND -> {
                if (!lhsBool) {
                    yield ConstExpr.of(false, getTokenRange());
                }
                yield ConstExpr.of(getRhs().evaluateAs(context, Boolean.class), getTokenRange());
            }
            case SC_OR -> {
                if (lhsBool) {
                    yield ConstExpr.of(true, getTokenRange());
                }
                yield ConstExpr.of(getRhs().evaluateAs(context, Boolean.class), getTokenRange());
            }
            default -> {
                final var message = String.format("Unsupported boolean binary expression: %s %s %s",
                    lhsBool,
                    op,
                    getRhs().evaluateAs(context, Object.class));
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForByte(final @NotNull Byte lhsByte,
                                               final @NotNull Number rhsNumber,
                                               final @NotNull Op op,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsByte == rhsNumber.byteValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsByte != rhsNumber.byteValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsByte < rhsNumber.byteValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsByte <= rhsNumber.byteValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsByte > rhsNumber.byteValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsByte >= rhsNumber.byteValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsByte.compareTo(rhsNumber.byteValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsByte + rhsNumber.byteValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsByte - rhsNumber.byteValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsByte * rhsNumber.byteValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsByte / rhsNumber.byteValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsByte % rhsNumber.byteValue(), getTokenRange());
            // Bitwise operations
            case LSH -> ConstExpr.of(lhsByte << rhsNumber.intValue(), getTokenRange());
            case RSH -> ConstExpr.of(lhsByte >> rhsNumber.intValue(), getTokenRange());
            case URSH -> ConstExpr.of(lhsByte >>> rhsNumber.intValue(), getTokenRange());
            case AND -> ConstExpr.of(lhsByte & rhsNumber.byteValue(), getTokenRange());
            case OR -> ConstExpr.of(lhsByte | rhsNumber.byteValue(), getTokenRange());
            case XOR -> ConstExpr.of(lhsByte ^ rhsNumber.byteValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsByte,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForShort(final @NotNull Short lhsShort,
                                                final @NotNull Number rhsNumber,
                                                final @NotNull Op op,
                                                final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsShort == rhsNumber.shortValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsShort != rhsNumber.shortValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsShort < rhsNumber.shortValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsShort <= rhsNumber.shortValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsShort > rhsNumber.shortValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsShort >= rhsNumber.shortValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsShort.compareTo(rhsNumber.shortValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsShort + rhsNumber.shortValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsShort - rhsNumber.shortValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsShort * rhsNumber.shortValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsShort / rhsNumber.shortValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsShort % rhsNumber.shortValue(), getTokenRange());
            // Bitwise operations
            case LSH -> ConstExpr.of(lhsShort << rhsNumber.intValue(), getTokenRange());
            case RSH -> ConstExpr.of(lhsShort >> rhsNumber.intValue(), getTokenRange());
            case URSH -> ConstExpr.of(lhsShort >>> rhsNumber.intValue(), getTokenRange());
            case AND -> ConstExpr.of(lhsShort & rhsNumber.shortValue(), getTokenRange());
            case OR -> ConstExpr.of(lhsShort | rhsNumber.shortValue(), getTokenRange());
            case XOR -> ConstExpr.of(lhsShort ^ rhsNumber.shortValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsShort,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForInteger(final @NotNull Integer lhsInteger,
                                                  final @NotNull Number rhsNumber,
                                                  final @NotNull Op op,
                                                  final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsInteger == rhsNumber.intValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsInteger != rhsNumber.intValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsInteger < rhsNumber.intValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsInteger <= rhsNumber.intValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsInteger > rhsNumber.intValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsInteger >= rhsNumber.intValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsInteger.compareTo(rhsNumber.intValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsInteger + rhsNumber.intValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsInteger - rhsNumber.intValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsInteger * rhsNumber.intValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsInteger / rhsNumber.intValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsInteger % rhsNumber.intValue(), getTokenRange());
            // Bitwise operations
            case LSH -> ConstExpr.of(lhsInteger << rhsNumber.intValue(), getTokenRange());
            case RSH -> ConstExpr.of(lhsInteger >> rhsNumber.intValue(), getTokenRange());
            case URSH -> ConstExpr.of(lhsInteger >>> rhsNumber.intValue(), getTokenRange());
            case AND -> ConstExpr.of(lhsInteger & rhsNumber.intValue(), getTokenRange());
            case OR -> ConstExpr.of(lhsInteger | rhsNumber.intValue(), getTokenRange());
            case XOR -> ConstExpr.of(lhsInteger ^ rhsNumber.intValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsInteger,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForLong(final @NotNull Long lhsLong,
                                               final @NotNull Number rhsNumber,
                                               final @NotNull Op op,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsLong == rhsNumber.longValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsLong != rhsNumber.longValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsLong < rhsNumber.longValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsLong <= rhsNumber.longValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsLong > rhsNumber.longValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsLong >= rhsNumber.longValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsLong.compareTo(rhsNumber.longValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsLong + rhsNumber.longValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsLong - rhsNumber.longValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsLong * rhsNumber.longValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsLong / rhsNumber.longValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsLong % rhsNumber.longValue(), getTokenRange());
            // Bitwise operations
            case LSH -> ConstExpr.of(lhsLong << rhsNumber.intValue(), getTokenRange());
            case RSH -> ConstExpr.of(lhsLong >> rhsNumber.intValue(), getTokenRange());
            case URSH -> ConstExpr.of(lhsLong >>> rhsNumber.intValue(), getTokenRange());
            case AND -> ConstExpr.of(lhsLong & rhsNumber.longValue(), getTokenRange());
            case OR -> ConstExpr.of(lhsLong | rhsNumber.longValue(), getTokenRange());
            case XOR -> ConstExpr.of(lhsLong ^ rhsNumber.longValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsLong,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForFloat(final @NotNull Float lhsFloat,
                                                final @NotNull Number rhsNumber,
                                                final @NotNull Op op,
                                                final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsFloat == rhsNumber.floatValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsFloat != rhsNumber.floatValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsFloat < rhsNumber.floatValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsFloat <= rhsNumber.floatValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsFloat > rhsNumber.floatValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsFloat >= rhsNumber.floatValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsFloat.compareTo(rhsNumber.floatValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsFloat + rhsNumber.floatValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsFloat - rhsNumber.floatValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsFloat * rhsNumber.floatValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsFloat / rhsNumber.floatValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsFloat % rhsNumber.floatValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsFloat,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForDouble(final @NotNull Double lhsDouble,
                                                 final @NotNull Number rhsNumber,
                                                 final @NotNull Op op,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> ConstExpr.of(lhsDouble == rhsNumber.doubleValue(), getTokenRange());
            case NE -> ConstExpr.of(lhsDouble != rhsNumber.doubleValue(), getTokenRange());
            case LT -> ConstExpr.of(lhsDouble < rhsNumber.doubleValue(), getTokenRange());
            case LE -> ConstExpr.of(lhsDouble <= rhsNumber.doubleValue(), getTokenRange());
            case GT -> ConstExpr.of(lhsDouble > rhsNumber.doubleValue(), getTokenRange());
            case GE -> ConstExpr.of(lhsDouble >= rhsNumber.doubleValue(), getTokenRange());
            case CMP -> ConstExpr.of(lhsDouble.compareTo(rhsNumber.doubleValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> ConstExpr.of(lhsDouble + rhsNumber.doubleValue(), getTokenRange());
            case SUB -> ConstExpr.of(lhsDouble - rhsNumber.doubleValue(), getTokenRange());
            case MUL -> ConstExpr.of(lhsDouble * rhsNumber.doubleValue(), getTokenRange());
            case DIV -> ConstExpr.of(lhsDouble / rhsNumber.doubleValue(), getTokenRange());
            case REM -> ConstExpr.of(lhsDouble % rhsNumber.doubleValue(), getTokenRange());
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsDouble,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForNumber(final @NotNull Number lhsNumber,
                                                 final @NotNull BuiltinType type,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var rhsValue = getRhs().evaluateAs(context, Object.class);
        if (!(rhsValue instanceof Number rhsNumber)) {
            final var message = "Numeric binary expression must have a number on the right hand side!";
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        return switch (type) {
            case I8 -> evaluateForByte(lhsNumber.byteValue(), rhsNumber, op, context);
            case I16 -> evaluateForShort(lhsNumber.shortValue(), rhsNumber, op, context);
            case I32 -> evaluateForInteger(lhsNumber.intValue(), rhsNumber, op, context);
            case I64 -> evaluateForLong(lhsNumber.longValue(), rhsNumber, op, context);
            case F32 -> evaluateForFloat(lhsNumber.floatValue(), rhsNumber, op, context);
            case F64 -> evaluateForDouble(lhsNumber.doubleValue(), rhsNumber, op, context);
            default -> {
                final var message = String.format("Unsupported numeric binary expression: %s %s %s",
                    lhsNumber,
                    op,
                    rhsNumber);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateForString(final @NotNull String lhsValue,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var rhsValue = getRhs().evaluateAs(context, Object.class).toString();
        return switch (op) {
            case ADD -> ConstExpr.of(String.format("%s%s", lhsValue, rhsValue), getTokenRange());
            case CMP -> ConstExpr.of(lhsValue.compareTo(rhsValue), getTokenRange());
            case EQ -> ConstExpr.of(lhsValue.equals(rhsValue), getTokenRange());
            case NE -> ConstExpr.of(!lhsValue.equals(rhsValue), getTokenRange());
            default -> {
                final var message = String.format("Unsupported string binary expression: %s %s %s",
                    lhsValue,
                    op,
                    rhsValue);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private @NotNull ConstExpr evaluateForArray(final @NotNull Object lhsValue,
                                                final @NotNull ArrayType lhsType,
                                                final @NotNull EvaluationContext context) throws EvaluationException {
        final var rhsValue = getRhs().evaluateAs(context, Object.class);
        final var rhsType = getRhs().getType(context);
        final var rhsArrayValue = rhsType instanceof ArrayType ? rhsValue : new Object[]{rhsValue};
        final var lhsLength = Array.getLength(lhsValue);
        final var rhsLength = Array.getLength(rhsArrayValue);
        final var lhsElementType = TypeMapper.map(lhsType.elementType());
        return switch (op) {
            case ADD -> {
                final var newLength = lhsLength + rhsLength;
                final var newArray = Array.newInstance(lhsElementType, newLength);
                System.arraycopy(lhsValue, 0, newArray, 0, lhsLength);
                System.arraycopy(rhsArrayValue, 0, newArray, lhsLength, rhsLength);
                yield ConstExpr.of(newArray, getTokenRange());
            }
            case SUB -> {
                final var lhsValues = new ArrayList<>(lhsLength);
                for (var i = 0; i < lhsLength; i++) {
                    lhsValues.add(Array.get(lhsValue, i));
                }
                final var rhsValues = new HashSet<>(rhsLength);
                for (var i = 0; i < rhsLength; i++) {
                    rhsValues.add(Array.get(rhsArrayValue, i));
                }
                lhsValues.removeAll(rhsValues);
                final var newArray = Array.newInstance(lhsElementType, lhsValues.size());
                for (var i = 0; i < lhsValues.size(); i++) {
                    Array.set(newArray, i, lhsValues.get(i));
                }
                yield ConstExpr.of(newArray, getTokenRange());
            }
            default -> {
                final var message = String.format("Unsupported array binary expression: %s %s %s",
                    lhsValue,
                    op,
                    rhsValue);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        if (op.isAssignment) {
            evaluateAssignment(context);
            return;
        }
        final var lhsValue = getLhs().evaluateAs(context, Object.class);
        final var lhsType = getLhs().getType(context);
        // Arrays are the only thing where we care about either sides type
        if (lhsType instanceof ArrayType lhsArrayType) {
            context.pushValue(evaluateForArray(lhsValue, lhsArrayType, context));
            return;
        }
        if (lhsType == BuiltinType.STRING) { // String concatenation with any type
            context.pushValue(evaluateForString((String) lhsValue, context));
            return;
        }
        else if (lhsType == PreproType.TYPE) { // Type addition/subtraction creates intersection types
            final var lhsTypeValue = getLhs().evaluateAs(context, Type.class);
            final var rhsTypeValue = getRhs().evaluateAs(context, Type.class);
            context.pushValue(evaluateForType(lhsTypeValue, rhsTypeValue, context));
            return;
        }
        else if (lhsType == BuiltinType.BOOL) { // Boolean binary expressions
            final var rhsValue = getRhs().evaluateAs(context, Boolean.class);
            context.pushValue(evaluateForBool((Boolean) lhsValue, rhsValue, op, context));
            return;
        }
        else if (lhsType.getCategory().isNumber()) { // Numeric binary expressions
            context.pushValue(evaluateForNumber((Number) lhsValue, (BuiltinType) lhsType, context));
            return;
        }
        final var message = String.format("Unsupported binary expression operands: %s %s %s",
            lhsValue,
            op,
            getRhs().evaluateAs(context, Object.class));
        throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
    }

    @Override
    public @NotNull BinaryExpr copy() {
        return copyParentAndSourceTo(new BinaryExpr(getLhs().copy(), getRhs().copy(), op));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s %s %s", getLhs(), op, getRhs());
    }

    public enum Op {
        // @formatter:off
        ASSIGN      (true), // Simple re-assignments
        ADD         (false), // Arithmetic
        SUB         (false),
        MUL         (false),
        DIV         (false),
        REM         (false),
        PLUS_ASSIGN (true), // Arithmetic re-assignments
        MINUS_ASSIGN(true),
        TIMES_ASSIGN(true),
        DIV_ASSIGN  (true),
        REM_ASSIGN  (true),
        EQ          (false), // Comparisons
        NE          (false),
        LT          (false),
        LE          (false),
        GT          (false),
        GE          (false),
        CMP         (false),
        LSH         (false), // Logic
        RSH         (false),
        URSH        (false),
        AND         (false),
        OR          (false),
        XOR         (false),
        LSH_ASSIGN  (true), // Logic re-assignments
        RSH_ASSIGN  (true),
        URSH_ASSIGN (true),
        AND_ASSIGN  (true),
        OR_ASSIGN   (true),
        XOR_ASSIGN  (true),
        SC_AND      (false), // Boolean logic
        SC_OR       (false);
        // @formatter:on

        public final boolean isAssignment;

        Op(final boolean isAssignment) {
            this.isAssignment = isAssignment;
        }

        public @NotNull Op discardAssign() {
            return switch (this) {
                case PLUS_ASSIGN -> ADD;
                case MINUS_ASSIGN -> SUB;
                case TIMES_ASSIGN -> MUL;
                case DIV_ASSIGN -> DIV;
                case REM_ASSIGN -> REM;
                case LSH_ASSIGN -> LSH;
                case RSH_ASSIGN -> RSH;
                case URSH_ASSIGN -> URSH;
                case AND_ASSIGN -> AND;
                case OR_ASSIGN -> OR;
                case XOR_ASSIGN -> XOR;
                default -> this;
            };
        }
    }
}
