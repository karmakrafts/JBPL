package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.lower.CompoundLowering;
import dev.karmakrafts.jbpl.assembler.lower.IncludeLowering;
import dev.karmakrafts.jbpl.assembler.lower.NoopRemovalLowering;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.parser.ElementParser;
import dev.karmakrafts.jbpl.assembler.validation.ValidationException;
import dev.karmakrafts.jbpl.assembler.validation.VersionValidationVisitor;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

public final class Assembler {
    public static final Set<Integer> BYTECODE_VERSIONS = Set.of(Opcodes.V1_1,
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

    private final Function<String, ReadableByteChannel> resourceProvider;
    private final HashMap<String, AssemblyFile> files = new HashMap<>();
    private final IncludeLowering includeLowering = new IncludeLowering(this);

    public Assembler(final @NotNull Function<String, ReadableByteChannel> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @SuppressWarnings("all")
    public static Assembler createFromResources(final @NotNull String basePath) {
        return new Assembler(path -> {
            final var stream = Assembler.class.getResourceAsStream(String.format("%s/%s", basePath, path));
            return Channels.newChannel(stream);
        });
    }

    public @NotNull AssemblyFile getOrParseFile(final @NotNull String path) {
        return files.computeIfAbsent(path, p -> {
            try (final var channel = resourceProvider.apply(path)) {
                final var charStream = CharStreams.fromChannel(channel, 4096, CodingErrorAction.REPLACE, path);
                final var lexer = new JBPLLexer(charStream);
                final var tokenStream = new CommonTokenStream(lexer);
                tokenStream.fill();
                final var source = new ArrayList<>(tokenStream.getTokens());
                final var parser = new JBPLParser(tokenStream);
                final var file = new AssemblyFile(path, source);
                // @formatter:off
                file.addElements(parser.file().bodyElement().stream()
                    .map(ElementParser::parse)
                    .toList());
                // @formatter:on
                return file;
            }
            catch (IOException error) {
                throw new RuntimeException(error);
            }
        });
    }

    private void validatePreLowering(final @NotNull AssemblerContext context) throws ValidationException {
        final var file = context.file;
        file.accept(new VersionValidationVisitor());
    }

    private void validatePostLowering(final @NotNull AssemblerContext context) throws ValidationException {
        validateBytecodeVersion(context);
    }

    private void validateBytecodeVersion(final @NotNull AssemblerContext context) throws ValidationException {
        final var version = context.bytecodeVersion;
        if (!BYTECODE_VERSIONS.contains(version)) {
            final var message = String.format("%d is not a valid class file version", version);
            throw new ValidationException(message, (AssemblyFile) null, null);
        }
    }

    private @NotNull AssemblerContext lower(final @NotNull AssemblyFile file,
                                            final @NotNull Function<String, ClassNode> classResolver) throws ValidationException {
        final var context = new AssemblerContext(file, classResolver);
        validatePreLowering(context);
        file.transform(includeLowering);
        file.transform(new CompoundLowering(context));
        file.transform(NoopRemovalLowering.INSTANCE);
        validatePostLowering(context);
        return context;
    }

    public @NotNull AssemblerContext getOrParseAndLowerFile(final @NotNull String path,
                                                            final @NotNull Function<String, ClassNode> classResolver) throws ValidationException {
        return lower(getOrParseFile(path), classResolver);
    }
}
