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

package dev.karmakrafts.jbpl.intellij.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class KeywordCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final LookupElement VOID = LookupElementBuilder.create("void");
    public static final LookupElement I8 = LookupElementBuilder.create("i8");
    public static final LookupElement I16 = LookupElementBuilder.create("i16");
    public static final LookupElement I32 = LookupElementBuilder.create("i32");
    public static final LookupElement I64 = LookupElementBuilder.create("i64");
    public static final LookupElement F32 = LookupElementBuilder.create("f32");
    public static final LookupElement F64 = LookupElementBuilder.create("f64");
    public static final LookupElement BOOL = LookupElementBuilder.create("bool");
    public static final LookupElement CHAR = LookupElementBuilder.create("char");
    public static final LookupElement STRING = LookupElementBuilder.create("string");
    public static final LookupElement TYPE = LookupElementBuilder.create("type");
    public static final LookupElement TYPEOF = LookupElementBuilder.create("typeof");
    public static final LookupElement OPCODE = LookupElementBuilder.create("opcode");
    public static final LookupElement OPCODEOF = LookupElementBuilder.create("opcodeof");
    public static final LookupElement INSTRUCTION = LookupElementBuilder.create("instruction");
    public static final LookupElement SIGNATURE = LookupElementBuilder.create("signature");

    public static final LookupElement PRIVATE = LookupElementBuilder.create("private");
    public static final LookupElement PROTECTED = LookupElementBuilder.create("protected");
    public static final LookupElement PUBLIC = LookupElementBuilder.create("public");
    public static final LookupElement STATIC = LookupElementBuilder.create("static");
    public static final LookupElement SYNC = LookupElementBuilder.create("sync");

    public static final LookupElement FUN = LookupElementBuilder.create("fun");
    public static final LookupElement FIELD = LookupElementBuilder.create("field");
    public static final LookupElement SELECTOR = LookupElementBuilder.create("selector");
    public static final LookupElement INJECT = LookupElementBuilder.create("inject");

    public static final LookupElement DEFINE = LookupElementBuilder.create("define");
    public static final LookupElement INCLUDE = LookupElementBuilder.create("include");
    public static final LookupElement CLASS = LookupElementBuilder.create("class");
    public static final LookupElement RETURN = LookupElementBuilder.create("return");
    public static final LookupElement MACRO = LookupElementBuilder.create("macro");
    public static final LookupElement INFO = LookupElementBuilder.create("info");
    public static final LookupElement ERROR = LookupElementBuilder.create("error");
    public static final LookupElement ASSERT = LookupElementBuilder.create("assert");

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final @NotNull ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        result.addAllElements(List.of(VOID,
            I8,
            I16,
            I32,
            I64,
            F32,
            F64,
            BOOL,
            CHAR,
            STRING,
            TYPE,
            TYPEOF,
            OPCODE,
            OPCODEOF,
            INSTRUCTION,
            SIGNATURE));
        result.addAllElements(List.of(PRIVATE, PROTECTED, PUBLIC, STATIC, SYNC));
        result.addAllElements(List.of(FUN, FIELD, SELECTOR, INJECT));
        result.addAllElements(List.of(DEFINE, INCLUDE, CLASS, RETURN, MACRO, INFO, ERROR, ASSERT));
    }
}
