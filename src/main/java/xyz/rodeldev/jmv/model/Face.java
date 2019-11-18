package xyz.rodeldev.jmv.model;

import com.google.gson.JsonObject;

import xyz.rodeldev.jmv.model.Element.Direction;

public class Face {
    public String texture;
    public Direction cullface;

    private JsonObject json;

    public Face(JsonObject json){
        this.json = json;

        if(json.has("texture")) {
            texture = json.get("texture").getAsString();
            System.out.println("TEXTURE "+texture);
            texture = (texture.startsWith("#") ? texture.substring(1) : texture);
        }
    }
}