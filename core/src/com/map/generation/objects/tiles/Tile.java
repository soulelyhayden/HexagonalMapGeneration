package com.map.generation.objects.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.map.generation.core.Functions;
import com.map.generation.core.main;
import com.map.generation.objects.Biome;

import java.util.ArrayList;
import java.util.Map;

public class Tile {

    private final Model MODEL;
    private final int sclVAL;
    private double scale;

    private Map biomeColours;
    private Color col;
    private float sDCol;
    private double waterFloat;
    private final double WATERSPEED;
    private final double EROSION_SPEED;
    private final int MAP_SIZE;

    private boolean water = false;
    private Map ranges;

    private Functions fct = new Functions();
    private int x, z;
    private int xShift = 1;

    private Tile[][] tilesAdj;

    //Tile class
    //note: the actual height of the tiles is always equal to the tile size. Displayed height is always controlled by scaling the model instance
    Tile(int x, int y, int z, double r, double h, int MAP_SIZE, Biome biome) {
         this.x = x;
         this.z = z;

         this.scale = h;
         this.sclVAL = main.getScale();
         this.MAP_SIZE = MAP_SIZE;
         this.biomeColours = biome.getBiome();
         this.WATERSPEED = biome.getWATERSPEED();
         this.EROSION_SPEED = fct.random(biome.getEROSION_SPEED() / 2, biome.getEROSION_SPEED() * 2);

         ranges = biome.getRanges();
         sDCol = biome.getsDCol();

         double n = Math.sqrt(Math.pow(r,2) - Math.pow(r/2,2));
         final double l = r/2;

         //determine x/z pos based on grid coordinates and tile size
         double xPos = x * (n * 2);
         if (z % 2 == 0) { xPos -= n; xShift *= -1; }
         double zPos = z * (r + (float)(r / 2));
         double yPos = y;

         h = r;

         //declaring the every point in a regular hexagonal prism in 3D space based on x and z vals
         double[] points =
                {
                        xPos, yPos, zPos,
                        xPos, yPos, zPos + r,
                        xPos + n, yPos, zPos + l,
                        xPos + n, yPos, zPos - l,
                        xPos, yPos, zPos - r,
                        xPos - n, yPos, zPos - l,
                        xPos - n, yPos, zPos + l,

                        xPos, yPos + h, zPos,
                        xPos, yPos + h, zPos + r,
                        xPos + n, yPos + h, zPos + l,
                        xPos + n, yPos + h, zPos - l,
                        xPos, yPos + h, zPos - r,
                        xPos - n, yPos + h, zPos - l,
                        xPos - n, yPos + h, zPos + l,
                };

         //array for all the vertices for easy drawing
         VertexInfo[] vertices = new VertexInfo[14];

         //assigning all the points to 3D vectors
         int p = 0;
         for (int v = 0; v < vertices.length; v++) {
            Vector3 pos = new Vector3((float)points[p], (float)points[p+1], (float)points[p+2]);
            vertices[v] = new VertexInfo().setPos(pos);
            p += 3;
         }

         //build the model using meshes
         MODEL = buildModel(vertices);

         setup();
    }

    //setting up colours based on the active biome and the ranges define in the biome class based on the scale height of the tile
    private void setup() {
         //set tiles in the water range to be water | it has different properties than other tiles
         if (scale <= sclVAL * (double)ranges.get("water")) {
             col = gaussianCol((Color)biomeColours.get("water"));
             water = true;
         } else
         if (scale > sclVAL * (double)ranges.get("water") && scale <= sclVAL * (double)ranges.get("base")) {
             col = gaussianCol((Color)biomeColours.get("base"));
         } else
         if (scale > sclVAL * (double)ranges.get("base") && scale <= sclVAL * (double)ranges.get("trans")) {
            col = gaussianCol((Color)biomeColours.get("trans"));
         } else
         if (scale > sclVAL * (double)ranges.get("trans") && scale <= sclVAL * (double)ranges.get("main")) {
             col = gaussianCol((Color)biomeColours.get("main"));
         } else
         if (scale > sclVAL * (double)ranges.get("main") && scale <= sclVAL * (double)ranges.get("mountain")) {
             col = gaussianCol((Color)biomeColours.get("mountain"));
         } else
         //peaks have a random chance between two colours
         if (scale > sclVAL * (double)ranges.get("mountain")) {
             if (fct.random() > 0.5) {
                 col = gaussianCol((Color) biomeColours.get("peak1"));
             } else {
                 col = gaussianCol((Color) biomeColours.get("peak2"));
             }
         }
    }

