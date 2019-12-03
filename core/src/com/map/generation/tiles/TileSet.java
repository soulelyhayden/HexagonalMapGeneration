package com.map.generation.tiles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.map.generation.core.*;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TileSet {
    private Functions fct = new Functions();

    private final int MAP_SIZE;
    private final int TILE_SIZE;
    private final int HALF;
    private final int SEED = (int)(fct.random() * 1000);
    private final double N;
    private final int SCALE;
    private final int RAISE;



    private ModelInstance tileInstance[][];
    private ArrayList<Tile> tiles;

    public Map<String, Color> normalColors = new HashMap<String, Color>();
    public Map<String, Map> biome = new HashMap<String, Map>();


    public TileSet(int mapSize, int r, int scale, int raiseHeight) {
        this.MAP_SIZE = mapSize;
        this.TILE_SIZE = r * 2;
        this.SCALE = scale;
        this.RAISE = raiseHeight;
        this.HALF = r;
        this.N = Math.sqrt(Math.pow(r,2) - Math.pow((float)r/2,2));

        tileInstance = new ModelInstance[MAP_SIZE][MAP_SIZE];
        tiles = new ArrayList<Tile>();



        normalColors.put("water", fct.fromRGB(24,52,70));
        normalColors.put("sand", fct.fromRGB(229,153,19));
        normalColors.put("dirt", fct.fromRGB(72,57,42));
        normalColors.put("grass", fct.fromRGB(130,155,46));
        normalColors.put("rock", fct.fromRGB(68,68,68));
        normalColors.put("snow", fct.fromRGB(251,242,192));
        normalColors.put("ice", fct.fromRGB(8,126,139));

        biome.put("standard", normalColors);
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
                int frequency = MAP_SIZE / 10;

                //adjust this to adjust initial wealth distribution. The last number affects the total wealth available.
                double wealth = (fct.PerlinNoise((dx * frequency) + SEED, (dz * frequency) + SEED, 1, 3));
                wealth = fct.map(fct.constrain(wealth,-1,1),-1,1,0,1);
                wealth *= SCALE;
                wealth += RAISE;

                wealth -= fct.constrain(nearEdge(x,z,0.2) * SCALE,0,9);
                wealth = fct.constrain(wealth,0.01,SCALE + 1);

                //create new pixel with defined values
                double xPos = (float)(-MAP_SIZE / 2) * (N * 2) + x * (N * 2);
                if (z % 2 == 0) { xPos -= N; }
                //System.out.println(wealth);
                double zPos = (float)(-MAP_SIZE / 2) * (HALF + (float)(HALF / 2)) + z * (HALF + (float)(HALF / 2));
                Tile tile = new Tile(xPos, 0, zPos, HALF, wealth, biome.get("standard"));
                tiles.add(tile);
            }
        }


    }



    private double nearEdge(int x, int z, double sensitivity) {
        double sink = 0;
        if (x <= MAP_SIZE * sensitivity) {
            sink += fct.map(x, 0,MAP_SIZE * sensitivity, 1, 0.1);

        }
        if (x >= MAP_SIZE * (1 - sensitivity)) {
            sink += fct.map(x, MAP_SIZE * (1 - sensitivity),MAP_SIZE, 0.1, 1);

        }
        if (z <= MAP_SIZE * sensitivity) {
            sink += fct.map(z, 0, MAP_SIZE * sensitivity, 1, 0.1);

        }
        if (z >= MAP_SIZE * (1 - sensitivity)) {
            sink += fct.map(z, MAP_SIZE * (1 - sensitivity),MAP_SIZE, 0.1, 1);

        }

        if (x <= MAP_SIZE * sensitivity * 2 && z <= MAP_SIZE * sensitivity * 2) {
            sink *= 2;
        }

        if (x >= MAP_SIZE * (1 - sensitivity * 2) && z >= MAP_SIZE * (1 - sensitivity * 2)) {
            sink *= 2;
        }
        return sink;
    }

}
