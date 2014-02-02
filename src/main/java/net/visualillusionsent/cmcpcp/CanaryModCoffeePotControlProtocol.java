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

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;
import net.visualillusionsent.utils.UtilityException;

import java.util.logging.Logger;

/** @author Jason (darkdiplomat) */
public final class CanaryModCoffeePotControlProtocol extends VisualIllusionsCanaryPlugin {
    private CoffeePotController cpc;

    @Override
    public final boolean enable() {
        try {
            super.enable();
            this.cpc = new CoffeePotController(this);
            new ProtocolCommandListener(this);
            Canary.hooks().registerListener(new ProtocolListener(), this);
            return true;
        }
        catch (UtilityException uex) {
            getLogman().error("Failed to initialize...", uex);
        }
        catch (CommandDependencyException cdex) {
            getLogman().error("Failed to initialize...", cdex);
        }
        return false;
    }

    @Override
    public final void disable() {
        cpc.cleanUp();
    }

    final CoffeePotController getController() {
        return cpc;
    }
}
