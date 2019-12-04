package com.map.generation.objects.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.Map;

public class Sky {
    private final Model sky;
    private Color col;

    public Sky(int mapSize, double tileSize, Map biome) {
        final float worldSize = (float)(mapSize * tileSize);
        final float size = worldSize * 6;

        ModelBuilder modelBuilder = new ModelBuilder();
        sky = modelBuilder.createSphere(size, size, size, 20, 20,
                new Material(),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        col = (Color)biome.get("water");
    }

    public Model getSky() {
        return sky;
    }

    public Color getCol() {
        return col;
    }
}