    public void update() {

         //make the water move! woohoo
         if (water) {
             if (fct.round(waterFloat,1) == fct.round(scale, 1)) {
                 waterFloat = fct.random(0.2, (double)ranges.get("water") * sclVAL);
             }
             scale = fct.lerp(WATERSPEED, scale, waterFloat);
             scale = fct.constrain(scale, 0, (double)ranges.get("water") * sclVAL);
         }

         //get the most recent ordered array of tiles
         tilesAdj = main.getTileSet().getTilesAdjacent();

         //reset and refill these values each time
         double erosion = 0;
         ArrayList<Tile> neighbours = getNeighbours();

         //check to see which neighbours are water
         for (Tile n : neighbours) {
             if (n.water) {
                 erosion += 1;
             }
         }

         //if you have don't have 6 neighbours means some adj tiles are nulls which means they still count as water tiles for erosion
        //they don't impact as much, just looks cooler that way
         erosion += (float)((6 - neighbours.size()) / 6);

         //erosion is proportional to scale
         erosion *= fct.map(scale, 0, sclVAL, 0, 6);

         //mapping to appropriate values to adjust scale, adjust this speed in the biome settings
         erosion = fct.map(erosion,0,36,0,EROSION_SPEED);

         //water doesn't erode
         if (!water) {
             scale -= erosion;

             //once a tile is below the water level it changes to water
             if (scale <= (float)(sclVAL / 5) * (double)ranges.get("water")) {
                 col = gaussianCol((Color)biomeColours.get("water"));
                 water = true;
             }
         }
    }

    private ArrayList<Tile> getNeighbours() {
        ArrayList<Tile> ngbr = new ArrayList<Tile>();

        //find your neighbours, keep in mind every other row has been shifted to the left
        //check to make sure the neighbour your looking for is inside the bounds
        //add the neighbours to an array list
        if (x - 1 >= 0 && tilesAdj[x - 1][z] != null) {
            //even if the coordinate exists make sure the neighbour does - not all coords are populated because of natural edge
            ngbr.add(tilesAdj[x - 1][z]);
        }

        //repeat for all neighbours
        if (x + 1 <= MAP_SIZE && tilesAdj[x + 1][z] != null) {
            ngbr.add(tilesAdj[x + 1][z]);
        }

        if (z - 1 >= 0 && tilesAdj[x][z - 1] != null) {
            ngbr.add(tilesAdj[x][z - 1]);
        }

        if (z + 1 <= MAP_SIZE && tilesAdj[x][z + 1] != null) {
            ngbr.add(tilesAdj[x][z + 1]);
        }

        //accounting for diagonal neighbours and grid shifting
        if (xShift < 0 && x + xShift >= 0) {
            if (z - 1 >= 0 && tilesAdj[x + xShift][z - 1] != null) {
                ngbr.add(tilesAdj[x + xShift][z - 1]);
            }
            if (z + 1 <= MAP_SIZE && tilesAdj[x + xShift][z + 1] != null) {
                ngbr.add(tilesAdj[x + xShift][z + 1]);
            }
        } else if (xShift > 0 && x + xShift <= MAP_SIZE) {
            if (z - 1 >= 0 && tilesAdj[x + xShift][z - 1] != null) {
                ngbr.add(tilesAdj[x + xShift][z - 1]);
            }
            if (z + 1 <= MAP_SIZE && tilesAdj[x + xShift][z + 1] != null) {
                ngbr.add(tilesAdj[x + xShift][z + 1]);
            }
        }

        return ngbr;
    }

    //gaussian colour distribution for pretty shades rather than solid blocks of colour, adjust standard deviation in biome settings
    private Color gaussianCol(Color col) {
         float[] colValTemp = new float[] {col.r, col.g, col.b};

         //adjust each rgb value by a gaussian curve
         for (int j = 0; j < 3; j++) {
            double rnd = fct.randomGaussian(sDCol, 0);
            colValTemp[j] += rnd;
            colValTemp[j] = (float)fct.constrain(colValTemp[j], 0, 255);
         }

         //creates LibGDX color
        return new Color(colValTemp[0], colValTemp[1], colValTemp[2], 1);
    }

