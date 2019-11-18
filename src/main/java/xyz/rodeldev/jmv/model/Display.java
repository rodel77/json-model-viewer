package xyz.rodeldev.jmv.model;

import com.google.gson.JsonObject;

import org.joml.Vector3f;

import xyz.rodeldev.jmv.Utils;

public class Display{
    public Vector3f rotation;
    public Vector3f translation;
    public Vector3f scale;

    public Display(JsonObject display){
        if(display.has("rotation")) rotation = Utils.parseJsonVector3(display.get("rotation").getAsJsonArray());
        if(display.has("scale")) scale = Utils.parseJsonVector3(display.get("scale").getAsJsonArray());
        if(display.has("translation")) translation = Utils.parseJsonVector3(display.get("translation").getAsJsonArray());
    }
}