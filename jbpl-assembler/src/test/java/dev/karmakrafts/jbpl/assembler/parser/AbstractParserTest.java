package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import dev.karmakrafts.jbpl.frontend.JBPLParser.FileContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.BitSet;

public abstract class AbstractParserTest {
    protected ParserTestResult parse(final @NotNull String input, final @Nullable Object... args) {
        try {
            final var charStream = CharStreams.fromString(String.format(input, args), "test");
            final var lexer = new JBPLLexer(charStream);
            final var errorListener = new ErrorListener();
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            final var tokenStream = new CommonTokenStream(lexer);
            tokenStream.fill();
            if (errorListener.lastError != null) {
                return new ParserTestResult(null, errorListener.lastError);
            }
            final var parser = new JBPLParser(tokenStream);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            errorListener.lastError = null; // Reset last error
            final var fileContext = parser.file();
            if (errorListener.lastError != null) {
                return new ParserTestResult(null, errorListener.lastError);
            }
            return new ParserTestResult(fileContext, null);
        }
        catch (Throwable error) {
            return new ParserTestResult(null, error);
        }
    }

    private static final class ErrorListener implements ANTLRErrorListener {
        public RecognitionException lastError;

        @Override
        public void syntaxError(final @NotNull Recognizer<?, ?> recognizer,
                                final @NotNull Object offendingSymbol,
                                final int line,
                                final int charPositionInLine,
                                final @NotNull String msg,
                                final @NotNull RecognitionException e) {
            if (lastError != null) {
                return;
            }
            lastError = e;
        }

        @Override
        public void reportAmbiguity(final @NotNull Parser recognizer,
                                    final @NotNull DFA dfa,
                                    final int startIndex,
                                    final int stopIndex,
                                    final boolean exact,
                                    final @NotNull BitSet ambigAlts,
                                    final @NotNull ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(final @NotNull Parser recognizer,
                                                final @NotNull DFA dfa,
                                                final int startIndex,
                                                final int stopIndex,
                                                final @NotNull BitSet conflictingAlts,
                                                final @NotNull ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(final @NotNull Parser recognizer,
                                             final @NotNull DFA dfa,
                                             final int startIndex,
                                             final int stopIndex,
                                             final int prediction,
                                             final @NotNull ATNConfigSet configs) {
        }
    }

    protected record ParserTestResult(@Nullable FileContext context, @Nullable Throwable error) {
        public void shouldFail() {
            if (context != null) {
                Assertions.fail("Parsing succeeded but was expected to fail");
            }
        }

        public void shouldSucceed() {
            if (error != null) {
                Assertions.fail(error);
            }
        }
    }
}
