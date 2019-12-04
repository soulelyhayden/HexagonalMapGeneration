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
import com.map.generation.objects.Colours;

import java.util.ArrayList;
import java.util.Map;

public class Tile {

    private final Model MODEL;
    private final int sclVAL;
    private double scale;
    private Map biomeColours;
    private Color col;
    private float sDCol = 0.02f;
    private double waterFloat;
    private final double WATERSPEED = 0.05;
    private final double EROSION_SPEED = 0.01;

    private boolean water = false;
    private Map ranges;

    private Functions fct = new Functions();
    private int x, z;

    private double[] colValTemp = new double[]{0, 0, 0};

    private Tile[][] tilesAdj;
    private ArrayList<Tile> neighbours = new ArrayList<Tile>();;

     Tile(int x, int y, int z, double r, double h, Map biome) {
         this.x = x;
         this.z = z;

         this.biomeColours = biome;
         this.scale = h;
         this.sclVAL = main.getScale();

         ranges = new Colours().getBiome("ranges");

         double n = Math.sqrt(Math.pow(r,2) - Math.pow(r/2,2));
         final double l = r/2;

         double xPos = x * (n * 2);
         if (z % 2 == 0) { xPos -= n; }
         double zPos = z * (r + (float)(r / 2));
         double yPos = y;

         h = r;

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

         VertexInfo[] vertices = new VertexInfo[14];

         int p = 0;
         for (int v = 0; v < vertices.length; v++) {
            Vector3 pos = new Vector3((float)points[p], (float)points[p+1], (float)points[p+2]);
            vertices[v] = new VertexInfo().setPos(pos);
            p += 3;
         }

         MODEL = buildModel(vertices);

         setup();

    }

    public void update() {
         if (water) {
             if (fct.round(waterFloat,1) == fct.round(scale, 1)) {
                 waterFloat = fct.random(0.2, 1.8);
             }

             scale = fct.lerp(WATERSPEED, scale, waterFloat);
         }

        tilesAdj = main.getTileSet().getTilesAdjacent();


        double erosion = 0;
        neighbours.clear();
        try {
            neighbours.add(tilesAdj[x - 1][z]);
            neighbours.add(tilesAdj[x + 1][z]);
            neighbours.add(tilesAdj[x][z - 1]);
            neighbours.add(tilesAdj[x][z + 1]);
            neighbours.add(tilesAdj[x - 1][z - 1]);
            neighbours.add(tilesAdj[x - 1][z + 1]);
        }
        catch(Exception e) {
            //  Block of code to handle errors
        }
        try {
            for (Tile n : neighbours) {
                if (n.water) {
                    erosion += 1;
                }
            }
        } catch(Exception e) {
                //  Block of code to handle errors
            }

        erosion += 6 - neighbours.size();
        //System.out.println(neighbours.size());
        erosion *= scale;

        erosion = fct.map(erosion,0,6 * sclVAL,0,EROSION_SPEED);

        if (!water) {
            scale -= erosion;
            if (scale <= (float)(sclVAL / 5) * (double)ranges.get("water")) {
                col = gaussianCol((Color)biomeColours.get("water"));
                water = true;
            }
        }



    }



    private void setup() {
        if (scale <= sclVAL * (double)ranges.get("water")) {
            col = gaussianCol((Color)biomeColours.get("water"));
            water = true;
            //TileSet tile = new TileSet(5, 5, 5, 5);
        }
        if (scale > sclVAL * (double)ranges.get("water") && scale <= sclVAL * (double)ranges.get("base")) {
            col = gaussianCol((Color)biomeColours.get("base"));
        }
        if (scale > sclVAL * (double)ranges.get("base") && scale <= sclVAL * (double)ranges.get("trans")) {
            col = gaussianCol((Color)biomeColours.get("trans"));
        }
        if (scale > sclVAL * (double)ranges.get("trans") && scale <= sclVAL * (double)ranges.get("main")) {
            col = gaussianCol((Color)biomeColours.get("main"));
        }
        if (scale > sclVAL * (double)ranges.get("main") && scale <= sclVAL * (double)ranges.get("mountain")) {
            col = gaussianCol((Color)biomeColours.get("mountain"));
        }
        if (scale > sclVAL * (double)ranges.get("mountain")) {
            if (fct.random() > 0.5) {
                col = gaussianCol((Color) biomeColours.get("peak1"));
            } else {
                col = gaussianCol((Color) biomeColours.get("peak2"));
            }
        }
    }

    private Color gaussianCol(Color col) {
         float[] colValTemp = new float[] {col.r, col.g, col.b};

         for (int j = 0; j < 3; j++) {
            double rnd = fct.randomGaussian(sDCol, 0);
            colValTemp[j] += rnd;
            colValTemp[j] = (float)fct.constrain(colValTemp[j], 0, 255);
         }

        return new Color(colValTemp[0], colValTemp[1], colValTemp[2], 1);
    }

    private Model buildModel(VertexInfo[] verts) {
        //meshBuilder.begin(Usage.Position | Usage.Normal);
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        meshBuilder = modelBuilder.part("hexagonalPrism", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());
        meshBuilder.triangle(verts[0], verts[1], verts[2]);
        meshBuilder.triangle(verts[0], verts[2], verts[3]);
        meshBuilder.triangle(verts[0], verts[3], verts[4]);
        meshBuilder.triangle(verts[0], verts[4], verts[5]);
        meshBuilder.triangle(verts[0], verts[5], verts[6]);
        meshBuilder.triangle(verts[0], verts[6], verts[1]);

        meshBuilder.triangle(verts[9], verts[8], verts[7]);
        meshBuilder.triangle(verts[10], verts[9], verts[7]);
        meshBuilder.triangle(verts[11], verts[10], verts[7]);
        meshBuilder.triangle(verts[12], verts[11], verts[7]);
        meshBuilder.triangle(verts[13], verts[12], verts[7]);
        meshBuilder.triangle(verts[8], verts[13], verts[7]);

        meshBuilder.rect(verts[1], verts[8], verts[9], verts[2]);

        meshBuilder.rect(verts[2], verts[9], verts[10], verts[3]);

        meshBuilder.rect(verts[3], verts[10], verts[11], verts[4]);

        meshBuilder.rect(verts[4], verts[11], verts[12], verts[5]);

        meshBuilder.rect(verts[5], verts[12], verts[13], verts[6]);

        meshBuilder.rect(verts[6], verts[13], verts[8], verts[1]);

        return modelBuilder.end();


        //Experimenting with other ways to build the mesh. This method below seems better, but not working right now.

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

        //Mesh mesh = meshBuilder.end();


        /*ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;
        meshBuilder = modelBuilder.part("part1", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());
        meshBuilder.(5, 5, 5, 10);
        Node node = modelBuilder.node();
        node.translation.set(10,0,0);
        meshBuilder = modelBuilder.part("part2", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());
        meshBuilder.sphere(5, 5, 5, 10, 10);
        Model model = modelBuilder.end();*/
    }

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
