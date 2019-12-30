package com.map.generation.objects.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.*;
import com.map.generation.objects.Biome;

//Make the ground
public class Ground {
    private final Model ground;
    private Color col;
    private final float height = 0.1f;

    //initialize ground model
    public Ground(int mapSize, double tileSize, Biome biome) {
        final float worldSize = (float)(mapSize * tileSize);
        final float size = worldSize * 3;

        //defining vertices for a big rectangle
        VertexInfo vert1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(-size + worldSize, height, -size + worldSize));
        VertexInfo vert2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(-size + worldSize, height, size + worldSize));
        VertexInfo vert3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(size + worldSize, height, size + worldSize));
        VertexInfo vert4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(size + worldSize, height, -size + worldSize));

        //build it
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        meshBuilder = modelBuilder.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());
        meshBuilder.rect(vert4, vert3, vert2, vert1);

        ground = modelBuilder.end();

        //define colour based on biome
        col = (Color)biome.getBiome().get("water");
    }

    //getters
    public Model getGround() {
        return ground;
    }

    public Color getCol() {
        return col;
    }
}
