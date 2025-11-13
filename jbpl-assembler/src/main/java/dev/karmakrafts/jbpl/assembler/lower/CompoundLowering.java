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

package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.Declaration;
import dev.karmakrafts.jbpl.assembler.model.decl.FunctionDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.InjectorDecl;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.expr.CompoundExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.CompoundStatement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Lifts all elements from a {@link CompoundStatement} or {@link CompoundExpr} into its
 * parent {@link ElementContainer}.
 * Mostly to reduce overhead when traversing the tree.
 */
public final class CompoundLowering implements ElementVisitor {
    public static final CompoundLowering INSTANCE = new CompoundLowering();

    private CompoundLowering() {
    }

    @SuppressWarnings("all")
    private <C extends ElementContainer> @NotNull C expandCompounds(final @NotNull C container) {
        final var newElements = new ArrayList<Element>();
        for (final var element : container.getElements()) {
            if (element instanceof CompoundStatement statement) {
                newElements.addAll(statement.getElements());
                continue;
            }
            newElements.add(element);
        }
        container.clearElements();
        container.addElementsVerbatim(newElements); // We don't want to override the original parent to retain source info
        return container;
    }

    @Override
    public @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        return expandCompounds(file);
    }

    @Override
    public @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        return expandCompounds(functionDecl);
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        return expandCompounds(macroDecl);
    }

    @Override
    public @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        return expandCompounds(injectorDecl);
    }
}
