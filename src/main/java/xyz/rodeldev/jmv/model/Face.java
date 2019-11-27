package xyz.rodeldev.jmv.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.joml.Vector2f;
import org.joml.Vector2i;

import xyz.rodeldev.jmv.model.Element.Direction;

public class Face {
    public Direction direction;
    public String texture;
    public Direction cullface;

    public Vector2f uvFrom = new Vector2f(0, 16), uvTo = new Vector2f(0, 16);

    private JsonObject json;

    public Face(JsonObject json, Direction direction){
        this.direction = direction;
        this.json = json;

        if(json.has("texture")) {
            texture = json.get("texture").getAsString();
            System.out.println("TEXTURE "+texture);
            texture = (texture.startsWith("#") ? texture.substring(1) : texture);
        }

        if(json.has("uv")){
            JsonArray array = json.get("uv").getAsJsonArray();
            uvFrom = new Vector2f(array.get(0).getAsFloat()/16, array.get(1).getAsFloat()/16);
            uvTo = new Vector2f(array.get(2).getAsFloat()/16, array.get(3).getAsFloat()/16);
        }
    }
}