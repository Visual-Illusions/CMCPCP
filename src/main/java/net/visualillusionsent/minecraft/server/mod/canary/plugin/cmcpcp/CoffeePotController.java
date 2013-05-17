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

import java.util.Timer;
import net.canarymod.Canary;
import net.visualillusionsent.utils.PropertiesFile;

public final class CoffeePotController{
    private final PropertiesFile settings;
    private boolean isBrewing;
    private Timer brew;

    public CoffeePotController(){
        this.settings = new PropertiesFile("config/CanaryModCoffeePotControlProtocol/settings.cfg");
        if (settings.getPropertiesMap().isEmpty()) {
            settings.getInt("coffeepot.size", 12);
            settings.addComment("coffeepot.size", "The number of cups the CoffeePot holds");
            settings.getInt("brew.time", 120);
            settings.addComment("brew.time", "The time in seconds to take to brew coffee");
            settings.getInt("coffeepot.dirt.value", 0);
            settings.addComment("coffeepot.dirt.value", "DO NOT EDIT DIRT VALUE");
            settings.getInt("coffeepot.level", 0);
            settings.addComment("coffeepot.level", "DO NOT EDIT LEVEL VALUE");
            settings.save();
        }
        brew = new Timer();
    }

    final int reportedPotSize(){
        return settings.getInt("coffeepot.size");
    }

    final int getBrewTime(){
        return settings.getInt("brew.time");
    }

    final int reportedDirtLevel(){
        return settings.getInt("coffeepot.dirt.value");
    }

    final void addDirt(){
        settings.setInt("coffeepot.dirt.value", reportedDirtLevel() + 1);
        settings.save();
    }

    final void clearDirt(){
        settings.setInt("coffeepot.dirt.value", 0);
        settings.setInt("coffeepot.level", 0);
        settings.save();
    }

    final boolean brewCoffee(){
        if (!isBrewing) {
            addDirt();
            brew.schedule(new BrewCoffeeTask(this), getBrewTime() * 1000);
            isBrewing = true;
            Canary.getServer().broadcastMessage("§6[CMCPCP] §bCoffee is now being brewed!");
            Canary.logNotice("[CMCPCP] Coffee is now being brewed!");
            return true;
        }
        return false;
    }

    final boolean reportBrewing(){
        return isBrewing;
    }

    final int reportedCoffeeLevel(){
        return settings.getInt("coffeepot.level");
    }

    final void takeCup(){
        settings.setInt("coffeepot.level", (reportedCoffeeLevel() - 1));
        settings.save();
    }

    final void done(){
        isBrewing = false;
        settings.setInt("coffeepot.level", reportedPotSize());
        settings.save();
    }

    final void cleanUp(){
        brew.cancel();
        brew.purge();
    }
}