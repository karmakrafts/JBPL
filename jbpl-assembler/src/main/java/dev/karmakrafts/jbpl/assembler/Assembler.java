package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.lower.CompoundLowering;
import dev.karmakrafts.jbpl.assembler.lower.IncludeLowering;
import dev.karmakrafts.jbpl.assembler.lower.NoopRemovalLowering;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.parser.ElementParser;
import dev.karmakrafts.jbpl.assembler.parser.ParserException;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.validation.ValidationException;
import dev.karmakrafts.jbpl.assembler.validation.VersionValidationVisitor;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Assembler {
    private static final Set<Integer> BYTECODE_VERSIONS = Set.of(Opcodes.V1_1,
        Opcodes.V1_2,
        Opcodes.V1_3,
        Opcodes.V1_4,
        Opcodes.V1_5,
        Opcodes.V1_6,
        Opcodes.V1_7,
        Opcodes.V1_8,
        Opcodes.V9,
        Opcodes.V10,
        Opcodes.V11,
        Opcodes.V12,
        Opcodes.V13,
        Opcodes.V14,
        Opcodes.V15,
        Opcodes.V16,
        Opcodes.V17,
        Opcodes.V18,
        Opcodes.V19,
        Opcodes.V20,
        Opcodes.V21,
        Opcodes.V22,
        Opcodes.V23,
        Opcodes.V24,
        Opcodes.V25);
    @SuppressWarnings("deprecation")
    private static final Set<Integer> BYTECODE_API_VERSIONS = Set.of(Opcodes.ASM4,
        Opcodes.ASM5,
        Opcodes.ASM6,
        Opcodes.ASM7,
        Opcodes.ASM8,
        Opcodes.ASM9,
        Opcodes.ASM10_EXPERIMENTAL);

    static {
        AnsiConsole.systemInstall(); // Make sure we have ANSI support enabled for all standard outputs
    }

    private final Function<String, ReadableByteChannel> resourceProvider;
    private final Consumer<String> infoConsumer;
    private final Consumer<String> errorConsumer;
    private final HashMap<String, AssemblyFile> files = new HashMap<>();
    private final IncludeLowering includeLowering = new IncludeLowering(this);

    public Assembler(final @NotNull Function<String, ReadableByteChannel> resourceProvider,
                     final @NotNull Consumer<String> infoConsumer,
                     final @NotNull Consumer<String> errorConsumer) {
        this.resourceProvider = resourceProvider;
        this.infoConsumer = infoConsumer;
        this.errorConsumer = errorConsumer;
    }

    @SuppressWarnings("all")
    public static Assembler createFromResources(final @NotNull String basePath,
                                                final @NotNull Consumer<String> infoConsumer,
                                                final @NotNull Consumer<String> errorConsumer) {
        return new Assembler(path -> {
            final var stream = Assembler.class.getResourceAsStream(String.format("%s/%s", basePath, path));
            return Channels.newChannel(stream);
        }, infoConsumer, errorConsumer);
    }

    public static Assembler createFromResources(final @NotNull String basePath) {
        return createFromResources(basePath, System.out::println, System.err::println);
    }

    public @NotNull AssemblyFile getOrParseFile(final @NotNull String path) throws ParserException {
        try {
            return files.computeIfAbsent(path, p -> {
                try (final var channel = resourceProvider.apply(path)) {
                    final var file = new AssemblyFile(path);
                    final var errorListener = new ErrorListener(file);
                    final var charStream = CharStreams.fromChannel(channel, 4096, CodingErrorAction.REPLACE, path);
                    final var lexer = new JBPLLexer(charStream);
                    lexer.removeErrorListeners();
                    lexer.addErrorListener(errorListener);
                    final var tokenStream = new CommonTokenStream(lexer);
                    tokenStream.fill();
                    file.source.addAll(tokenStream.getTokens());
                    final var parser = new JBPLParser(tokenStream);
                    parser.removeErrorListeners();
                    parser.addErrorListener(errorListener);
                    // @formatter:off
                    file.addElements(parser.file().bodyElement().stream()
                        .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                        .toList());
                    // @formatter:on
                    file.updateChildParents(); // Update all parent references recursively
                    return file;
                }
                catch (IOException error) {
                    throw new RuntimeException(error);
                }
            });
        }
        catch (SyntaxError error) {
            throw error.cause; // Syntax errors get unwrapped and rethrown as ParserException
        }
    }

    private void validateFile(final @NotNull AssemblyFile file) throws ValidationException {
        file.accept(new VersionValidationVisitor());
    }

    private void validatePostLowering(final @NotNull EvaluationContext context) throws ValidationException {
        validateBytecodeVersion(context);
    }

    private void validateBytecodeVersion(final @NotNull EvaluationContext context) throws ValidationException {
        final var version = context.bytecodeVersion;
        if (!BYTECODE_VERSIONS.contains(version)) {
            final var message = String.format("%d is not a valid class file version", version);
            throw new ValidationException(message, null, context.createStackTrace());
        }
    }

    private @NotNull EvaluationContext lower(final @NotNull AssemblyFile file,
                                             final @NotNull Function<String, ClassNode> classResolver) throws ValidationException {
        validateFile(file);
        file.transform(includeLowering);
        file.transform(CompoundLowering.INSTANCE);
        file.transform(NoopRemovalLowering.INSTANCE);
        final var context = new EvaluationContext(file, classResolver, infoConsumer, errorConsumer);
        validatePostLowering(context);
        return context;
    }

    public @NotNull EvaluationContext getOrParseAndLowerFile(final @NotNull String path,
                                                             final @NotNull Function<String, ClassNode> classResolver) throws ValidationException, ParserException {
        return lower(getOrParseFile(path), classResolver);
    }

    private static final class SyntaxError extends RuntimeException {
        public final ParserException cause;

        public SyntaxError(ParserException cause) {
            super(cause);
            this.cause = cause;
        }
    }

    private record ErrorListener(AssemblyFile file) implements ANTLRErrorListener {
        private ErrorListener(final @NotNull AssemblyFile file) {
            this.file = file;
        }

        @Override
        public void syntaxError(final @NotNull Recognizer<?, ?> recognizer,
                                final @Nullable Object offendingSymbol,
                                final int line,
                                final int charPositionInLine,
                                final @NotNull String msg,
                                final @NotNull RecognitionException e) {
            final var token = (Token) offendingSymbol;
            if (token == null) { // TODO: improve this..
                throw new SyntaxError(new ParserException(msg, e, null));
            }
            if (e instanceof NoViableAltException noViableAltException) {
                throw new SyntaxError(new ParserException("Syntax error",
                    e,
                    SourceDiagnostic.from(file, noViableAltException.getStartToken(), msg)));
            }
            throw new SyntaxError(new ParserException("Syntax error", e, SourceDiagnostic.from(file, token, msg)));
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
}
