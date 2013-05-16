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
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;
import net.visualillusionsent.utils.ProgramStatus;
import net.visualillusionsent.utils.VersionChecker;

public final class ProtocolCommandListener implements CommandListener{
    private final CoffeePotController controller;
    private final VersionChecker check;

    public ProtocolCommandListener(CoffeePotController controller){
        this.controller = controller;
        check = new VersionChecker("CanaryModCoffeePotControlProtocol", "3.0", "0", "http://visualillusionsent.net/minecraft/plugins/", ProgramStatus.STABLE, false);
    }

    @Command(aliases = { "cmcpcp" },
            description = "CanaryModCoffeePotControlProtocol command",
            permissions = { "cmcpcp.main" },
            toolTip = "/cmcpcp <brew|get|clean|check>")
    public final void ProtocolCommand(MessageReceiver msgrec, String[] args){
        msgrec.message("§6[CMCPCP] §eCanary Mod Coffee Pot Control Protocol Version 3.0");
        if (check.isLatest() == null || check.isLatest()) {
            msgrec.message("§6[CMCPCP] §bSoftware is UP TO DATE");
        }
        else {
            msgrec.message("§6[CMCPCP] §bYour software needs an upgrade to §ev" + check.getCurrentVersion());
        }
    }

    @Command(aliases = { "brew" },
            description = "CMCPCP BREW Command",
            permissions = { "cmcpcp.brew" },
            toolTip = "/cmcpcp brew",
            parent = "cmcpcp")
    public final void brewCommand(MessageReceiver msgrec, String[] args){
        if (controller.reportedCoffeeLevel() <= 0) {
            if (controller.brewCoffee()) {
                if (controller.reportedDirtLevel() >= 5) {
                    Canary.getServer().broadcastMessage("§6[CMCPCP] §cThe coffee pot is dirty and needs to be cleaned!");
                }
            }
            else {
                msgrec.message("§6[CMCPCP] §cERROR 400 BAD REQUEST");
                msgrec.message("§cBrewing already in progress.");
            }
        }
        else {
            msgrec.message("§6[CMCPCP] §cERROR 503 SERVICE UNAVAILIBLE");
            msgrec.message("§cCoffee has already been brewed.");
        }
    }

    @Command(aliases = { "get" },
            description = "CMCPCP GET Command",
            permissions = { "cmcpcp.get" },
            toolTip = "/cmcpcp get",
            parent = "cmcpcp")
    public final void getCommand(MessageReceiver msgrec, String[] args){
        if (controller.reportBrewing()) {
            msgrec.message("§6[CMCPCP] §cERROR 503 SERVICE UNAVAILIBLE");
            msgrec.message("§cCoffee is being brewed.");
            return;
        }
        else if (controller.reportedCoffeeLevel() <= 0) {
            msgrec.message("§6[CMCPCP] §cERROR 400 BAD REQUEST");
            msgrec.message("§cOut of Coffee.");
            return;
        }
        if (msgrec instanceof Player) {
            controller.takeCup();
            Player player = (Player) msgrec;
            Item coffee = Canary.factory().getItemFactory().newItem(ItemType.Potion);
            coffee.setDamage(47);
            coffee.setAmount(1);
            coffee.getMetaTag().put("CMCPCP_COLDTIME", System.currentTimeMillis() + 600000);
            if (controller.reportedDirtLevel() >= 5) {
                coffee.setDisplayName("Tainted Cup-o-Coffee");
                coffee.getMetaTag().put("CMCPCP_TAINTED", true);
                player.giveItem(coffee);
            }
            else {
                coffee.setDisplayName("Cup-o-Coffee");
                coffee.getMetaTag().put("CMCPCP_TAINTED", false);
                player.giveItem(coffee);
            }
            msgrec.message("§6[CMCPCP] §bHere's 1 Cup-o-Coffee");
        }
        else {
            msgrec.message("§6[CMCPCP] §cERROR 400 BAD REQUEST");
            msgrec.message("§cCannot give a machine coffee.");
        }
    }

    @Command(aliases = { "clean" },
            description = "CMCPCP CLEAN Command",
            permissions = { "cmcpcp.clean" },
            toolTip = "/cmcpcp clean",
            parent = "cmcpcp")
    public final void cleanCommand(MessageReceiver msgrec, String[] args){
        if (controller.reportBrewing()) {
            msgrec.message("§6[CMCPCP] §cERROR 503 SERVICE UNAVAILIBLE");
            msgrec.message("§cCoffee is being brewed.");
        }
        else {
            msgrec.message("§6[CMCPCP] §bCoffee Pot cleaned.");
            controller.clearDirt();
        }
    }

    @Command(aliases = { "check" },
            description = "CMCPCP CHECK Command",
            permissions = { "cmcpcp.check" },
            toolTip = "/cmcpcp check",
            parent = "cmcpcp")
    public final void checkCommand(MessageReceiver msgrec, String[] args){
        if (controller.reportBrewing()) {
            msgrec.message("§6[CMCPCP] §bBrewing in progress");
            return;
        }
        else {
            if (controller.reportedCoffeeLevel() > 80 % controller.reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §2" + String.valueOf(controller.reportedCoffeeLevel()));
            }
            else if (controller.reportedCoffeeLevel() > 60 % controller.reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §9" + String.valueOf(controller.reportedCoffeeLevel()));
            }
            else if (controller.reportedCoffeeLevel() > 40 % controller.reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §e" + String.valueOf(controller.reportedCoffeeLevel()));
            }
            else if (controller.reportedCoffeeLevel() > 20 % controller.reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §6" + String.valueOf(controller.reportedCoffeeLevel()));
            }
            else if (controller.reportedCoffeeLevel() > 0 % controller.reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §c" + String.valueOf(controller.reportedCoffeeLevel()));
            }
            else if (controller.reportedCoffeeLevel() == 0) {
                msgrec.message("§6[CMCPCP] §bThe coffee pot is §4EMPTY");
            }
        }
        if (controller.reportedDirtLevel() >= 5) {
            msgrec.message("§6[CMCPCP] §bThe coffee pot is §4DIRTY");
        }
    }
}
