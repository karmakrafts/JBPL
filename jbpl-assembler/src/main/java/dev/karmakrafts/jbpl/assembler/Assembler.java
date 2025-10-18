package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.lower.CompoundLowering;
import dev.karmakrafts.jbpl.assembler.lower.IncludeLowering;
import dev.karmakrafts.jbpl.assembler.lower.NoopRemovalLowering;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.parser.ElementParser;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public final class Assembler {
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

    private @NotNull AssemblerContext lower(final @NotNull AssemblyFile file) {
        final var context = new AssemblerContext(file);
        file.transform(includeLowering);
        file.transform(CompoundLowering.INSTANCE);
        file.transform(NoopRemovalLowering.INSTANCE);
        return context;
    }

    public @NotNull AssemblerContext getOrParseAndLowerFile(final @NotNull String path) {
        return lower(getOrParseFile(path));
    }
}
