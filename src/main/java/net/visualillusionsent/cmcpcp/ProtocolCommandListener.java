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

import com.google.common.collect.Lists;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.potion.PotionEffect;
import net.canarymod.api.potion.PotionEffectType;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.ReceiverType;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.TabComplete;
import net.canarymod.commandsys.TabCompleteHelper;
import net.visualillusionsent.minecraft.plugin.ChatFormat;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.List;

import static net.canarymod.Canary.factory;
import static net.canarymod.api.inventory.helper.PotionItemHelper.addCustomPotionEffects;
import static net.canarymod.api.potion.PotionEffectType.DIGSLOWDOWN;
import static net.canarymod.api.potion.PotionEffectType.DIGSPEED;
import static net.canarymod.api.potion.PotionEffectType.MOVESLOWDOWN;
import static net.canarymod.api.potion.PotionEffectType.MOVESPEED;
import static net.canarymod.api.potion.PotionEffectType.POISON;
import static net.canarymod.api.potion.PotionEffectType.REGENERATION;

/** @author Jason (darkdiplomat) */
public final class ProtocolCommandListener extends VisualIllusionsCanaryPluginInformationCommand {

    private final Item coffeeHot = factory().getItemFactory().newItem(ItemType.Potion, 8201, 1);
    private final Item coffeeCold = factory().getItemFactory().newItem(ItemType.Potion, 8201, 1);
    private final Item taintedCoffee = factory().getItemFactory().newItem(ItemType.Potion, 8204, 1);
    private final Item taintedCoffeeCold = factory().getItemFactory().newItem(ItemType.Potion, 8204, 1);
    private final CoffeePotController controller;

    public ProtocolCommandListener(CanaryModCoffeePotControlProtocol proto) throws CommandDependencyException {
        super(proto);
        this.controller = proto.getController();
        proto.registerCommands(this, false);

        coffeeHot.setDisplayName("Cup-o-Coffee");
        coffeeCold.setDisplayName("Cold Cup-o-Coffee");
        taintedCoffee.setDisplayName("Tainted Cup-o-Coffee");
        taintedCoffeeCold.setDisplayName("Tainted Cold Cup-o-Coffee");

        addCustomPotionEffects(coffeeHot, newPotionEffect(REGENERATION, 900, 2), newPotionEffect(MOVESPEED, 900, 2), newPotionEffect(DIGSPEED, 900, 2));
        addCustomPotionEffects(coffeeCold, newPotionEffect(MOVESPEED, 300, 2), newPotionEffect(DIGSPEED, 300, 2));
        addCustomPotionEffects(taintedCoffee, newPotionEffect(POISON, 600, 2), newPotionEffect(MOVESLOWDOWN, 600, 2), newPotionEffect(DIGSLOWDOWN, 600, 2));
        addCustomPotionEffects(taintedCoffeeCold, newPotionEffect(POISON, 900, 2), newPotionEffect(MOVESLOWDOWN, 900, 2), newPotionEffect(DIGSLOWDOWN, 900, 2));

    }

    private static PotionEffect newPotionEffect(PotionEffectType pet, int duration, int amplifier) {
        return factory().getPotionFactory().newPotionEffect(pet, duration, amplifier);
    }

    @Command(
            aliases = { "cmcpcp" },
            description = "CanaryModCoffeePotControlProtocol command",
            permissions = { "" },
            toolTip = "/cmcpcp [on|off|brew|get|clean|check|cfgreload]"
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
            aliases = { "on" },
            description = "CMCPCP ON Command",
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp on",
            parent = "cmcpcp"
    )
    public final void onCommand(MessageReceiver msgrec, String[] args) {
        if (!controller.reportPower()) {
            controller.togglePower();
            controller.inform(msgrec, "status.200");
            controller.inform(msgrec, "power.on");
        }
        else {
            controller.inform(msgrec, "error.503");
            controller.inform(msgrec, "power.already.on");
        }
    }

