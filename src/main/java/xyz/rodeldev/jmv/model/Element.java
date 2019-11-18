package xyz.rodeldev.jmv.model;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.joml.Vector3f;

import xyz.rodeldev.jmv.Utils;

public class Element {
    public Vector3f from;
    public Vector3f to;

    public HashMap<Direction, Face> faces = new HashMap<>();

    public JsonObject json;

    public Element(Vector3f from, Vector3f to){
        this.from = from;
        this.to = to;
    }

    public Element(JsonObject json){
        this.json = json;
        this.from = Utils.parseJsonVector3(json.get("from").getAsJsonArray());
        this.to   = Utils.parseJsonVector3(json.get("to").getAsJsonArray());

        if(json.has("faces")){
            for(Entry<String, JsonElement> entry : json.get("faces").getAsJsonObject().entrySet()){
                faces.put(Direction.valueOf(entry.getKey().toUpperCase()), new Face(entry.getValue().getAsJsonObject()));
            }
        }
    }

    public enum Direction {
        UP, DOWN, NORTH, SOUTH, EAST, WEST;
    }
}