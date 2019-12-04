package com.map.generation.objects.tiles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.map.generation.core.*;

import java.util.ArrayList;
import java.util.Map;

public class TileSet {
    private Functions fct = new Functions();

    private final int MAP_SIZE;
    private final double HALF;
    private final int SEED = (int)(fct.random() * 1000);
    private final double N;
    private final int SCALE;
    private final int RAISE;

    //bigger number = smaller centre
    private final int CENTRAL_ISLAND = 5;

    private ArrayList<Tile> tiles;
    private Map biome;

    private final int CENTRE_POINT;
    private final double FURTHEST_POINT;

    private Tile[][] tilesAdjacent;

    public TileSet(int mapSize, double r, int scale, int raiseHeight, Map biome) {
        this.MAP_SIZE = mapSize;
        this.SCALE = scale;
        this.RAISE = raiseHeight;
        this.biome = biome;

        this.HALF = r;
        this.N = Math.sqrt(Math.pow(r,2) - Math.pow((float)r/2,2));

        tiles = new ArrayList<Tile>();
        tilesAdjacent = new Tile[MAP_SIZE + 1][MAP_SIZE + 1];

        CENTRE_POINT = MAP_SIZE / 2;
        FURTHEST_POINT = getDistance(0, 0, CENTRE_POINT, CENTRE_POINT);
    }

    public ArrayList<Tile> getTileSet() {
        setup();
        return tiles;
    }

    private void setup() {

        for(int z = 0; z <= MAP_SIZE; z++) {
            for(int x = 0; x <= MAP_SIZE; x++) {
                double dx = (double) x / MAP_SIZE;
                double dz = (double) z / MAP_SIZE;
                int frequency = MAP_SIZE / 15;

                //adjust this to adjust initial height distribution.
                double height = (fct.PerlinNoise((dx * frequency) + SEED, (dz * frequency) + SEED, 1, 3));
                height = fct.map(fct.constrain(height,-1,1),-1,1,0.2,1);

                height *= SCALE;
                height += RAISE;

                double sink = nearEdge(x, z, MAP_SIZE / CENTRAL_ISLAND);
                sink = fct.map(sink,0,FURTHEST_POINT,0,1) * SCALE + RAISE;
                height -= fct.constrain(sink, 0, SCALE + RAISE);
                height = fct.constrain(height,0,SCALE + RAISE);

                //create new pixel with defined values


                if (height > 0) {
                    Tile tile = new Tile(x, 0, z, HALF, height, biome);
                    tiles.add(tile);
                    tilesAdjacent[x][z] = tile;
                }
            }
        }
    }

    public Tile[][] getTilesAdjacent() {
        return tilesAdjacent;
    }

    public void setTilesAdjacent(Tile[][] tiles) {
        this.tilesAdjacent = tiles;
    }

    private double nearEdge(int x, int z, int radius) {
        if (getDistance(x, z, CENTRE_POINT, CENTRE_POINT) < radius) {
            return 0;
        } else {
            return Math.floor(getDistance(x, z, CENTRE_POINT, CENTRE_POINT));
        }
    }

    double getDistance(int x1, int z1, int x2, int z2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((z1 - z2), 2));
    }

}
