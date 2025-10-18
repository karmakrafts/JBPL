package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

public final class BinaryExpr extends AbstractExprContainer implements Expr {
    public static final int LHS_INDEX = 0;
    public static final int RHS_INDEX = 1;
    public Op op;

    public BinaryExpr(final @NotNull Expr lhs, final @NotNull Expr rhs, final @NotNull Op op) {
        addExpression(lhs);
        addExpression(rhs);
        this.op = op;
    }

    public void setLhs(final @NotNull Expr lhs) {
        getExpressions().set(LHS_INDEX, lhs);
    }

    public @NotNull Expr getLhs() {
        return getExpressions().get(LHS_INDEX);
    }

    public void setRhs(final @NotNull Expr rhs) {
        getExpressions().set(RHS_INDEX, rhs);
    }

    public @NotNull Expr getRhs() {
        return getExpressions().get(RHS_INDEX);
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        final var lhsValue = getLhs().evaluateAsLiteral(context, Object.class);
        if (lhsValue instanceof String lhsString) { // String concatenation with any type
            final var rhsValue = getRhs().evaluateAsLiteral(context, Object.class);
            return LiteralExpr.of(String.format("%s%s", lhsString, rhsValue));
        }
        else if (lhsValue instanceof Boolean lhsBool) { // Boolean binary expressions
            return switch (op) {
                // Boolean equality
                case EQ -> {
                    final var rhsBool = getRhs().evaluateAsLiteral(context, Boolean.class);
                    yield LiteralExpr.of(lhsBool == rhsBool);
                }
                case NE -> {
                    final var rhsBool = getRhs().evaluateAsLiteral(context, Boolean.class);
                    yield LiteralExpr.of(lhsBool != rhsBool);
                }
                // Boolean logic
                case AND -> {
                    final var rhsBool = getRhs().evaluateAsLiteral(context, Boolean.class);
                    yield LiteralExpr.of(lhsBool & rhsBool);
                }
                case OR -> {
                    final var rhsBool = getRhs().evaluateAsLiteral(context, Boolean.class);
                    yield LiteralExpr.of(lhsBool | rhsBool);
                }
                case XOR -> {
                    final var rhsBool = getRhs().evaluateAsLiteral(context, Boolean.class);
                    yield LiteralExpr.of(lhsBool ^ rhsBool);
                }
                case SC_AND -> {
                    if (!lhsBool) {
                        yield LiteralExpr.of(false);
                    }
                    yield LiteralExpr.of(getRhs().evaluateAsLiteral(context, Boolean.class));
                }
                case SC_OR -> {
                    if (lhsBool) {
                        yield LiteralExpr.of(true);
                    }
                    yield LiteralExpr.of(getRhs().evaluateAsLiteral(context, Boolean.class));
                }
                default -> throw new IllegalStateException(String.format(
                    "Unsupported boolean binary expression: %s %s %s",
                    lhsValue,
                    op,
                    getRhs().evaluateAsLiteral(context, Object.class)));
            };
        }
        else if (lhsValue instanceof Number lhsNumber) { // Numeric binary expressions
            final var rhsValue = getRhs().evaluateAsLiteral(context, Object.class);
            if (!(rhsValue instanceof Number rhsNumber)) {
                throw new IllegalStateException("Numeric binary expression must have a number on the right hand side!");
            }
            if (lhsNumber instanceof Byte lhsByte) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsByte == rhsNumber.byteValue());
                    case NE -> LiteralExpr.of(lhsByte != rhsNumber.byteValue());
                    case LT -> LiteralExpr.of(lhsByte < rhsNumber.byteValue());
                    case LE -> LiteralExpr.of(lhsByte <= rhsNumber.byteValue());
                    case GT -> LiteralExpr.of(lhsByte > rhsNumber.byteValue());
                    case GE -> LiteralExpr.of(lhsByte >= rhsNumber.byteValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsByte + rhsNumber.byteValue());
                    case SUB -> LiteralExpr.of(lhsByte - rhsNumber.byteValue());
                    case MUL -> LiteralExpr.of(lhsByte * rhsNumber.byteValue());
                    case DIV -> LiteralExpr.of(lhsByte / rhsNumber.byteValue());
                    case REM -> LiteralExpr.of(lhsByte % rhsNumber.byteValue());
                    // Bitwise operations
                    case LSH -> LiteralExpr.of(lhsByte << rhsNumber.intValue());
                    case RSH -> LiteralExpr.of(lhsByte >> rhsNumber.intValue());
                    case URSH -> LiteralExpr.of(lhsByte >>> rhsNumber.intValue());
                    case AND -> LiteralExpr.of(lhsByte & rhsNumber.byteValue());
                    case OR -> LiteralExpr.of(lhsByte | rhsNumber.byteValue());
                    case XOR -> LiteralExpr.of(lhsByte ^ rhsNumber.byteValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
            else if (lhsNumber instanceof Short lhsShort) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsShort == rhsNumber.shortValue());
                    case NE -> LiteralExpr.of(lhsShort != rhsNumber.shortValue());
                    case LT -> LiteralExpr.of(lhsShort < rhsNumber.shortValue());
                    case LE -> LiteralExpr.of(lhsShort <= rhsNumber.shortValue());
                    case GT -> LiteralExpr.of(lhsShort > rhsNumber.shortValue());
                    case GE -> LiteralExpr.of(lhsShort >= rhsNumber.shortValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsShort + rhsNumber.shortValue());
                    case SUB -> LiteralExpr.of(lhsShort - rhsNumber.shortValue());
                    case MUL -> LiteralExpr.of(lhsShort * rhsNumber.shortValue());
                    case DIV -> LiteralExpr.of(lhsShort / rhsNumber.shortValue());
                    case REM -> LiteralExpr.of(lhsShort % rhsNumber.shortValue());
                    // Bitwise operations
                    case LSH -> LiteralExpr.of(lhsShort << rhsNumber.intValue());
                    case RSH -> LiteralExpr.of(lhsShort >> rhsNumber.intValue());
                    case URSH -> LiteralExpr.of(lhsShort >>> rhsNumber.intValue());
                    case AND -> LiteralExpr.of(lhsShort & rhsNumber.shortValue());
                    case OR -> LiteralExpr.of(lhsShort | rhsNumber.shortValue());
                    case XOR -> LiteralExpr.of(lhsShort ^ rhsNumber.shortValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
            else if (lhsNumber instanceof Integer lhsInteger) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsInteger == rhsNumber.intValue());
                    case NE -> LiteralExpr.of(lhsInteger != rhsNumber.intValue());
                    case LT -> LiteralExpr.of(lhsInteger < rhsNumber.intValue());
                    case LE -> LiteralExpr.of(lhsInteger <= rhsNumber.intValue());
                    case GT -> LiteralExpr.of(lhsInteger > rhsNumber.intValue());
                    case GE -> LiteralExpr.of(lhsInteger >= rhsNumber.intValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsInteger + rhsNumber.intValue());
                    case SUB -> LiteralExpr.of(lhsInteger - rhsNumber.intValue());
                    case MUL -> LiteralExpr.of(lhsInteger * rhsNumber.intValue());
                    case DIV -> LiteralExpr.of(lhsInteger / rhsNumber.intValue());
                    case REM -> LiteralExpr.of(lhsInteger % rhsNumber.intValue());
                    // Bitwise operations
                    case LSH -> LiteralExpr.of(lhsInteger << rhsNumber.intValue());
                    case RSH -> LiteralExpr.of(lhsInteger >> rhsNumber.intValue());
                    case URSH -> LiteralExpr.of(lhsInteger >>> rhsNumber.intValue());
                    case AND -> LiteralExpr.of(lhsInteger & rhsNumber.intValue());
                    case OR -> LiteralExpr.of(lhsInteger | rhsNumber.intValue());
                    case XOR -> LiteralExpr.of(lhsInteger ^ rhsNumber.intValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
            else if (lhsNumber instanceof Long lhsLong) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsLong == rhsNumber.longValue());
                    case NE -> LiteralExpr.of(lhsLong != rhsNumber.longValue());
                    case LT -> LiteralExpr.of(lhsLong < rhsNumber.longValue());
                    case LE -> LiteralExpr.of(lhsLong <= rhsNumber.longValue());
                    case GT -> LiteralExpr.of(lhsLong > rhsNumber.longValue());
                    case GE -> LiteralExpr.of(lhsLong >= rhsNumber.longValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsLong + rhsNumber.longValue());
                    case SUB -> LiteralExpr.of(lhsLong - rhsNumber.longValue());
                    case MUL -> LiteralExpr.of(lhsLong * rhsNumber.longValue());
                    case DIV -> LiteralExpr.of(lhsLong / rhsNumber.longValue());
                    case REM -> LiteralExpr.of(lhsLong % rhsNumber.longValue());
                    // Bitwise operations
                    case LSH -> LiteralExpr.of(lhsLong << rhsNumber.intValue());
                    case RSH -> LiteralExpr.of(lhsLong >> rhsNumber.intValue());
                    case URSH -> LiteralExpr.of(lhsLong >>> rhsNumber.intValue());
                    case AND -> LiteralExpr.of(lhsLong & rhsNumber.longValue());
                    case OR -> LiteralExpr.of(lhsLong | rhsNumber.longValue());
                    case XOR -> LiteralExpr.of(lhsLong ^ rhsNumber.longValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
            else if (lhsNumber instanceof Float lhsFloat) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsFloat == rhsNumber.floatValue());
                    case NE -> LiteralExpr.of(lhsFloat != rhsNumber.floatValue());
                    case LT -> LiteralExpr.of(lhsFloat < rhsNumber.floatValue());
                    case LE -> LiteralExpr.of(lhsFloat <= rhsNumber.floatValue());
                    case GT -> LiteralExpr.of(lhsFloat > rhsNumber.floatValue());
                    case GE -> LiteralExpr.of(lhsFloat >= rhsNumber.floatValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsFloat + rhsNumber.floatValue());
                    case SUB -> LiteralExpr.of(lhsFloat - rhsNumber.floatValue());
                    case MUL -> LiteralExpr.of(lhsFloat * rhsNumber.floatValue());
                    case DIV -> LiteralExpr.of(lhsFloat / rhsNumber.floatValue());
                    case REM -> LiteralExpr.of(lhsFloat % rhsNumber.floatValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
            else if (lhsNumber instanceof Double lhsDouble) {
                return switch (op) {
                    // Comparisons
                    case EQ -> LiteralExpr.of(lhsDouble == rhsNumber.doubleValue());
                    case NE -> LiteralExpr.of(lhsDouble != rhsNumber.doubleValue());
                    case LT -> LiteralExpr.of(lhsDouble < rhsNumber.doubleValue());
                    case LE -> LiteralExpr.of(lhsDouble <= rhsNumber.doubleValue());
                    case GT -> LiteralExpr.of(lhsDouble > rhsNumber.doubleValue());
                    case GE -> LiteralExpr.of(lhsDouble >= rhsNumber.doubleValue());
                    // Arithmetic operations
                    case ADD -> LiteralExpr.of(lhsDouble + rhsNumber.doubleValue());
                    case SUB -> LiteralExpr.of(lhsDouble - rhsNumber.doubleValue());
                    case MUL -> LiteralExpr.of(lhsDouble * rhsNumber.doubleValue());
                    case DIV -> LiteralExpr.of(lhsDouble / rhsNumber.doubleValue());
                    case REM -> LiteralExpr.of(lhsDouble % rhsNumber.doubleValue());
                    default -> throw new IllegalStateException(String.format(
                        "Unsupported numeric binary expression: %s %s %s",
                        lhsValue,
                        op,
                        rhsValue));
                };
            }
        }
        throw new IllegalStateException(String.format("Unsupported binary expression operands: %s %s %s",
            lhsValue,
            op,
            getRhs().evaluateAsLiteral(context, Object.class)));
    }

    public enum Op {
        ADD, SUB, MUL, DIV, REM, // Comparisons
        EQ, NE, LT, LE, GT, GE, // Logic
        LSH, RSH, URSH, AND, OR, XOR, SC_AND, SC_OR
    }
}
