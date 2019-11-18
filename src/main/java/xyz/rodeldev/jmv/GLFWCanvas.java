package xyz.rodeldev.jmv;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.system.MemoryStack.*;

public class GLFWCanvas {
    public long windowID;

    public long initWindow(){
        // glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        // glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        // glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        // glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GL_TRUE);

        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

        windowID = glfwCreateWindow(640, 640, "JSONModelViewer", NULL, NULL);

        if(windowID==NULL){
            System.err.println("No glfw window!");
        }

        // try(MemoryStack stack = stackPush()) {
        //     IntBuffer pw = stack.mallocInt(1);
        //     IntBuffer ph = stack.mallocInt(1);

        //     glfwGetFramebufferSize(windowID, pw, ph);

        //     // render(pw.get(0), ph.get(0));
        // }

        glfwShowWindow(windowID);

        glfwMakeContextCurrent(windowID);
        GLCapabilities cap = GL.createCapabilities();
        return windowID;
    }
}