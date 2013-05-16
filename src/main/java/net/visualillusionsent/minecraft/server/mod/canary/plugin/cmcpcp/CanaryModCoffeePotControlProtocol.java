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
package net.visualillusionsent.minecraft.server.mod.canary.plugin.cmcpcp;

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;
import net.visualillusionsent.utils.UtilityException;

public final class CanaryModCoffeePotControlProtocol extends Plugin{
    private final CoffeePotController cpc;

    public CanaryModCoffeePotControlProtocol(){
        cpc = new CoffeePotController();
    }

    public final boolean enable(){
        try {
            Canary.commands().registerCommands(new ProtocolCommandListener(cpc), this, false);
            Canary.hooks().registerListener(new ProtocolListener(), this);
            return true;
        }
        catch (UtilityException uex) {
            getLogman().logStacktrace("Failed to start...", uex);
        }
        catch (CommandDependencyException cdex) {
            getLogman().logStacktrace("Failed to start...", cdex);
        }
        return false;
    }

    public final void disable(){
        cpc.cleanUp();
    }
}
