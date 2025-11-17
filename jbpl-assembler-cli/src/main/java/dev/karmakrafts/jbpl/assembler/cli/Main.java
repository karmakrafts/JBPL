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

package dev.karmakrafts.jbpl.assembler.cli;

import dev.karmakrafts.jbpl.assembler.Assembler;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import joptsimple.OptionParser;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Main {
    private static @NotNull ReadableByteChannel readFile(final @NotNull String path) throws IOException {
        return Files.newByteChannel(Path.of(path));
    }

    public static void main(final @NotNull String[] args) {
        if (args.length == 0) {
            System.out.println("Assembler requires at least an input file to be specified.");
            System.out.println("Run 'jbpl --help' to get more information.");
            return;
        }
        final var optionParser = new OptionParser(true);
        // @formatter:off
        final var helpSpec = optionParser.accepts("help", "Display this help summary");
        final var inputSpec = optionParser.accepts("input", "A path to the input file to be assembled")
            .availableUnless(helpSpec)
            .withRequiredArg()
            .ofType(String.class);
        final var outputSpec = optionParser.accepts("output", "A path to the output directory")
            .availableUnless(helpSpec)
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo("");
        // @formatter:on
        final var options = optionParser.parse(args);
        if (options.has(helpSpec)) {
            try {
                optionParser.printHelpOn(System.out);
            }
            catch (Throwable error) {
                System.err.println(error.getMessage());
            }
            return;
        }
        try {
            final var assembler = new Assembler(ExceptionUtils.unsafeFunction(Main::readFile),
                System.out::println,
                System.err::println);
            final var context = assembler.lowerAndCreateContext(options.valueOf(inputSpec), name -> new ClassNode());
            context.eval();
            final var output = context.output.values();
            for (final var clazz : output) {
                if (clazz == null) {
                    continue;
                }
                final var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                clazz.accept(writer);
                final var outputDirPath = Path.of(options.valueOf(outputSpec));
                Files.createDirectories(outputDirPath);
                final var outputPath = outputDirPath.resolve(String.format("%s.class", clazz.name));
                Files.write(outputPath, writer.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            }
        }
        catch (Throwable error) {
            System.err.println(error.getMessage());
        }
    }
}
