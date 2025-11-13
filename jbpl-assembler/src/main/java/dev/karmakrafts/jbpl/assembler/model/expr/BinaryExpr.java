package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.IntersectionType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
    private @NotNull LiteralExpr evaluatePreAssignmentForNumber(final @NotNull Number lhsNumber,
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

    private @NotNull LiteralExpr evaluatePreAssignment(final @NotNull EvaluationContext context,
                                                       final @NotNull LiteralExpr oldValue,
                                                       final @NotNull LiteralExpr operand) throws EvaluationException {
        if (op == Op.ASSIGN) {
            return oldValue; // On regular assignments, we just forward the value as is
        }
        final var type = oldValue.type;
        if (!(type instanceof BuiltinType builtinType)) {
            final var message = String.format("Cannot perform re-assignment with operator %s on type %s", op, type);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var oldValueRef = oldValue.value;
        final var operandRef = operand.value;
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
        final var reference = getLhs();
        final var expectedType = getType(context);
        final var value = getRhs();
        final var valueType = value.getType(context);
        if (!expectedType.isAssignableFrom(valueType)) {
            final var message = String.format("Cannot assign value of type %s to %s", valueType, expectedType);
            throw new EvaluationException(message,
                SourceDiagnostic.from(this, value, message),
                context.createStackTrace());
        }
        if (reference instanceof ReferenceExpr refExpr) {
            // In this case, the only valid option is that we are re-assigning a define, since params are always immutable
            if (refExpr.findArgument(context) != null) {
                final var message = String.format("Cannot re-assign parameter '%s'", refExpr.name);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, reference, message),
                    context.createStackTrace());
            }
            final var define = refExpr.getDefine(context);
            if (define.isFinal) {
                final var message = String.format("Cannot re-assign final define '%s'", refExpr.name);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, reference, message),
                    context.createStackTrace());
            }
            final var oldValue = define.getValue().evaluateAsConst(context);
            final var wrappedValue = evaluatePreAssignment(context, oldValue, value.evaluateAsConst(context));
            define.setValue(wrappedValue);
            context.pushValue(wrappedValue); // Assignments evaluate to their assigned value
            return;
        }
        else if (reference instanceof ArrayAccessExpr arrayAccessExpr) {
            // In this case, we are storing into the array. Reference evaluates to an array ref, against what the function name suggests.
            final var arrayRef = arrayAccessExpr.getReference().evaluateAs(context, Object.class);
            final int arrayIndex = arrayAccessExpr.getIndex().evaluateAs(context, Integer.class);
            final var oldValue = Array.get(arrayRef, arrayIndex);
            final var wrappedValue = evaluatePreAssignment(context,
                LiteralExpr.of(oldValue),
                value.evaluateAsConst(context));
            Array.set(arrayRef, arrayIndex, wrappedValue.value);
            context.pushValue(wrappedValue);
            return;
        }
        final var message = String.format("Cannot re-assign expression %s", reference);
        throw new EvaluationException(message,
            SourceDiagnostic.from(this, reference, message),
            context.createStackTrace());
    }

    private @NotNull LiteralExpr evaluateForType(final @NotNull Type lhs,
                                                 final @NotNull Type rhs,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case ADD -> LiteralExpr.of(IntersectionType.unfold(List.of(lhs, rhs)), getTokenRange());
            case SUB -> {
                if (lhs instanceof IntersectionType lhsIntersectionType) {
                    final var newType = lhsIntersectionType.unfold();
                    final var alternatives = newType.alternatives();
                    alternatives.remove(rhs);
                    if (alternatives.size() == 1) {
                        yield LiteralExpr.of(alternatives.get(0),
                            getTokenRange()); // Unwrap single type from intersection type
                    }
                    yield LiteralExpr.of(newType, getTokenRange());
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
                yield LiteralExpr.of(IntersectionType.unfold(
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

    private @NotNull LiteralExpr evaluateForBool(final @NotNull Boolean lhsBool,
                                                 final @NotNull Boolean rhsBool,
                                                 final @NotNull Op op,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Boolean equality
            case EQ -> LiteralExpr.of(lhsBool == rhsBool, getTokenRange());
            case NE -> LiteralExpr.of(lhsBool != rhsBool, getTokenRange());
            // Boolean logic
            case AND -> LiteralExpr.of(lhsBool & rhsBool, getTokenRange());
            case OR -> LiteralExpr.of(lhsBool | rhsBool, getTokenRange());
            case XOR -> LiteralExpr.of(lhsBool ^ rhsBool, getTokenRange());
            case SC_AND -> {
                if (!lhsBool) {
                    yield LiteralExpr.of(false, getTokenRange());
                }
                yield LiteralExpr.of(getRhs().evaluateAs(context, Boolean.class), getTokenRange());
            }
            case SC_OR -> {
                if (lhsBool) {
                    yield LiteralExpr.of(true, getTokenRange());
                }
                yield LiteralExpr.of(getRhs().evaluateAs(context, Boolean.class), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForByte(final @NotNull Byte lhsByte,
                                                 final @NotNull Number rhsNumber,
                                                 final @NotNull Op op,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsByte == rhsNumber.byteValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsByte != rhsNumber.byteValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsByte < rhsNumber.byteValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsByte <= rhsNumber.byteValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsByte > rhsNumber.byteValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsByte >= rhsNumber.byteValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsByte.compareTo(rhsNumber.byteValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsByte + rhsNumber.byteValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsByte - rhsNumber.byteValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsByte * rhsNumber.byteValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsByte / rhsNumber.byteValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsByte % rhsNumber.byteValue(), getTokenRange());
            // Bitwise operations
            case LSH -> LiteralExpr.of(lhsByte << rhsNumber.intValue(), getTokenRange());
            case RSH -> LiteralExpr.of(lhsByte >> rhsNumber.intValue(), getTokenRange());
            case URSH -> LiteralExpr.of(lhsByte >>> rhsNumber.intValue(), getTokenRange());
            case AND -> LiteralExpr.of(lhsByte & rhsNumber.byteValue(), getTokenRange());
            case OR -> LiteralExpr.of(lhsByte | rhsNumber.byteValue(), getTokenRange());
            case XOR -> LiteralExpr.of(lhsByte ^ rhsNumber.byteValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForShort(final @NotNull Short lhsShort,
                                                  final @NotNull Number rhsNumber,
                                                  final @NotNull Op op,
                                                  final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsShort == rhsNumber.shortValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsShort != rhsNumber.shortValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsShort < rhsNumber.shortValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsShort <= rhsNumber.shortValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsShort > rhsNumber.shortValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsShort >= rhsNumber.shortValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsShort.compareTo(rhsNumber.shortValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsShort + rhsNumber.shortValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsShort - rhsNumber.shortValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsShort * rhsNumber.shortValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsShort / rhsNumber.shortValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsShort % rhsNumber.shortValue(), getTokenRange());
            // Bitwise operations
            case LSH -> LiteralExpr.of(lhsShort << rhsNumber.intValue(), getTokenRange());
            case RSH -> LiteralExpr.of(lhsShort >> rhsNumber.intValue(), getTokenRange());
            case URSH -> LiteralExpr.of(lhsShort >>> rhsNumber.intValue(), getTokenRange());
            case AND -> LiteralExpr.of(lhsShort & rhsNumber.shortValue(), getTokenRange());
            case OR -> LiteralExpr.of(lhsShort | rhsNumber.shortValue(), getTokenRange());
            case XOR -> LiteralExpr.of(lhsShort ^ rhsNumber.shortValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForInteger(final @NotNull Integer lhsInteger,
                                                    final @NotNull Number rhsNumber,
                                                    final @NotNull Op op,
                                                    final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsInteger == rhsNumber.intValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsInteger != rhsNumber.intValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsInteger < rhsNumber.intValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsInteger <= rhsNumber.intValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsInteger > rhsNumber.intValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsInteger >= rhsNumber.intValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsInteger.compareTo(rhsNumber.intValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsInteger + rhsNumber.intValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsInteger - rhsNumber.intValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsInteger * rhsNumber.intValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsInteger / rhsNumber.intValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsInteger % rhsNumber.intValue(), getTokenRange());
            // Bitwise operations
            case LSH -> LiteralExpr.of(lhsInteger << rhsNumber.intValue(), getTokenRange());
            case RSH -> LiteralExpr.of(lhsInteger >> rhsNumber.intValue(), getTokenRange());
            case URSH -> LiteralExpr.of(lhsInteger >>> rhsNumber.intValue(), getTokenRange());
            case AND -> LiteralExpr.of(lhsInteger & rhsNumber.intValue(), getTokenRange());
            case OR -> LiteralExpr.of(lhsInteger | rhsNumber.intValue(), getTokenRange());
            case XOR -> LiteralExpr.of(lhsInteger ^ rhsNumber.intValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForLong(final @NotNull Long lhsLong,
                                                 final @NotNull Number rhsNumber,
                                                 final @NotNull Op op,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsLong == rhsNumber.longValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsLong != rhsNumber.longValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsLong < rhsNumber.longValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsLong <= rhsNumber.longValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsLong > rhsNumber.longValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsLong >= rhsNumber.longValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsLong.compareTo(rhsNumber.longValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsLong + rhsNumber.longValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsLong - rhsNumber.longValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsLong * rhsNumber.longValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsLong / rhsNumber.longValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsLong % rhsNumber.longValue(), getTokenRange());
            // Bitwise operations
            case LSH -> LiteralExpr.of(lhsLong << rhsNumber.intValue(), getTokenRange());
            case RSH -> LiteralExpr.of(lhsLong >> rhsNumber.intValue(), getTokenRange());
            case URSH -> LiteralExpr.of(lhsLong >>> rhsNumber.intValue(), getTokenRange());
            case AND -> LiteralExpr.of(lhsLong & rhsNumber.longValue(), getTokenRange());
            case OR -> LiteralExpr.of(lhsLong | rhsNumber.longValue(), getTokenRange());
            case XOR -> LiteralExpr.of(lhsLong ^ rhsNumber.longValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForFloat(final @NotNull Float lhsFloat,
                                                  final @NotNull Number rhsNumber,
                                                  final @NotNull Op op,
                                                  final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsFloat == rhsNumber.floatValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsFloat != rhsNumber.floatValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsFloat < rhsNumber.floatValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsFloat <= rhsNumber.floatValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsFloat > rhsNumber.floatValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsFloat >= rhsNumber.floatValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsFloat.compareTo(rhsNumber.floatValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsFloat + rhsNumber.floatValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsFloat - rhsNumber.floatValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsFloat * rhsNumber.floatValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsFloat / rhsNumber.floatValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsFloat % rhsNumber.floatValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForDouble(final @NotNull Double lhsDouble,
                                                   final @NotNull Number rhsNumber,
                                                   final @NotNull Op op,
                                                   final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Comparisons
            case EQ -> LiteralExpr.of(lhsDouble == rhsNumber.doubleValue(), getTokenRange());
            case NE -> LiteralExpr.of(lhsDouble != rhsNumber.doubleValue(), getTokenRange());
            case LT -> LiteralExpr.of(lhsDouble < rhsNumber.doubleValue(), getTokenRange());
            case LE -> LiteralExpr.of(lhsDouble <= rhsNumber.doubleValue(), getTokenRange());
            case GT -> LiteralExpr.of(lhsDouble > rhsNumber.doubleValue(), getTokenRange());
            case GE -> LiteralExpr.of(lhsDouble >= rhsNumber.doubleValue(), getTokenRange());
            case CMP -> LiteralExpr.of(lhsDouble.compareTo(rhsNumber.doubleValue()), getTokenRange());
            // Arithmetic operations
            case ADD -> LiteralExpr.of(lhsDouble + rhsNumber.doubleValue(), getTokenRange());
            case SUB -> LiteralExpr.of(lhsDouble - rhsNumber.doubleValue(), getTokenRange());
            case MUL -> LiteralExpr.of(lhsDouble * rhsNumber.doubleValue(), getTokenRange());
            case DIV -> LiteralExpr.of(lhsDouble / rhsNumber.doubleValue(), getTokenRange());
            case REM -> LiteralExpr.of(lhsDouble % rhsNumber.doubleValue(), getTokenRange());
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

    private @NotNull LiteralExpr evaluateForNumber(final @NotNull Number lhsNumber,
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

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        if (op.isAssignment) {
            evaluateAssignment(context);
            return;
        }
        final var lhsValue = getLhs().evaluateAs(context, Object.class);
        final var lhsType = getLhs().getType(context);
        if (lhsType == BuiltinType.STRING) { // String concatenation with any type
            switch (op) {
                case ADD -> {
                    final var rhsValue = getRhs().evaluateAs(context, Object.class);
                    context.pushValue(LiteralExpr.of(String.format("%s%s", lhsValue, rhsValue), getTokenRange()));
                }
                case CMP -> {
                    final var rhsString = getRhs().evaluateAs(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(((String) lhsValue).compareTo(rhsString), getTokenRange()));
                }
                case EQ -> {
                    final var rhsString = getRhs().evaluateAs(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(lhsValue.equals(rhsString), getTokenRange()));
                }
                case NE -> {
                    final var rhsString = getRhs().evaluateAs(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(!lhsValue.equals(rhsString), getTokenRange()));
                }
            }
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
