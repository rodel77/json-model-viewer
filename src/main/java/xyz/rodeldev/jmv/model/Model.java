package xyz.rodeldev.jmv.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.plaf.TextUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonStreamParser;

import xyz.rodeldev.jmv.JSONModelViewer;

public class Model {
    JsonObject json;

    public String name;

    private Display guiDisplay;

    public HashMap<String, String> textures;

    public Model parent;
    public Model child;

    public List<Element> elements = new ArrayList<>();

    public Model(File file, HashMap<String, String> textures) throws FileNotFoundException {
        name = file.getName();
        JsonStreamParser parser = new JsonStreamParser(new FileReader(file));
        json = parser.next().getAsJsonObject();

        if(textures==null) {
            this.textures = new HashMap<>();
        }else{
            this.textures = textures;
        }

        if(json.has("parent")){
            File parent_file = new File(JSONModelViewer.models_folder, json.get("parent").getAsString()+".json");
            parent = new Model(parent_file, this.textures);
            parent.child = this;
        }

        loadModel();
    }

    public Model(File file) throws FileNotFoundException {
        this(file, null);
    }

    private void loadModel(){
        System.out.println("Loading model "+name);

        if(json.has("textures")){
            JsonObject jsonTextures = json.get("textures").getAsJsonObject();
            for(Entry<String, JsonElement> texture : jsonTextures.entrySet()){
                textures.put(texture.getKey(), texture.getValue().getAsString());
            }
        }

        if(json.has("elements")){
            JsonArray jsonElements = json.get("elements").getAsJsonArray();
            for(JsonElement jsonElement : jsonElements){
                elements.add(new Element(jsonElement.getAsJsonObject()));
            }
        }

        if(json.has("display")){
            JsonObject display = json.get("display").getAsJsonObject();
            if(display.has("gui")){
                guiDisplay = new Display(display.get("gui").getAsJsonObject());
            }
        }
    }

    public Display getFirstGUIDisplay(){
        Model root = this;
        while(root!=null){
            if(root.guiDisplay!=null) return root.guiDisplay;
            root = root.parent;
        }
        return null;
    }

    public List<Element> flatElement(){
        List<Element> elements = new ArrayList<>();   

        Model root = this;
        while(root!=null){
            for(Element element : root.elements){
                elements.add(element);
            }
            root = root.parent;
        }

        return elements;
    }
}