/*
 * This file is part of CanaryModCoffeePotControlProtocol.
 *
 * Copyright © 2011-2014 Visual Illusions Entertainment
 *
 * CanaryModCoffeePotControlProtocol is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.cmcpcp;

/**
 * @author Jason Jones (darkdiplomat)
 */
final class CleanTask implements Runnable {
    private final CoffeePotController controller;

    CleanTask(CoffeePotController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.clearDirt();
    }
}
