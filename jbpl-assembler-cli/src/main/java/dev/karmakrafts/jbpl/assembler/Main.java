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

package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
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

    static void main(final @NotNull String[] args) {
        try {
            final var assembler = new Assembler(ExceptionUtils.unsafeFunction(Main::readFile),
                System.out::println,
                System.err::println);
            final var context = assembler.getOrParseAndLowerFile(args[0], name -> new ClassNode());
            final var output = context.output.values();
            for (final var clazz : output) {
                if (clazz == null) {
                    continue;
                }
                final var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                clazz.accept(writer);
                final var outputPath = String.format("%s.class", clazz.name);
                Files.write(Path.of(outputPath),
                    writer.toByteArray(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE);
            }
        }
        catch (Throwable error) {
            System.err.println(error.getMessage());
        }
    }
}
