package com.map.generation.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.map.generation.core.main;

//LibGDX config init

public class DesktopLauncher {
	public static void main (String[] arg) {

		//launcher args
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "HexTerrain";
		cfg.height = 1024;
		cfg.width = 1920;
		new LwjglApplication(new main(), cfg);
	}
}
