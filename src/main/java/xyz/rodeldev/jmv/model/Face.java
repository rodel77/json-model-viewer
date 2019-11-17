package xyz.rodeldev.jmv.model;

import com.google.gson.JsonObject;

import xyz.rodeldev.jmv.model.Element.Direction;

public class Face {
    private String texture;
    private Direction cullface;

    private JsonObject json;

    public Face(JsonObject json){
        this.json = json;

        if(json.has("texture")) texture = json.get("texture").getAsString();
        if(json.has("cullface")) texture = json.get("cullface").getAsString();
    }
}