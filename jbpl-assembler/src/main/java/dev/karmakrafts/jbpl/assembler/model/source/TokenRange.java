package dev.karmakrafts.jbpl.assembler.model.source;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TokenRange(int start, int end) {
    public static final int UNDEFINED_INDEX = -1;
    public static final int SYNTHETIC_INDEX = -2;
    public static final TokenRange UNDEFINED = new TokenRange(UNDEFINED_INDEX, UNDEFINED_INDEX);
    public static final TokenRange SYNTHETIC = new TokenRange(SYNTHETIC_INDEX, SYNTHETIC_INDEX);

    public static @NotNull TokenRange fromTokens(final @NotNull Token start, final @NotNull Token end) {
        return new TokenRange(start.getTokenIndex(), end.getTokenIndex());
    }

    public static @NotNull TokenRange fromToken(final @NotNull Token token) {
        return fromTokens(token, token);
    }

    public static @NotNull TokenRange fromContext(final @NotNull ParserRuleContext ctx) {
        return fromTokens(ctx.getStart(), ctx.getStop());
    }

    public static @NotNull TokenRange fromTerminalNode(final @NotNull TerminalNode node) {
        return fromToken(node.getSymbol());
    }

    public static @NotNull TokenRange union(final @NotNull List<TokenRange> ranges) {
        var start = Integer.MAX_VALUE;
        var end = 0;
        for (final var range : ranges) {
            final var sliceStart = range.start;
            if (start > sliceStart) {
                start = sliceStart;
            }
            final var sliceEnd = range.end;
            if (end < sliceEnd || sliceEnd == UNDEFINED_INDEX || sliceEnd == SYNTHETIC_INDEX) {
                end = sliceEnd;
            }
        }
        if (start == UNDEFINED_INDEX || end == UNDEFINED_INDEX) {
            start = UNDEFINED_INDEX;
            end = UNDEFINED_INDEX;
        }
        if (start == SYNTHETIC_INDEX || end == SYNTHETIC_INDEX) {
            start = SYNTHETIC_INDEX;
            end = SYNTHETIC_INDEX;
        }
        return new TokenRange(start, end);
    }

    public boolean isUndefined() {
        return start == UNDEFINED_INDEX || end == UNDEFINED_INDEX;
    }

    public boolean isSynthetic() {
        return start == SYNTHETIC_INDEX || end == SYNTHETIC_INDEX;
    }
}
