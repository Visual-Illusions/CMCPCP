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
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.TabComplete;
import net.canarymod.commandsys.TabCompleteHelper;
import net.visualillusionsent.minecraft.plugin.ChatFormat;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.Iterator;
import java.util.List;

/** @author Jason (darkdiplomat) */
public final class ProtocolCommandListener extends VisualIllusionsCanaryPluginInformationCommand {

    private final Item coffee = Canary.factory().getItemFactory().newItem(ItemType.Potion, 47, 1);
    private final CoffeePotController controller;

    public ProtocolCommandListener(CanaryModCoffeePotControlProtocol proto) throws CommandDependencyException {
        super(proto);
        this.controller = proto.getController();
        proto.registerCommands(this, false);
    }

    @Command(
            aliases = { "cmcpcp" },
            description = "CanaryModCoffeePotControlProtocol command",
            permissions = { "cmcpcp.main" },
            toolTip = "/cmcpcp [brew|get|clean|check|cfgreload]"
    )
    public final void protocolCommand(MessageReceiver msgrec, String[] args) {
        this.sendInformation(msgrec);
    }

    @Command(
            aliases = { "cfgreload" },
            description = "CMCPCP Config Reload",
            permissions = { "cmcpcp.cfgreload" },
            toolTip = "/cmcpcp cfgreload",
            parent = "cmcpcp"
    )
    public final void cfgReloadCommand(MessageReceiver msgrec, String[] args) {
        try {
            controller.reload();
            controller.inform(msgrec, "cfg.reload.success");
        }
        catch (Exception ex) {
            controller.inform(msgrec, "cfg.reload.fail");
        }
    }

    @Command(
            aliases = { "brew" },
            description = "CMCPCP BREW Command",
            permissions = { "cmcpcp.brew" },
            toolTip = "/cmcpcp brew",
            parent = "cmcpcp"
    )
    public final void brewCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (controller.reportedCoffeeLevel() <= 0) {
                if (controller.brewCoffee()) {
                    if (controller.reportedDirtLevel() >= 5) {
                        controller.informAll("pot.dirty");
                    }
                }
                else {
                    controller.inform(msgrec, "error.400");
                    controller.inform(msgrec, "in.progress");
                }
            }
            else {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "already.brewed");
            }
        }
        catch (Exception ex) {
            controller.inform(msgrec, "error.500", ex.getMessage());
        }
    }

    @Command(
            aliases = { "get" },
            description = "CMCPCP GET Command",
            permissions = { "cmcpcp.get" },
            toolTip = "/cmcpcp get",
            parent = "cmcpcp"
    )
    public final void getCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (controller.reportBrewing()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "in.progress");
                return;
            }
            else if (controller.reportedCoffeeLevel() <= 0) {
                controller.inform(msgrec, "error.400");
                controller.inform(msgrec, "coffee.out");
                return;
            }
            if (msgrec instanceof Player) {
                controller.takeCup();
                Player player = (Player) msgrec;
                Item coffee = this.coffee.clone();
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
                controller.inform(msgrec, "one.cup");
            }
            else {
                controller.inform(msgrec, "error.400");
                controller.inform(msgrec, "no.machine");
            }
        }
        catch (Exception ex) {
            controller.inform(msgrec, "error.500", ex.getMessage());
        }
    }

    @Command(
            aliases = { "clean" },
            description = "CMCPCP CLEAN Command",
            permissions = { "cmcpcp.clean" },
            toolTip = "/cmcpcp clean",
            parent = "cmcpcp"
    )
    public final void cleanCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (controller.reportBrewing()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "in.progress");
            }
            else {
                msgrec.message("§6[CMCPCP] §bCoffee Pot cleaned.");
                controller.clearDirt();
            }
        }
        catch (Exception ex) {
            controller.inform(msgrec, "error.500", ex.getMessage());
        }
    }

    @Command(
            aliases = { "check" },
            description = "CMCPCP CHECK Command",
            permissions = { "cmcpcp.check" },
            toolTip = "/cmcpcp check",
            parent = "cmcpcp"
    )
    public final void checkCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (controller.reportBrewing()) {
                msgrec.message("§6[CMCPCP] §bBrewing in progress");
            }
            else {
                // Coffee Left
                String color = ChatFormat.RED.toString(); // 0%
                float percent = controller.reportedCoffeeLevel() * 100.0F / controller.reportedPotSize();
                if (percent == 100) {
                    // 100% Full
                    color = ChatFormat.GREEN.toString();
                }
                else if (percent >= 75) {
                    //99% to 75% Full
                    color = ChatFormat.LIGHT_GREEN.toString();
                }
                else if (percent >= 50) {
                    // 74% to 50% Full
                    color = ChatFormat.YELLOW.toString();
                }
                else if (percent >= 25) {
                    // 49% to 25% Full
                    color = ChatFormat.ORANGE.toString();
                }
                else if (percent > 0) {
                    color = ChatFormat.LIGHT_RED.toString();
                }
                controller.inform(msgrec, "coffee.left", color.concat(String.valueOf(controller.reportedCoffeeLevel())));
                // Check Dirt Level
                if (controller.reportedDirtLevel() >= 5) {
                    controller.inform(msgrec, "pot.dirty");
                }
            }
        }
        catch (Exception ex) {
            controller.inform(msgrec, "error.500", ex.getMessage());
        }
    }

    @TabComplete(commands = "cmcpcp")
    public final List<String> protoComp(MessageReceiver msgrec, String[] args) {
        if (args.length == 1) {
            List<String> matching = TabCompleteHelper.matchTo(args, new String[]{ "brew", "get", "clean", "check", "cfgreload" });
            Iterator<String> matchItr = matching.iterator();
            while (matchItr.hasNext()) {
                if (!msgrec.hasPermission("cmcpcp.".concat(matchItr.next()))) {
                    matchItr.remove();
                }
            }
            return matching;
        }
        return null;
    }
}
