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

package dev.karmakrafts.jbpl.assembler.box;

import dev.karmakrafts.jbpl.assembler.Assembler;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.parser.ParserException;
import dev.karmakrafts.jbpl.assembler.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public abstract class AssemblerBoxTest {
    protected final ArrayList<String> infoBuffer = new ArrayList<>();
    protected final ArrayList<String> errorBuffer = new ArrayList<>();

    protected abstract @NotNull String getFileName();

    protected void checkOutput() {
    }

    @Test
    public void invoke() throws ParserException, ValidationException, EvaluationException {
        final var assembler = Assembler.createFromResources("box/", message -> {
            infoBuffer.add(message);
            System.out.println(message);
        }, message -> {
            errorBuffer.add(message);
            System.err.println(message);
        });
        final var context = assembler.lowerAndCreateContext(getFileName(), className -> {
            final var node = new ClassNode();
            node.name = className;
            return node;
        });
        context.file.evaluate(context);
        checkOutput();
    }
}
