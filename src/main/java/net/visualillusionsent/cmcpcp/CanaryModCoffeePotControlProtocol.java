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

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;
import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.utils.ProgramStatus;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.utils.VersionChecker;

public final class CanaryModCoffeePotControlProtocol extends Plugin {

    static CanaryModCoffeePotControlProtocol $;
    private final CoffeePotController cpc;
    private final VersionChecker vc;
    private float version;
    private short build;
    private String buildTime;
    private ProgramStatus status;

    public CanaryModCoffeePotControlProtocol() {
        $ = this;
        cpc = new CoffeePotController();
        readManifest();
        vc = new VersionChecker(getName(), String.valueOf(version), String.valueOf(build), "http://visualillusionsent.net/minecraft/plugins/", status, false);
    }

    public final boolean enable() {
        try {
            checkStatus();
            checkVersion();
            new ProtocolCommandListener(this);
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

    public final void disable() {
        cpc.cleanUp();
        $ = null;
    }

    final CoffeePotController getController() {
        return cpc;
    }

    private final Manifest getManifest() throws Exception {
        Manifest toRet = null;
        Exception ex = null;
        JarFile jar = null;
        try {
            jar = new JarFile(getJarPath());
            toRet = jar.getManifest();
        }
        catch (Exception e) {
            ex = e;
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException e) {}
            }
            if (ex != null) {
                throw ex;
            }
        }
        return toRet;
    }

    private final void readManifest() {
        try {
            Manifest manifest = getManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            version = Float.parseFloat(mainAttribs.getValue("Version").replace("-SNAPSHOT", ""));
            build = Short.parseShort(mainAttribs.getValue("Build"));
            buildTime = mainAttribs.getValue("Build-Time");
            try {
                status = ProgramStatus.valueOf(mainAttribs.getValue("ProgramStatus"));
            }
            catch (IllegalArgumentException iaex) {
                status = ProgramStatus.UNKNOWN;
            }
        }
        catch (Exception ex) {
            version = -1.0F;
            build = -1;
            buildTime = "19700101-0000";
        }
    }

    private final void checkStatus() {
        if (status == ProgramStatus.UNKNOWN) {
            getLogman().severe(getName() + " has declared itself as an 'UNKNOWN STATUS' build. Use is not advised and could cause damage to your system!");
        }
        else if (status == ProgramStatus.ALPHA) {
            getLogman().warning(getName() + " has declared itself as a 'ALPHA' build. Production use is not advised!");
        }
        else if (status == ProgramStatus.BETA) {
            getLogman().warning(getName() + " has declared itself as a 'BETA' build. Production use is not advised!");
        }
        else if (status == ProgramStatus.RELEASE_CANDIDATE) {
            getLogman().info(getName() + " has declared itself as a 'Release Candidate' build. Expect some bugs.");
        }
    }

    private final void checkVersion() {
        Boolean islatest = vc.isLatest();
        if (islatest == null) {
            getLogman().warning("VersionCheckerError: " + vc.getErrorMessage());
        }
        else if (!vc.isLatest()) {
            getLogman().warning(vc.getUpdateAvailibleMessage());
            getLogman().warning("You can view update info @ " + getWikiLink() + "#ChangeLog");
        }
    }

    final float getRawVersion() {
        return version;
    }

    final short getBuildNumber() {
        return build;
    }

    final String getBuildTime() {
        return buildTime;
    }

    final VersionChecker getVersionChecker() {
        return vc;
    }

    final String inceptionYear() {
        return this.getCanaryInf().getString("inceptionYear");
    }

    final String currentYear() {
        return DateUtils.longToFormatedDateTime(System.currentTimeMillis(), "YYYY");
    }

    final String getWikiLink() {
        return this.getCanaryInf().getString("wikiLink");
    }

    final String getIssuesLink() {
        return this.getCanaryInf().getString("issuesLink");
    }
}
