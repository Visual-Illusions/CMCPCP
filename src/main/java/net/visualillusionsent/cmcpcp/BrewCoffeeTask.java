/*
 * CanaryMod Coffee Pot Control Protocol v3.x
 * Copyright (C) 2011-2013 Visual Illusions Entertainment
 *
 * Author: Jason Jones (darkdiplomat) <darkdiplomat@visualillusionsent.net>
 *
 * This Program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl.html
 */
package net.visualillusionsent.cmcpcp;

import java.util.TimerTask;

import net.canarymod.Canary;

public final class BrewCoffeeTask extends TimerTask {
    private final CoffeePotController controller;

    public BrewCoffeeTask(CoffeePotController controller) {
        this.controller = controller;
    }

    public final void run() {
        Canary.getServer().broadcastMessage("§6[CMCPCP] §2Coffee is done brewing!");
        Canary.logInfo("[CMCPCP] Coffee is done brewing!");
        controller.done();
    }
}
