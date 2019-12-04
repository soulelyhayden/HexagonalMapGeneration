package com.map.generation.objects;

import com.badlogic.gdx.graphics.Color;
import com.map.generation.core.Functions;

import java.util.HashMap;
import java.util.Map;

public class Colours {
    private Functions fct = new Functions();

    private Map<String, Color> normalColors = new HashMap<String, Color>();
    private Map<String, Color> fireColors = new HashMap<String, Color>();
    private Map<String, Map> biome = new HashMap<String, Map>();
    private Map<String,Double> ranges = new HashMap<String,Double>();

    public Colours() {
        ranges.put("water", 0.2);
        ranges.put("base", 0.3);
        ranges.put("trans", 0.4);
        ranges.put("main", 0.7);
        ranges.put("mountain", 0.95);
        ranges.put("peak", 1.0);
        ranges.put("peak1", 1.0);
        ranges.put("peak2", 1.0);

        normalColors.put("water", fct.fromRGB(24,52,70));
        normalColors.put("base", fct.fromRGB(229,153,19));
        normalColors.put("trans", fct.fromRGB(72,57,42));
        normalColors.put("main", fct.fromRGB(130,155,46));
        normalColors.put("mountain", fct.fromRGB(68,68,68));
        normalColors.put("peak1", fct.fromRGB(251,242,192));
        normalColors.put("peak2", fct.fromRGB(8,126,139));
        normalColors.put("sky", fct.fromRGB(49,107,105));

        fireColors.put("water", fct.fromRGB(137,0,0));
        fireColors.put("base", fct.fromRGB(224,100,0));
        fireColors.put("trans", fct.fromRGB(69,33,3));
        fireColors.put("main", fct.fromRGB(33,15,4));
        fireColors.put("mountain", fct.fromRGB(50,50,44));
        fireColors.put("peak1", fct.fromRGB(34,34,34));
        fireColors.put("peak2", fct.fromRGB(56,0,11));

        biome.put("ranges", ranges);
        biome.put("standard", normalColors);
        biome.put("lava", fireColors);
    }

    public Map getBiome(String str) {
        return biome.get(str);
    }
}
