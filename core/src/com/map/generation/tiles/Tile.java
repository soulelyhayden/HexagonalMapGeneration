package com.map.generation.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.map.generation.core.Functions;
import com.map.generation.core.main;

import java.util.Map;

public class Tile {

    private final Model MODEL;
    private final int sclVAL;
    private double scale;
    private double heightRange;
    private Map biomeColours;
    private Color col;
    private float sDCol = 0.02f;
    private double waterFloat;
    private final double WATERSPEED = 0.05;

    private boolean water = false;

    private Functions fct = new Functions();

     Tile(double x, double y, double z, double r, double h, Map biome) {

        this.biomeColours = biome;
        this.scale = h;
        this.sclVAL = main.getScale();
        h = r;

        final double l = r/2;
        final double n = (float)Math.sqrt(Math.pow(r,2) - Math.pow(l,2));

        double[] points =
                {
                        x, y, z,
                        x, y, z + r,
                        x + n, y, z + l,
                        x + n, y, z - l,
                        x, y, z - r,
                        x - n, y, z - l,
                        x - n, y, z + l,

                        x, y + h, z,
                        x, y + h, z + r,
                        x + n, y + h, z + l,
                        x + n, y + h, z - l,
                        x, y + h, z - r,
                        x - n, y + h, z - l,
                        x - n, y + h, z + l,
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
    }

    private void setup() {
        if (scale <= sclVAL * 0.2) {
            col = gaussianCol((Color)biomeColours.get("water"));
            water = true;
        }
        if (scale > sclVAL * 0.2 && scale <= sclVAL * 0.3) {
            col = gaussianCol((Color)biomeColours.get("sand"));
        }
        if (scale > sclVAL * 0.3 && scale <= sclVAL * 0.4) {
            col = gaussianCol((Color)biomeColours.get("dirt"));
        }
        if (scale > sclVAL * 0.4 && scale <= sclVAL * 0.7) {
            col = gaussianCol((Color)biomeColours.get("grass"));
        }
        if (scale > sclVAL * 0.7 && scale <= sclVAL * 0.95) {
            col = gaussianCol((Color)biomeColours.get("rock"));
        }
        if (scale > sclVAL * 0.95) {
            if (fct.random() > 0.5) {
                col = gaussianCol((Color) biomeColours.get("ice"));
            } else {
                col = gaussianCol((Color) biomeColours.get("snow"));
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

        meshBuilder = modelBuilder.part("part1", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material());



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

        //MeshPart part2 = meshBuilder.part("part2", GL20.GL_TRIANGLES);
        // build the second part
        return modelBuilder.end();


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
}
