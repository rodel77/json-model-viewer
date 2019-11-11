package xyz.rodeldev.jmv;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import xyz.rodeldev.jmv.model.Element;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer{
    private long context;
    private LWJGLCanvas canvas;

    public float zoom = -180;

    public boolean isOrtho = true;
    public boolean needsUpdate = false;

    public float rx = 30, ry = 225, rz;

    private int floor_texture;

    private Element element;

    public BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public void createContext(long context, LWJGLCanvas canvas){
        this.context = context;
        this.canvas = canvas;

        glEnable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if(isOrtho) {
            perspectiveGL(2, (float)canvas.getWidth()/(float)canvas.getHeight(), .3f, 10000);
        }else{
            perspectiveGL(60, (float)canvas.getWidth()/(float)canvas.getHeight(), .3f, 100);
        }
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // glEnable(GL_CULL_FACE);
        glClearColor(0.375F, 0.5625F, 0.75F, 1F);

        // Textures
        floor_texture = TextureLoader.loadTexture("/dirt.png");

        element = new Element(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));
    }
    
    public void resize(int width, int height){
        needsUpdate = true;
    }

    public void render(int width, int height){
        if(needsUpdate){
            glViewport(0, 0, width, height);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            if(isOrtho) {
                perspectiveGL(2, (float)width/(float)height, .3f, 10000);
            }else{
                perspectiveGL(60, (float)width/(float)height, .3f, 100);
            }
            glMatrixMode(GL_MODELVIEW);
            needsUpdate = false;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        glTranslatef(0, 0, zoom);

        glRotatef(rx, 1, 0, 0);
        glRotatef(ry, 0, 1, 0);
        glRotatef(rz, 0, 0, 1);

        // glDisable(GL_TEXTURE_2D);

        // renderFloor();

        renderElement(element);

        // glBegin(GL_QUADS);
        // glTexCoord2f(0, 0);
        // glVertex2f(0, 0);
        // glTexCoord2f(0, 1);
        // glVertex2f(0, 1);
        // glTexCoord2f(1, 1);
        // glVertex2f(1, 1);
        // glTexCoord2f(1, 0);
        // glVertex2f(1, 0);
        // glEnd();

        while(!queue.isEmpty()){
            try {
                queue.take().run();
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    void renderFloor(){
        glPushMatrix();
        {
            glScalef(.25f, .25f, .25f);
            glTranslatef(-8f, -8f, -8f);
            glBegin(GL_QUADS);
            {
                glTexCoord2f(0, 0);
				glVertex3f(-16, 0, -16);
				glTexCoord2f(0, 1);
				glVertex3f(-16, 0, 32);
				glTexCoord2f(1, 1);
				glVertex3f(32, 0, 32);
				glTexCoord2f(1, 0);
				glVertex3f(32, 0, -16);
            }
            glEnd();
        }
        glPopMatrix();
    }

    void renderElement(Element element){
        Vector3f from = element.from;
        Vector3f to = element.to;
        glPushMatrix();
        glScalef(.25f, .25f, .25f);
        glTranslatef(-8f, -8f, -8f);
        glBegin(GL_QUADS);
        // glTexCoord2f(0, 0);
        // glVertex3f(element.from.x, element.from.y, element.from.z);
        // glTexCoord2f(0, 1);
        // glVertex3f(-16, 0, 32);
        // glTexCoord2f(1, 1);
        // glVertex3f(32, 0, 32);
        // glTexCoord2f(1, 0);
        // glVertex3f(32, 0, -16);
        Vector2f uv_to = new Vector2f(0, 1);
        Vector2f uv_from = new Vector2f(1, 0);
        glColor3f(.43f, .43f, .43f);
        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(from.x, from.y, from.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(from.x, to.y, from.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(to.x, to.y, from.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(to.x, from.y, from.z);
        glColor3f(1, 1, 1);

        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(to.x, from.y, to.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(to.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(from.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(from.x, from.y, to.z);

        glColor3f(.63f, .63f, .63f);
        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(to.x, from.y, from.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(to.x, to.y, from.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(to.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(to.x, from.y, to.z);
        glColor3f(1, 1, 1);

        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(from.x, from.y, to.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(from.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(from.x, to.y, from.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(from.x, from.y, from.z);

        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(to.x, from.y, from.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(to.x, from.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(from.x, from.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(from.x, from.y, from.z);

        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(from.x, to.y, from.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(from.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(to.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(to.x, to.y, from.z);

        glEnd();
        glPopMatrix();
    }

    public void screenshot(){
        queue.add(()->{
            glViewport(0, 0, 32, 32);
            glfwSwapBuffers(context);
    
            ByteBuffer buffer = ByteBuffer.allocateDirect(32 * 32 * 4);
    
            glReadPixels(0, 0, 32, 32, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    
            screenshot(buffer);
            System.out.println("SCREENSHOT");
        });
    }

    void screenshot(ByteBuffer buffer){
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++){
                int i = (x + (image.getWidth() * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i+1) & 0xFF;
                int b = buffer.get(i+2) & 0xFF;
                image.setRGB(x, image.getWidth() - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            File out = new File("out.png");
            System.out.println(out.getAbsolutePath());
            ImageIO.write(image, "PNG", out);
        } catch(IOException e) {e.printStackTrace();}
    }

    void perspectiveGL( double fovY, double aspect, double zNear, double zFar ) {
        double fW, fH;

        fH = Math.tan( fovY / 360 * Math.PI ) * zNear;
        fW = fH * aspect;

        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }
}