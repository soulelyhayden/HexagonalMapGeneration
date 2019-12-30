package com.map.generation.objects.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.map.generation.objects.Biome;

//Make the sky model
public class Sky {
    private final Model sky;
    private Color col;

    //initialize the sky
    public Sky(int mapSize, double tileSize, Biome biome) {
        final float worldSize = (float)(mapSize * tileSize);
        final float size = worldSize * 6;

        //big sphere
        ModelBuilder modelBuilder = new ModelBuilder();
        sky = modelBuilder.createSphere(size, size, size, 20, 20,
                new Material(),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        //define colour based on biome (matching to ground/water colour looks best so far)
        col = (Color)biome.getBiome().get("water");
    }

    //getters
    public Model getSky() {
        return sky;
    }

    public Color getCol() {
        return col;
    }
}
