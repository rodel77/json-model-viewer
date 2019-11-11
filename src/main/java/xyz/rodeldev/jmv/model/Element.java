package xyz.rodeldev.jmv.model;

import java.util.HashMap;

import org.joml.Vector3f;

public class Element {
    public Vector3f from;
    public Vector3f to;

    private HashMap<Direction, Face> faces = new HashMap<>();

    public Element(Vector3f from, Vector3f to){
        this.from = from;
        this.to = to;
    }

    public enum Direction {
        UP, DOWN, NORTH, SOUTH, EAST, WEST;
    }
}