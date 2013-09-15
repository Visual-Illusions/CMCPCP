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

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.VersionChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.visualillusionsent.cmcpcp.CanaryModCoffeePotControlProtocol.$;

public final class ProtocolCommandListener implements CommandListener {

    private final List<String> about;
    private final Item coffee = Canary.factory().getItemFactory().newItem(ItemType.Potion, 47, 1);

    public ProtocolCommandListener(CanaryModCoffeePotControlProtocol proto) throws CommandDependencyException {
        Canary.commands().registerCommands(this, proto, false);
        List<String> pre = new ArrayList<String>();
        pre.add(center(Colors.TURQUIOSE + "--- " + Colors.LIGHT_GREEN + $.getName() + Colors.ORANGE + " v" + $.getVersion() + Colors.TURQUIOSE + " ---"));
        pre.add("$VERSION_CHECK$");
        pre.add(Colors.ORANGE + "Build: " + Colors.LIGHT_GREEN + $.getBuildNumber());
        pre.add(Colors.ORANGE + "Built: " + Colors.LIGHT_GREEN + $.getBuildTime());
        pre.add(Colors.ORANGE + "Developer(s): " + Colors.LIGHT_GREEN + "darkdiplomat");
        pre.add(Colors.ORANGE + "Website: " + Colors.LIGHT_GREEN + $.getWikiLink());
        pre.add(Colors.ORANGE + "Sources: " + Colors.LIGHT_GREEN + $.getSourceLink());
        pre.add(Colors.ORANGE + "Issues: " + Colors.LIGHT_GREEN + $.getIssuesLink());

        // Next line should always remain at the end of the About
        pre.add(center(String.format("§6Copyright © %s-%s §2Visual §6I§9l§bl§4u§as§2i§5o§en§7s §AEntertainment", $.inceptionYear(), $.currentYear())));
        about = Collections.unmodifiableList(pre);
    }

    @Command(aliases = {"cmcpcp"},
            description = "CanaryModCoffeePotControlProtocol command",
            permissions = {"cmcpcp.main"},
            toolTip = "/cmcpcp [brew|get|clean|check]")
    public final void ProtocolCommand(MessageReceiver msgrec, String[] args) {
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = $.getVersionChecker();
                Boolean islatest = vc.isLatest();
                if (islatest == null) {
                    msgrec.message(center(Colors.LIGHT_GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                }
                else if (!vc.isLatest()) {
                    msgrec.message(center(Colors.LIGHT_GRAY + vc.getUpdateAvailibleMessage()));
                }
                else {
                    msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                }
            }
            else {
                msgrec.message(msg);
            }
        }
    }

    @Command(aliases = {"brew"},
            description = "CMCPCP BREW Command",
            permissions = {"cmcpcp.brew"},
            toolTip = "/cmcpcp brew",
            parent = "cmcpcp")
    public final void brewCommand(MessageReceiver msgrec, String[] args) {
        if ($.getController().reportedCoffeeLevel() <= 0) {
            if ($.getController().brewCoffee()) {
                if ($.getController().reportedDirtLevel() >= 5) {
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

    @Command(aliases = {"get"},
            description = "CMCPCP GET Command",
            permissions = {"cmcpcp.get"},
            toolTip = "/cmcpcp get",
            parent = "cmcpcp")
    public final void getCommand(MessageReceiver msgrec, String[] args) {
        if ($.getController().reportBrewing()) {
            msgrec.message("§6[CMCPCP] §cERROR 503 SERVICE UNAVAILIBLE");
            msgrec.message("§cCoffee is being brewed.");
            return;
        }
        else if ($.getController().reportedCoffeeLevel() <= 0) {
            msgrec.message("§6[CMCPCP] §cERROR 400 BAD REQUEST");
            msgrec.message("§cOut of Coffee.");
            return;
        }
        if (msgrec instanceof Player) {
            $.getController().takeCup();
            Player player = (Player)msgrec;
            Item coffee = this.coffee.clone();
            coffee.getMetaTag().put("CMCPCP_COLDTIME", System.currentTimeMillis() + 600000);
            if ($.getController().reportedDirtLevel() >= 5) {
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

    @Command(aliases = {"clean"},
            description = "CMCPCP CLEAN Command",
            permissions = {"cmcpcp.clean"},
            toolTip = "/cmcpcp clean",
            parent = "cmcpcp")
    public final void cleanCommand(MessageReceiver msgrec, String[] args) {
        if ($.getController().reportBrewing()) {
            msgrec.message("§6[CMCPCP] §cERROR 503 SERVICE UNAVAILIBLE");
            msgrec.message("§cCoffee is being brewed.");
        }
        else {
            msgrec.message("§6[CMCPCP] §bCoffee Pot cleaned.");
            $.getController().clearDirt();
        }
    }

    @Command(aliases = {"check"},
            description = "CMCPCP CHECK Command",
            permissions = {"cmcpcp.check"},
            toolTip = "/cmcpcp check",
            parent = "cmcpcp")
    public final void checkCommand(MessageReceiver msgrec, String[] args) {
        if ($.getController().reportBrewing()) {
            msgrec.message("§6[CMCPCP] §bBrewing in progress");
            return;
        }
        else {
            if ($.getController().reportedCoffeeLevel() > 80 % $.getController().reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §2" + String.valueOf($.getController().reportedCoffeeLevel()));
            }
            else if ($.getController().reportedCoffeeLevel() > 60 % $.getController().reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §9" + String.valueOf($.getController().reportedCoffeeLevel()));
            }
            else if ($.getController().reportedCoffeeLevel() > 40 % $.getController().reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §e" + String.valueOf($.getController().reportedCoffeeLevel()));
            }
            else if ($.getController().reportedCoffeeLevel() > 20 % $.getController().reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §6" + String.valueOf($.getController().reportedCoffeeLevel()));
            }
            else if ($.getController().reportedCoffeeLevel() > 0 % $.getController().reportedPotSize()) {
                msgrec.message("§6[CMCPCP] §bCups left = §c" + String.valueOf($.getController().reportedCoffeeLevel()));
            }
            else if ($.getController().reportedCoffeeLevel() == 0) {
                msgrec.message("§6[CMCPCP] §bThe coffee pot is §4EMPTY");
            }
        }
        if ($.getController().reportedDirtLevel() >= 5) {
            msgrec.message("§6[CMCPCP] §bThe coffee pot is §4DIRTY");
        }
    }

    private final String center(String toCenter) {
        String strColorless = TextFormat.removeFormatting(toCenter);
        return StringUtils.padCharLeft(toCenter, (int)(Math.floor(63 - strColorless.length()) / 2), ' ');
    }
}