    //building the 3D model
    private Model buildModel(VertexInfo[] verts) {

        //setup libgdx meshbuilding
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        meshBuilder = modelBuilder.part("hexagonalPrism", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());

        //build the top hexagon
        meshBuilder.triangle(verts[0], verts[1], verts[2]);
        meshBuilder.triangle(verts[0], verts[2], verts[3]);
        meshBuilder.triangle(verts[0], verts[3], verts[4]);
        meshBuilder.triangle(verts[0], verts[4], verts[5]);
        meshBuilder.triangle(verts[0], verts[5], verts[6]);
        meshBuilder.triangle(verts[0], verts[6], verts[1]);

        //build the bottom hexagon
        meshBuilder.triangle(verts[9], verts[8], verts[7]);
        meshBuilder.triangle(verts[10], verts[9], verts[7]);
        meshBuilder.triangle(verts[11], verts[10], verts[7]);
        meshBuilder.triangle(verts[12], verts[11], verts[7]);
        meshBuilder.triangle(verts[13], verts[12], verts[7]);
        meshBuilder.triangle(verts[8], verts[13], verts[7]);

        //build rectangular walls - seems to work better than building them out of triangles
        meshBuilder.rect(verts[1], verts[8], verts[9], verts[2]);
        meshBuilder.rect(verts[2], verts[9], verts[10], verts[3]);
        meshBuilder.rect(verts[3], verts[10], verts[11], verts[4]);
        meshBuilder.rect(verts[4], verts[11], verts[12], verts[5]);
        meshBuilder.rect(verts[5], verts[12], verts[13], verts[6]);
        meshBuilder.rect(verts[6], verts[13], verts[8], verts[1]);

        return modelBuilder.end();


        //Experimenting with other ways to build the mesh. This method below seems better, but not working right now, also not sure because building meshes in libgdx is weird.

        /*MeshBuilder meshBuilder = new MeshBuilder();
        ModelBuilder modelBuilder = new ModelBuilder();

        meshBuilder.begin(Usage.Position | Usage.Normal, GL20.GL_TRIANGLES);
        meshBuilder.triangle(vertices[0], vertices[1], vertices[2]);
        meshBuilder.triangle(vertices[0], vertices[2], vertices[3]);
        meshBuilder.triangle(vertices[0], vertices[3], vertices[4]);
        meshBuilder.triangle(vertices[0], vertices[4], vertices[5]);
        meshBuilder.triangle(vertices[0], vertices[5], vertices[6]);
        meshBuilder.triangle(vertices[0], vertices[6], vertices[1]);
        Mesh top = meshBuilder.end();

        meshBuilder.begin(Usage.Position | Usage.Normal);
        meshBuilder.part("id2", GL20.GL_TRIANGLES);
        meshBuilder.rect(vertices[2], vertices[9], vertices[8], vertices[1]);

        meshBuilder.rect(vertices[3], vertices[10], vertices[9], vertices[2]);

        meshBuilder.rect(vertices[4], vertices[11], vertices[10], vertices[3]);

        meshBuilder.rect(vertices[5], vertices[12], vertices[11], vertices[4]);

        meshBuilder.rect(vertices[6], vertices[13], vertices[12], vertices[5]);

        meshBuilder.rect(vertices[1], vertices[8], vertices[13], vertices[6]);
        Mesh walls = meshBuilder.end();



        modelBuilder.begin();

        modelBuilder.part("top",
                top, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(col))).mesh.transform(new Matrix4().translate(0, h, 0));

        modelBuilder.part("bottom",
                top,
                Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(col)))
                .mesh.transform(new Matrix4().translate(0, 0, 0).rotate(180,0,0,180));

        modelBuilder.part("sides",
                walls, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(col))).mesh.transform(new Matrix4().translate(0, 0, 0));

        model = modelBuilder.end();*/
    }

    //getters and setters
    public Model getModel() {
        return MODEL;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scl) {
        this.scale = scl;
    }

    public Color getCol() {
        return col;
    }

    public int getX() {
         return x;
    }

    public int getZ() {
         return z;
    }
}
