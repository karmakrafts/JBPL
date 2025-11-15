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

package dev.karmakrafts.jbpl.intellij.structview;

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import dev.karmakrafts.jbpl.intellij.JBPLFile;
import dev.karmakrafts.jbpl.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JBPLStructureViewModel extends TextEditorBasedStructureViewModel implements ElementInfoProvider {
    private static final Sorter[] SORTERS = new Sorter[]{Sorter.ALPHA_SORTER};
    private final JBPLStructureViewElement rootView;

    public JBPLStructureViewModel(final @Nullable Editor editor, final @NotNull JBPLFile root) {
        super(editor, root);
        rootView = new JBPLStructureViewElement(root);
    }

    @Override
    public @NotNull StructureViewTreeElement getRoot() {
        return rootView;
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return SORTERS;
    }

    @Override
    public boolean isAlwaysShowsPlus(final @NotNull StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(final @NotNull StructureViewTreeElement element) {
        return false;
    }

    @Override
    protected Class<?> @NotNull [] getSuitableClasses() {
        return new Class<?>[]{ // @formatter:off
            DefineNode.class,
            MacroNode.class,
            FieldNode.class,
            FunctionNode.class,
            InjectorNode.class,
            PreproClassNode.class,
            YeetNode.class,
            ClassNode.class
        }; // @formatter:on
    }
}
