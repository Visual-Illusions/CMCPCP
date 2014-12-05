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
import net.canarymod.chat.MessageReceiver;
import net.canarymod.logger.Logman;
import net.visualillusionsent.utils.PropertiesFile;

import java.util.concurrent.ScheduledFuture;

import static net.visualillusionsent.utils.TaskManager.scheduleDelayedTaskInMinutes;

/** @author Jason (darkdiplomat) */
final class CoffeePotController {
    private final Logman logman;
    private final PropertiesFile settings;
    private final ProtocolTranslator translator;
    private boolean power, cold;
    private ScheduledFuture coldtask, brewtask, cleantask;

    public CoffeePotController(CanaryModCoffeePotControlProtocol cmcpcp) {
        this.logman = cmcpcp.getLogman();
        this.settings = new PropertiesFile("config/CanaryModCoffeePotControlProtocol/settings.cfg");
        settings.getBoolean("update.lang", true);
        settings.setComments("update.lang", "Whether to allow auto-updating of the default lang files");
        settings.getString("server.locale", "en_US");
        settings.setComments("server.locale", "The default locale to use in messages");
        settings.getInt("coffeepot.size", 12);
        settings.setComments("coffeepot.size", "The number of cups the CoffeePot holds");
        settings.getString("server.locale", "en_US");
        settings.setComments("server.locale", "The default locale for message");
        settings.getInt("coffeepot.dirt.value", 0);
        settings.setComments("coffeepot.dirt.value", "DO NOT EDIT DIRT VALUE");
        settings.getInt("coffeepot.level", 0);
        settings.setComments("coffeepot.level", "DO NOT EDIT LEVEL VALUE");
        settings.save();
        translator = new ProtocolTranslator(cmcpcp, settings.getString("server.locale"), settings.getBoolean("update.lang"));
    }

    final int reportedPotSize() {
        return settings.getInt("coffeepot.size");
    }

    final int getBrewTime() {
        return reportedPotSize() * 45 / 60;
    }

    final int reportedDirtLevel() {
        return settings.getInt("coffeepot.dirt.value");
    }

    final void addDirt() {
        settings.setInt("coffeepot.dirt.value", reportedDirtLevel() + 1);
        settings.save();
    }

    final void clearDirt() {
        settings.setInt("coffeepot.dirt.value", 0);
        settings.setInt("coffeepot.level", 0);
        settings.save();
        informAll("pot.cleaned");
    }

    final boolean startCleaningCycle() {
        if (cleantask == null || cleantask.isDone()) {
            cleantask = scheduleDelayedTaskInMinutes(new CleanTask(this), 3);
            return true;
        }
        return false;
    }

    final boolean reportCleaning() {
        return cleantask != null && !cleantask.isDone();
    }

    final boolean brewCoffee() {
        if (brewtask == null || brewtask.isDone()) {
            addDirt();
            brewtask = scheduleDelayedTaskInMinutes(new BrewCoffeeTask(this), getBrewTime());
            informAll("status.200");
            informAll("coffee.brewing");
            return true;
        }
        return false;
    }

    final boolean reportBrewing() {
        return brewtask != null && !brewtask.isDone();
    }

    final boolean reportPower() {
        return power;
    }

    final void togglePower() {
        power = !power;
        if (brewtask != null && !brewtask.isDone()) {
            brewtask.cancel(true);
            settings.setInt("coffeepot.level", reportedPotSize() / 2);
        }
        if (coldtask != null && !coldtask.isDone()) {
            coldtask.cancel(true);
        }
        coldtask = scheduleDelayedTaskInMinutes(new ChillTask(this, !power), power ? 5 : 15);
    }

    final void setCold(boolean cold) {
        this.cold = cold;
    }

    final boolean isCold() {
        return this.cold;
    }

    final int reportedCoffeeLevel() {
        return settings.getInt("coffeepot.level");
    }

    final void takeCup() {
        settings.setInt("coffeepot.level", (reportedCoffeeLevel() - 1));
        settings.save();
    }

    final void done() {
        informAll("coffee.brewed");
        if (coldtask != null && !coldtask.isDone()) {
            coldtask.cancel(true);
        }
        cold = false;
        settings.setInt("coffeepot.level", reportedPotSize());
        settings.save();
    }

    final void cleanUp() {
        if (brewtask != null && !brewtask.isDone()) {
            brewtask.cancel(true);
        }
        if (coldtask != null && !coldtask.isDone()) {
            coldtask.cancel(true);
        }
        if (cleantask != null && !cleantask.isDone()) {
            cleantask.cancel(true);
        }
    }

    final void informAll(String key, Object... args) {
        String msg = translator.translate(key, settings.getString("server.locale"), args);
        Canary.getServer().broadcastMessage(msg);
        logman.info(msg);
    }

    final void inform(MessageReceiver msgrec, String key, Object... args) {
        msgrec.message(translator.translate(key, msgrec.getLocale(), args));
    }

    final void reload() {
        settings.reload();
    }
}
