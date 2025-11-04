package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.IntersectionType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

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
                throw new EvaluationException(
                    "Left hand side type must be an intersection type for subtraction operation!",
                    SourceDiagnostic.from(this),
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
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            // Boolean equality
            case EQ -> {
                final var rhsBool = getRhs().evaluateAsConst(context, Boolean.class);
                yield LiteralExpr.of(lhsBool == rhsBool, getTokenRange());
            }
            case NE -> {
                final var rhsBool = getRhs().evaluateAsConst(context, Boolean.class);
                yield LiteralExpr.of(lhsBool != rhsBool, getTokenRange());
            }
            // Boolean logic
            case AND -> {
                final var rhsBool = getRhs().evaluateAsConst(context, Boolean.class);
                yield LiteralExpr.of(lhsBool & rhsBool, getTokenRange());
            }
            case OR -> {
                final var rhsBool = getRhs().evaluateAsConst(context, Boolean.class);
                yield LiteralExpr.of(lhsBool | rhsBool, getTokenRange());
            }
            case XOR -> {
                final var rhsBool = getRhs().evaluateAsConst(context, Boolean.class);
                yield LiteralExpr.of(lhsBool ^ rhsBool, getTokenRange());
            }
            case SC_AND -> {
                if (!lhsBool) {
                    yield LiteralExpr.of(false);
                }
                yield LiteralExpr.of(getRhs().evaluateAsConst(context, Boolean.class), getTokenRange());
            }
            case SC_OR -> {
                if (lhsBool) {
                    yield LiteralExpr.of(true, getTokenRange());
                }
                yield LiteralExpr.of(getRhs().evaluateAsConst(context, Boolean.class), getTokenRange());
            }
            default -> throw new EvaluationException(String.format("Unsupported boolean binary expression: %s %s %s",
                lhsBool,
                op,
                getRhs().evaluateAsConst(context, Object.class)),
                SourceDiagnostic.from(this),
                context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForByte(final @NotNull Byte lhsByte,
                                                 final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsByte,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForShort(final @NotNull Short lhsShort,
                                                  final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsShort,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForInteger(final @NotNull Integer lhsInteger,
                                                    final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsInteger,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForLong(final @NotNull Long lhsLong,
                                                 final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsLong,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForFloat(final @NotNull Float lhsFloat,
                                                  final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsFloat,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForDouble(final @NotNull Double lhsDouble,
                                                   final @NotNull Number rhsNumber,
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
            default -> throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
                lhsDouble,
                op,
                rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
        };
    }

    private @NotNull LiteralExpr evaluateForNumber(final @NotNull Number lhsNumber,
                                                   final @NotNull EvaluationContext context) throws EvaluationException {
        final var rhsValue = getRhs().evaluateAsConst(context, Object.class);
        if (!(rhsValue instanceof Number rhsNumber)) {
            throw new EvaluationException("Numeric binary expression must have a number on the right hand side!",
                SourceDiagnostic.from(this),
                context.createStackTrace());
        }
        if (lhsNumber instanceof Byte lhsByte) {
            return evaluateForByte(lhsByte, rhsNumber, context);
        }
        else if (lhsNumber instanceof Short lhsShort) {
            return evaluateForShort(lhsShort, rhsNumber, context);
        }
        else if (lhsNumber instanceof Integer lhsInteger) {
            return evaluateForInteger(lhsInteger, rhsNumber, context);
        }
        else if (lhsNumber instanceof Long lhsLong) {
            return evaluateForLong(lhsLong, rhsNumber, context);
        }
        else if (lhsNumber instanceof Float lhsFloat) {
            return evaluateForFloat(lhsFloat, rhsNumber, context);
        }
        else if (lhsNumber instanceof Double lhsDouble) {
            return evaluateForDouble(lhsDouble, rhsNumber, context);
        }
        throw new EvaluationException(String.format("Unsupported numeric binary expression: %s %s %s",
            lhsNumber,
            op,
            rhsNumber), SourceDiagnostic.from(this), context.createStackTrace());
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var lhsValue = getLhs().evaluateAsConst(context, Object.class);
        if (lhsValue instanceof String lhsString) { // String concatenation with any type
            switch (op) {
                case ADD -> {
                    final var rhsValue = getRhs().evaluateAsConst(context, Object.class);
                    context.pushValue(LiteralExpr.of(String.format("%s%s", lhsString, rhsValue), getTokenRange()));
                }
                case CMP -> { // TODO: there are better ways to handle this but this'll do for now..
                    final var rhsString = getRhs().evaluateAsConst(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(lhsString.compareTo(rhsString)));
                }
                case EQ -> {
                    final var rhsString = getRhs().evaluateAsConst(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(lhsString.equals(rhsString)));
                }
                case NE -> {
                    final var rhsString = getRhs().evaluateAsConst(context, Object.class).toString();
                    context.pushValue(LiteralExpr.of(!lhsString.equals(rhsString)));
                }
            }
            return;
        }
        else if (lhsValue instanceof Type lhsType) { // Type addition/subtraction creates intersection types
            final var rhsType = getRhs().evaluateAsConst(context, Type.class);
            context.pushValue(evaluateForType(lhsType, rhsType, context));
            return;
        }
        else if (lhsValue instanceof Boolean lhsBool) { // Boolean binary expressions
            context.pushValue(evaluateForBool(lhsBool, context));
            return;
        }
        else if (lhsValue instanceof Number lhsNumber) { // Numeric binary expressions
            context.pushValue(evaluateForNumber(lhsNumber, context));
            return;
        }
        throw new EvaluationException(String.format("Unsupported binary expression operands: %s %s %s",
            lhsValue,
            op,
            getRhs().evaluateAsConst(context, Object.class)), SourceDiagnostic.from(this), context.createStackTrace());
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
        ADD, SUB, MUL, DIV, REM, // Arithmetic
        EQ, NE, LT, LE, GT, GE, CMP, // Comparisons
        LSH, RSH, URSH, AND, OR, XOR, SC_AND, SC_OR // Logic
    }
}
