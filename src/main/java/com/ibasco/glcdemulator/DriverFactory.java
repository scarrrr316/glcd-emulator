/*-
 * ========================START=================================
 * Organization: Rafael Luis Ibasco
 * Project: GLCD Simulator
 * Filename: DriverFactory.java
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
package com.ibasco.glcdemulator;

import com.ibasco.glcdemulator.emulator.GlcdEmulator;
import com.ibasco.glcdemulator.emulator.GlcdEmulatorFactory;
import com.ibasco.glcdemulator.utils.GlcdUtil;
import com.ibasco.glcdemulator.utils.PixelBuffer;
import com.ibasco.ucgdisplay.drivers.glcd.*;
import com.ibasco.ucgdisplay.drivers.glcd.enums.GlcdBusInterface;
import com.ibasco.ucgdisplay.drivers.glcd.enums.GlcdControllerType;
import com.ibasco.ucgdisplay.drivers.glcd.enums.GlcdRotation;

import java.util.List;
import java.util.function.Predicate;

/**
 * Factory class for constructing glcd drivers (virtual/non-virtual)
 *
 * @author Rafael Ibasco
 */
public class DriverFactory {

    private static final int DUMMY_I2C_ADDRESS = 0x10;

    /**
     * Creates a virtual driver based on the display parameter provided. A bus interface will be automatically be assigned.
     *
     * @param display
     *         The display controller to be used
     *
     * @return The virtual {@link GlcdDriver} instance
     *
     * @see Glcd
     */
    public static GlcdDriver createVirtual(GlcdDisplay display) {
        return createVirtual(display, GlcdUtil.findPreferredBusInterface(display), (GlcdDriverEventHandler) null);
    }

    /**
     * Creates a virtual driver with the specified bus interface
     *
     * @param display
     *         The display setup where the virtual driver will be based on.
     * @param busInterface
     *         The bus interface that will currently be used by the virtual driver. If null, the default bus interface
     *         will be selected.
     *
     * @return The virtual {@link GlcdDriver} instance
     *
     * @see Glcd
     */
    public static GlcdDriver createVirtual(GlcdDisplay display, GlcdBusInterface busInterface) {
        GlcdEmulator emulator = GlcdEmulatorFactory.createFrom(display, busInterface);
        return createVirtual(display, emulator.getBusInterface(), emulator);
    }

    public static GlcdDriver createVirtual(GlcdDisplay display, GlcdBusInterface busInterface, PixelBuffer outputBuffer) {
        GlcdEmulator emulator = GlcdEmulatorFactory.createFrom(display, busInterface, outputBuffer);
        return createVirtual(display, busInterface, emulator);
    }

    public static GlcdDriver createVirtual(GlcdControllerType controller, GlcdBusInterface busInterface, PixelBuffer outputBuffer) {
        Predicate<GlcdDisplay> filterDimensions = p -> (p.getDisplaySize().getDisplayWidth() >= outputBuffer.getWidth()) &&
                (p.getDisplaySize().getDisplayHeight() >= outputBuffer.getHeight());
        Predicate<GlcdDisplay> filterBusInterface = p -> p.hasBusInterface(busInterface);
        List<GlcdDisplay> result = GlcdUtil.findDisplayByType(controller, filterDimensions.and(filterBusInterface));
        GlcdDisplay display = null;

        //select first entry
        if (result.size() >= 1) {
            display = result.get(0);
        }

        if (display == null)
            throw new IllegalStateException("No display found from the specified criteria");

        GlcdEmulator emulator = GlcdEmulatorFactory.createFrom(display, busInterface, outputBuffer);

        return createVirtual(display, busInterface, emulator);
    }

    public static GlcdDriver createVirtual(GlcdDisplay display, GlcdBusInterface busInterface, GlcdDriverEventHandler handler) {
        GlcdConfig config = GlcdConfigBuilder
                .create()
                .display(display)
                .rotation(GlcdRotation.ROTATION_NONE)
                .busInterface(busInterface)
                .build();

        //use a dummy i2c address
        if (busInterface != null && busInterface.name().contains("I2C")) {
            config.setDeviceAddress(DUMMY_I2C_ADDRESS);
        }

        return new GlcdDriver(config, true, handler);
    }
}
