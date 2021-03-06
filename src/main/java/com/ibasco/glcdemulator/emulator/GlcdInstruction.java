/*-
 * ========================START=================================
 * Organization: Rafael Luis Ibasco
 * Project: GLCD Simulator
 * Filename: GlcdInstruction.java
 * 
 * ---------------------------------------------------------
 * %%
 * Copyright (C) 2018 - 2019 Rafael Luis Ibasco
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================END==================================
 */
package com.ibasco.glcdemulator.emulator;

import com.ibasco.glcdemulator.utils.ByteUtils;

abstract public class GlcdInstruction<T extends GlcdInstructionFlag> {

    private byte value;

    private T flag;

    public GlcdInstruction(T flag, byte value) {
        this.value = value;
        this.flag = flag;
    }

    public byte getValue() {
        return value;
    }

    public T getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GlcdInstruction{");
        sb.append("value=").append(ByteUtils.toHexString(value));
        sb.append(", flag=").append(ByteUtils.toHexString((byte) flag.getCode()));
        sb.append('}');
        return sb.toString();
    }
}
