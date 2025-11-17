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

package dev.karmakrafts.jbpl.assembler.eval;

public final class ControlFlowState {
    // @formatter:off
    public static final byte MASK_NONE      = 0b0000_0000;
    public static final byte MASK_RETURN    = 0b0000_0001;
    public static final byte MASK_CONTINUE  = 0b0000_0010;
    public static final byte MASK_BREAK     = 0b0000_0100;
    // @formatter:on

    private byte mask = MASK_NONE;

    public byte getMask() {
        return mask;
    }

    public boolean clearReturnMask() {
        final var result = hasRet();
        mask = MASK_NONE;
        return result;
    }

    public boolean hasCnt() {
        return (mask & MASK_CONTINUE) != 0;
    }

    public boolean clearCnt() {
        final var result = mask;
        mask &= ~MASK_CONTINUE;
        return (result & MASK_CONTINUE) != 0;
    }

    public void cnt() {
        mask |= MASK_CONTINUE;
    }

    public boolean hasBrk() {
        return (mask & MASK_BREAK) != 0;
    }

    public boolean clearBrk() {
        final var result = mask;
        mask &= ~MASK_BREAK;
        return (result & MASK_BREAK) != 0;
    }

    public void brk() {
        mask |= MASK_BREAK;
    }

    public boolean hasRet() {
        return (mask & MASK_RETURN) != 0;
    }

    public boolean clearRet() {
        final var result = mask;
        mask &= ~MASK_RETURN;
        return (result & MASK_RETURN) != 0;
    }

    public void ret() {
        mask |= MASK_RETURN;
    }
}
