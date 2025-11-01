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

package dev.karmakrafts.jbpl.assembler.source;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_METHOD)
public final class SourceRangeTest {
    @Test
    public void contains() {
        final var range1 = new SourceRange(0, 0, 2, 10);
        final var range2 = new SourceRange(1, 10, 1, 20);
        Assertions.assertTrue(range1.contains(range2));
        Assertions.assertFalse(range2.contains(range1));
    }

    @Test
    public void containsLine() {
        final var range = new SourceRange(0, 0, 100, 100);
        Assertions.assertFalse(range.containsLine(-1));
        Assertions.assertTrue(range.containsLine(0));
        Assertions.assertTrue(range.containsLine(100));
        Assertions.assertFalse(range.containsLine(101));
    }

    @Test
    public void containsColumn() {
        final var range = new SourceRange(0, 0, 100, 100);
        Assertions.assertFalse(range.containsColumn(-1, 0));
        Assertions.assertFalse(range.containsColumn(0, -1));
        Assertions.assertTrue(range.containsColumn(0, 0));
        Assertions.assertTrue(range.containsColumn(50, 0));
        Assertions.assertTrue(range.containsColumn(50, 50));
        Assertions.assertTrue(range.containsColumn(100, 100));
        Assertions.assertFalse(range.containsColumn(100, 101));
        Assertions.assertFalse(range.containsColumn(101, 100));
    }
}
