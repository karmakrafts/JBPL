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

package dev.karmakrafts.jbpl.assembler.model.type;

public enum TypeCategory {
    // @formatter:off
    VOID,
    INTEGER,
    FLOAT,
    BOOL,
    CHAR,
    ARRAY,
    STRING,
    OBJECT,
    INTERSECTION,
    PREPROCESSOR;
    // @formatter:on

    public boolean isMaterializable() {
        return this != INTERSECTION && this != PREPROCESSOR;
    }
}
