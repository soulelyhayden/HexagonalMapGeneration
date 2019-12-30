package com.map.generation.objects.tiles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.map.generation.core.*;
import com.map.generation.objects.Biome;

import java.util.ArrayList;
import java.util.Map;

public class TileSet {
    private Functions fct = new Functions();

    //all these things are defined from vals in main and are final during run
    private final int MAP_SIZE;
    private final double HALF;
    private final int SEED = (int)(fct.random() * 1000);
    private final double N;
    private final int SCALE;
    private final int RAISE;

    //bigger number = smaller centre
    private final int CENTRAL_ISLAND;

    //general arrayList for tiles for updating and such
    private ArrayList<Tile> tiles;
    private Biome biome;

    //points from edge of map
    private final int CENTRE_POINT;
    private final double FURTHEST_POINT;

    //for finding neighbours, kept updated by main
    private Tile[][] tilesAdjacent;

    //initializing
    public TileSet(int mapSize, double r, int scale, int raiseHeight, Biome biome) {
        //all the map and tile stuff
        this.MAP_SIZE = mapSize;
        this.SCALE = scale;
        this.RAISE = raiseHeight;
        this.biome = biome;
        this.HALF = r;
        this.N = Math.sqrt(Math.pow(r,2) - Math.pow((float)r/2,2));

        //init tile arrays
        tiles = new ArrayList<Tile>();
        tilesAdjacent = new Tile[MAP_SIZE + 1][MAP_SIZE + 1];

        //points used for determining gradient slope
        CENTRE_POINT = MAP_SIZE / 2;
        FURTHEST_POINT = getDistance(0, 0, CENTRE_POINT, CENTRE_POINT);
        CENTRAL_ISLAND = (int)(FURTHEST_POINT / 5);
    }

    public ArrayList<Tile> getTileSet() {
        setup();
        return tiles;
    }

    private void setup() {

        //make a 2d noise map, iterating through
        for(int z = 0; z <= MAP_SIZE; z++) {
            for(int x = 0; x <= MAP_SIZE; x++) {
                double dx = (double) x / MAP_SIZE;
                double dz = (double) z / MAP_SIZE;
                int frequency = MAP_SIZE / 15;

                //adjust this to adjust initial height distribution. Smoothness & rep
                double height = (fct.PerlinNoise((dx * frequency) + SEED, (dz * frequency) + SEED, biome.getSMOOTH(), biome.getREP()));
                height = fct.map(fct.constrain(height,-1,1),-1,1,0,1);

                //initial height after scale
                height *= SCALE;
                height += RAISE;

                //apply gradient around edges to lower into water
                double sink = nearEdge(x, z);

                //constraining and mapping basically pushes the start of the gradient to a radius around the centre rather than a centre point
                sink = fct.constrain(sink, CENTRAL_ISLAND, FURTHEST_POINT);
                sink = fct.map(sink,CENTRAL_ISLAND,FURTHEST_POINT,0,1) * SCALE + RAISE;

                //adjust hegiht based on gradient edge map and keep inside scale vals
                height -= fct.constrain(sink, 0, SCALE + RAISE);
                height = fct.constrain(height,0,SCALE + RAISE);

                //only make a tile if the scale > 0, gives that natural edge rather than a square grid
                //also adds to both the arrays for updating, finding neighbours
                if (height > 0) {

                    //pass in height value and x/z coordinates on grid
                    Tile tile = new Tile(x, 0, z, HALF, height, MAP_SIZE, biome);
                    tiles.add(tile);
                    tilesAdjacent[x][z] = tile;
                }
            }
        }
    }

    //uses distance from edge and centre to draw a circular gradient
    private double nearEdge(int x, int z) {
        return Math.floor(getDistance(x, z, CENTRE_POINT, CENTRE_POINT));
    }

    //gets distance between two coordinates in grid system
    private double getDistance(int x1, int z1, int x2, int z2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((z1 - z2), 2));
    }

    //getters and setters
    Tile[][] getTilesAdjacent() {
        return tilesAdjacent;
    }

    public void setTilesAdjacent(Tile[][] tiles) {
        this.tilesAdjacent = tiles;
    }
}