    @Command(
            aliases = { "off" },
            description = "CMCPCP OFF Command",
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp off",
            parent = "cmcpcp"
    )
    public final void offCommand(MessageReceiver msgrec, String[] args) {
        if (controller.reportPower()) {
            controller.togglePower();
            controller.inform(msgrec, "status.200");
            controller.inform(msgrec, "power.off");
        }
        else {
            controller.inform(msgrec, "error.503");
            controller.inform(msgrec, "power.already.off");
        }
    }

    @Command(
            aliases = { "brew" },
            description = "CMCPCP BREW Command",
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp brew",
            parent = "cmcpcp"
    )
    public final void brewCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (!controller.reportPower()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "no.power");
            }
            else if (controller.reportCleaning()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "cleaning.cycle.running");
            }
            else if (controller.reportedCoffeeLevel() <= 0) {
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
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp get",
            parent = "cmcpcp"
    )
    public final void getCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (!controller.reportPower()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "no.power");
            }
            else if (controller.reportCleaning()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "cleaning.cycle.running");
            }
            else if (controller.reportBrewing()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "in.progress");
            }
            else if (controller.reportedCoffeeLevel() <= 0) {
                controller.inform(msgrec, "error.400");
                controller.inform(msgrec, "coffee.out");
            }
            else if (msgrec.getReceiverType().equals(ReceiverType.PLAYER)) {
                controller.takeCup();
                Player player = (Player) msgrec;
                Item coffee;
                if (controller.reportedDirtLevel() >= 5) {
                    if (controller.isCold()) {
                        coffee = this.taintedCoffeeCold.clone();
                    }
                    else {
                        coffee = this.taintedCoffee.clone();
                    }
                }
                else {
                    if (controller.isCold()) {
                        coffee = this.coffeeCold.clone();
                    }
                    else {
                        coffee = this.coffeeHot.clone();
                    }
                }
                player.giveItem(coffee);
                controller.inform(msgrec, "status.200");
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
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp clean",
            parent = "cmcpcp"
    )
    public final void cleanCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (!controller.reportPower()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "no.power");
            }
            else if (controller.reportBrewing()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "in.progress");
            }
            else if (controller.startCleaningCycle()) {
                controller.inform(msgrec, "status.200");
                controller.inform(msgrec, "cleaning.cycle.start");
            }
            else {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "cleaning.cycle.running");
            }
        }
        catch (Exception ex) {
            controller.inform(msgrec, "error.500", ex.getMessage());
        }
    }

    @Command(
            aliases = { "check" },
            description = "CMCPCP CHECK Command",
            permissions = { "cmcpcp.use" },
            toolTip = "/cmcpcp check",
            parent = "cmcpcp"
    )
    public final void checkCommand(MessageReceiver msgrec, String[] args) {
        try {
            if (!controller.reportPower()) {
                controller.inform(msgrec, "error.503");
                controller.inform(msgrec, "no.power");
            }
            else if (controller.reportCleaning()) {
                controller.inform(msgrec, "status.200");
                controller.inform(msgrec, "cleaning.cycle.running");
            }
            else if (controller.reportBrewing()) {
                controller.inform(msgrec, "status.200");
                controller.inform(msgrec, "in.progress");
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
                controller.inform(msgrec, "status.200");
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
            List<String> tempMatch;
            List<String> matching = Lists.newArrayList();
            if (msgrec.hasPermission("cmcpcp.use")) {
                tempMatch = TabCompleteHelper.matchTo(args, new String[]{ "on", "off", "brew", "get", "clean", "check" });
                if (tempMatch != null) {
                    matching.addAll(tempMatch);
                }
            }
            if (msgrec.hasPermission("cmcpcp.cfgreload")) {
                tempMatch = TabCompleteHelper.matchTo(args, new String[]{ "cfgreload" });
                if (tempMatch != null) {
                    matching.addAll(tempMatch);
                }
            }
            return matching.isEmpty() ? null : matching;
        }
        return null;
    }
}
