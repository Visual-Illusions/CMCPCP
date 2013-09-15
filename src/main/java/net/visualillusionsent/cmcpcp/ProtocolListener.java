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
import net.canarymod.api.inventory.Item;
import net.canarymod.api.potion.PotionEffect;
import net.canarymod.api.potion.PotionEffectType;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.EatHook;
import net.canarymod.plugin.PluginListener;

public final class ProtocolListener implements PluginListener {
    private final PotionEffect[] freshCoffee, coldCoffee, dirtyCoffee, coldDirtyCoffee;

    public ProtocolListener() {
        freshCoffee = new PotionEffect[]{
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.REGENERATION, 900, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.MOVESPEED, 900, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.DIGSPEED, 900, 2)
        };
        coldCoffee = new PotionEffect[]{
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.MOVESPEED, 300, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.DIGSPEED, 300, 2)
        };
        dirtyCoffee = new PotionEffect[]{
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.POISON, 600, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.MOVESLOWDOWN, 600, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.DIGSLOWDOWN, 600, 2)
        };
        coldDirtyCoffee = new PotionEffect[]{
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.POISON, 900, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.MOVESLOWDOWN, 900, 2),
                Canary.factory().getPotionFactory().newPotionEffect(PotionEffectType.DIGSLOWDOWN, 900, 2)
        };
    }

    @HookHandler
    public final void drankCoffee(EatHook hook) {
        long cold = System.currentTimeMillis();
        Item coffee = hook.getItem();
        if (coffee.getId() == 373 && coffee.getDamage() == 47) {
            if (coffee.getMetaTag().containsKey("CMCPCP_TAINTED") && coffee.getMetaTag().containsKey("CMCPCP_COLDTIME")) {
                if (!coffee.getMetaTag().getBoolean("CMCPCP_TAINTED")) {
                    if (coffee.getMetaTag().getLong("CMCPCP_COLDTIME") > cold) {
                        hook.setPotionEffects(freshCoffee);
                    }
                    else {
                        hook.setPotionEffects(coldCoffee);
                    }
                }
                else {
                    if (coffee.getMetaTag().getLong("CMCPCP_COLDTIME") > cold) {
                        hook.setPotionEffects(dirtyCoffee);
                    }
                    else {
                        hook.setPotionEffects(coldDirtyCoffee);
                    }
                }
            }
        }
    }
}
