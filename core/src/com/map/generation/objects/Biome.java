package com.map.generation.objects;

import com.badlogic.gdx.graphics.Color;
import com.map.generation.core.Functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//fun little class just for storing all the biome associated data. Allows the main class to change the whole scene with just one word through dictionaries on dictionaries
public class Biome {
    private Functions fct = new Functions();
    private final String ACTIVE_BIOME;

    //the colour biomes
    private Map<String, Color> normalColours = new HashMap<String, Color>();
    private Map<String, Color> fireColours = new HashMap<String, Color>();
    private Map<String, Color> iceColours = new HashMap<String, Color>();

    //biome map for retrieving biomes, and range maps for the ranges of colours
    private Map<String, Map> biome = new HashMap<String, Map>();
    private Map<String,Double> ranges = new HashMap<String,Double>();

    private final double WATER_SPEED;
    private final double EROSION_SPEED;

    private final double SMOOTH;
    private final int REP;
    private final float sDCol;


    public Biome(String activeBiome) {
        //declare your active biome on class initialization, now if you pass that object around it'll always be that active biome
        this.ACTIVE_BIOME = activeBiome;
        SMOOTH = setNoiseVals()[0];
        REP = (int)setNoiseVals()[1];

        WATER_SPEED = setWATER_SPEED();
        EROSION_SPEED = setEROSION_SPEED();
        sDCol = setSDCOL();

        setColours();
        setBiomes();
    }

    private void setColours() {
        //the standard colour biome, duplicate this in a new mpa with new color values and add that map to the biomes map, now you can set your biome in main
        normalColours.put("water", fct.fromRGB(24,52,70));
        normalColours.put("base", fct.fromRGB(229,153,19));
        normalColours.put("trans", fct.fromRGB(72,57,42));
        normalColours.put("main", fct.fromRGB(130,155,46));
        normalColours.put("mountain", fct.fromRGB(68,68,68));
        normalColours.put("peak1", fct.fromRGB(251,242,192));
        normalColours.put("peak2", fct.fromRGB(8,126,139));
        normalColours.put("sky", fct.fromRGB(49,107,105));

        //lava colours
        fireColours.put("water", fct.fromRGB(137,0,0));
        fireColours.put("base", fct.fromRGB(224,100,0));
        fireColours.put("trans", fct.fromRGB(69,33,3));
        fireColours.put("main", fct.fromRGB(33,15,4));
        fireColours.put("mountain", fct.fromRGB(50,50,44));
        fireColours.put("peak1", fct.fromRGB(34,34,34));
        fireColours.put("peak2", fct.fromRGB(56,0,11));

        //ice colours
        iceColours.put("water", fct.fromRGB(24,52,70));
        iceColours.put("base", fct.fromRGB(132,159,181));
        iceColours.put("trans", fct.fromRGB(206,211,220));
        iceColours.put("main", fct.fromRGB(252,247,248));
        iceColours.put("mountain", fct.fromRGB(78,128,152));
        iceColours.put("peak1", fct.fromRGB(38,132,175));
        iceColours.put("peak2", fct.fromRGB(164,203,221));
        iceColours.put("sky", fct.fromRGB(204,221,229));
    }

    private void setBiomes() {
        //names for each elements have to be the same across all dictionaries, there's no check for what dictionary is being accessed which makes changing biomes easy
        ranges.put("water", 0.2);
        ranges.put("base", 0.3);
        ranges.put("trans", 0.4);
        ranges.put("main", 0.7);
        ranges.put("mountain", 0.95);
        ranges.put("peak", 1.0);
        ranges.put("peak1", 1.0);
        ranges.put("peak2", 1.0);

        //map of maps
        biome.put("ranges", ranges);
        biome.put("standard", normalColours);
        biome.put("lava", fireColours);
        biome.put("ice", iceColours);
    }

    private double setWATER_SPEED() {
        if (Objects.equals(ACTIVE_BIOME, "standard")) return 0.05f;
        if (Objects.equals(ACTIVE_BIOME, "lava")) return 0.08f;
        if (Objects.equals(ACTIVE_BIOME, "ice")) return 0.03f;
        return 0;
    }

    private double setEROSION_SPEED() {
        return WATER_SPEED * 0.5;
    }

    private double[] setNoiseVals() {
        if (Objects.equals(ACTIVE_BIOME, "standard")) return new double[] {1, 3};
        if (Objects.equals(ACTIVE_BIOME, "lava")) return new double[] {1, 5};
        if (Objects.equals(ACTIVE_BIOME, "ice")) return new double[] {0.65, 6};
        return new double[] {0, 0};
    }

    private float setSDCOL() {
        if (Objects.equals(ACTIVE_BIOME, "standard")) return 0.02f;
        if (Objects.equals(ACTIVE_BIOME, "lava")) return 0.03f;
        if (Objects.equals(ACTIVE_BIOME, "ice")) return 0.01f;
        return 0f;
    }

    //getters - what this class is all about

    //returns different standard Deviation vals depending on the active biome
    public float getsDCol() { return sDCol; }

    public double getWATERSPEED() { return WATER_SPEED; }

    public double getEROSION_SPEED() {
        return EROSION_SPEED;
    }

    public double getSMOOTH() { return SMOOTH; }
    public int getREP() { return REP; }

    public Map getRanges() { return biome.get("ranges"); }
    public Map getBiome() {
        return biome.get(ACTIVE_BIOME);
    }
}
