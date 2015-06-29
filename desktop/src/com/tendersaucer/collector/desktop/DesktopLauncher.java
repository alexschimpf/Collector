package com.tendersaucer.collector.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import core.TheGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.vSyncEnabled = true;
		config.resizable = false;
		config.title = "The Grid";
		
		if(TheGame.PACK_TEXTURES) {
			Settings settings = new Settings();
			settings.duplicatePadding = true;
			TexturePacker.process(settings, "/Users/schimpf1/Desktop/Collector_Art/textures", "/Users/schimpf1/Desktop/Collector/android/assets", "game");	
		} else {
			new LwjglApplication(new TheGame(), config);
		}
	}
}
