import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class CMCPCPprops {
	public CMCPCPprops(){
		loadPROPERTIES();
	}
	
	private static String PropertiesFILE = "plugins/config/CMCPCP/CMCPCPProperties.ini";
	Logger log = Logger.getLogger("Minecraft");
	
	String CoffeePotSize = "12";
	String CoffeeBrewDelay = "5";
	String CoffeePotDirty = "0";
	String GiveHunger = "true";
	
	public boolean getGIVEHUNGER(){
		boolean hunger = Boolean.parseBoolean(GiveHunger);
		return hunger;
	}
	
	public Integer getCOFFEEPOTSIZE() {
		int cps = Integer.parseInt(CoffeePotSize);
		return cps;
	}
	
	public Integer getCOFFEEBREWDELAY() {
		int cbd = Integer.parseInt(CoffeePotSize);
		return cbd;
	}
	
	public Integer getCOFFEEDIRTY() {
		int cpd = Integer.parseInt(CoffeePotDirty);
		return cpd;
	}
	
	public void setCOFFEEPOTDIRTY(Integer CoffeePotDirty) {
		this.CoffeePotDirty = String.valueOf(CoffeePotDirty);
		createPROPERTIES();
	}
	
	public void loadPROPERTIES() {
		File Propertiesfile = new File(PropertiesFILE);
		if (Propertiesfile.exists()) {
			try {				
				Properties iniSettings = new Properties();
				iniSettings.load(new FileInputStream(PropertiesFILE));
				CoffeePotSize = iniSettings.getProperty("CoffeePotSize", CoffeePotSize);
				CoffeeBrewDelay = iniSettings.getProperty("CofeeBrewDelay", CoffeeBrewDelay);
				CoffeePotDirty = iniSettings.getProperty("CoffeePotDirty", CoffeePotDirty);
				GiveHunger = iniSettings.getProperty("GiveHunger", GiveHunger);
			}catch (IOException ioe) {
				log.severe("CMCPCP: - file loading failed, using defaults.");
			}
			
		}else{
			createPROPERTIES();
		}
	}
	
	public void createPROPERTIES() {
		File inifile = new File(PropertiesFILE);
		try {
			inifile.getParentFile().mkdirs();
			BufferedWriter outChannel = new BufferedWriter(new FileWriter(inifile));
			outChannel.write("#Number of Cups per pot"); outChannel.newLine();
			outChannel.write("CoffeePotSize =" + CoffeePotSize); outChannel.newLine();
			outChannel.write("#Brew Delay in Minutes"); outChannel.newLine();
			outChannel.write("CoffeeBrewDelay =" + CoffeeBrewDelay); outChannel.newLine();
			outChannel.write("#Set to false to give health instead"); outChannel.newLine();
			outChannel.write("GiveHunger =" + GiveHunger); outChannel.newLine();
			outChannel.write("#########################################"); outChannel.newLine();
			outChannel.write("###DON'T EDIT ANYTHING BELOW THIS LINE###"); outChannel.newLine();
			outChannel.write("#########################################"); outChannel.newLine();
			outChannel.write("CoffeePotDirty =" + CoffeePotDirty);
			outChannel.close();
		} catch (IOException ioe) {
			log.severe("CMCPCP: - file creation failed, using defaults.");
		}
	}
}

/*******************************************************************************\
* CanaryMod Coffee Pot Control Protocol v2.x                                    *
* Copyright (C) 2012 Visual Illusions Entertainment                             *
* @author darkdiplomat <darkdiplomat@visualillusionsent.net>                    *
*                                                                               *
* This file is part of CanaryMod Coffee Pot Control Protocol                    *
*                                                                               *
* This program is free software: you can redistribute it and/or modify          *
* it under the terms of the GNU General Public License as published by          *
* the Free Software Foundation, either version 3 of the License, or             *
* (at your option) any later version.                                           *
*                                                                               *
* This program is distributed in the hope that it will be useful,               *
* but WITHOUT ANY WARRANTY; without even the implied warranty of                *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
* See the GNU General Public License for more details.                          *
*                                                                               *
* You should have received a copy of the GNU General Public License             *
* along with this program.  If not, see http://www.gnu.org/licenses/gpl.html    *
*                                                                               *
\*******************************************************************************/