package xyz.rodeldev.jmv;

import com.google.gson.JsonArray;

import org.joml.Vector3f;

public class Utils {
    public static Vector3f parseJsonVector3(JsonArray vector){
        return new Vector3f(vector.get(0).getAsFloat(), vector.get(1).getAsFloat(), vector.get(2).getAsFloat());
    }
}