package xyz.rodeldev.jmv;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.naming.spi.DirStateFactory.Result;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import xyz.rodeldev.jmv.model.Element;
import xyz.rodeldev.jmv.model.Model;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.*;

public class Renderer{
    private long context;

    public float zoom = -187;

    public boolean isOrtho = true;
    public boolean needsUpdate = false;

    public float rx = 30, ry = 225, rz;

    private int floor_texture;

    private Element element;

    int fbo, texture;

    public BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public boolean pixel = false;
    public int resolution = 32;

    public Model model;

    public void createContext(long context){

        System.out.println(glGetString(GL_VERSION));

        this.context = context;

        try(MemoryStack stack = stackPush()) {
            IntBuffer draw_fb = stack.mallocInt(1);
            glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, draw_fb);
    
            System.out.println("Current buffer: "+draw_fb.get(0));
        } catch(Exception e){
            e.printStackTrace();
        }

        glEnable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        updatePerspective();
        glViewport(0, 0, resolution, resolution);
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // glEnable(GL_CULL_FACE);
        glClearColor(0f, 0f, 0f, 0f);
        glColorMask(true, true, true, true);

        int renderbuf = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuf);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, resolution, resolution);

        int depthbuf = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthbuf);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, resolution, resolution);

        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
        glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderbuf);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthbuf);

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, resolution, resolution, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        // glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, 32, 32, 0, GL_DEPTH_COMPONENT24, GL_UNSIGNED_BYTE, NULL);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        // glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture, 0);

        try(MemoryStack stack = stackPush()) {
            IntBuffer draw_fb = stack.mallocInt(1);
            glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, draw_fb);
            IntBuffer dst = stack.mallocInt(1);
            glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, draw_fb);
    
            System.out.println("Current buffer: "+draw_fb.get(0));
            System.out.println("Current buffer: "+draw_fb.get(0));
        } catch(Exception e){
            e.printStackTrace();
        }


        // Textures
        floor_texture = TextureLoader.loadTexture("/dirt.png");
        // floor_texture = TextureLoader.loadTexture("/white_stained_glass.png");

        element = new Element(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));
    }

    public void updatePerspective(){
        perspectiveGL(2, 1, .3f, 10000);
    }
    
    public void resize(int width, int height){
        needsUpdate = true;
    }

    public void render(int width, int height){
        glBindTexture(GL_TEXTURE_2D, floor_texture);
        glBindFramebuffer(GL_FRAMEBUFFER, pixel ? fbo : 0);
        // glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        // glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuf);

        // if(needsUpdate){
            // glViewport(0, 0, resolution, resolution);
            glViewport(0, 0, pixel ? resolution : width, pixel ? resolution : height);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            updatePerspective();
            // if(isOrtho) {
            //     perspectiveGL(2, 1, .3f, 10000);
            // }else{
            //     perspectiveGL(60, 1, .3f, 100);
            // }
            glMatrixMode(GL_MODELVIEW);
            // needsUpdate = false;
        // }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        glTranslatef(0, 0, zoom);

        glRotatef(rx, 1, 0, 0);
        glRotatef(ry, 0, 1, 0);
        glRotatef(rz, 0, 0, 1);

        // glDisable(GL_TEXTURE_2D);

        // renderFloor();

        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        // glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        // glDepthMask(true);
        // glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        if(this.model!=null){
            for(Element element : this.model.flatElement()){
                renderElement(element);
            }
        }
        // renderElement(element);

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

        // screenshot();
        // System.out.println("RENDER!");

        while(!queue.isEmpty()){
            try {
                queue.take().run();
            } catch(Exception e) {e.printStackTrace();}
        }

        // System.exit(1);

        if(pixel){
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glPushMatrix();
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, texture);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glViewport(0, 0, width, height);
            // perspectiveGL(2, 1, .3f, 10000);
            glOrtho(-1f, 1f, -1f, 1f, -1f, 1f);
            // glOrtho(0f, 10f, 10f, 0f, -1f, 1f);
            glBegin(GL_QUADS);
            glTexCoord2f(0, 1);
            glVertex3f(-1f,  1f, 0f);
            glTexCoord2f(0, 0);
            glVertex3f(-1f, -1f, 0f);
            glTexCoord2f(1, 0);
            glVertex3f( 1f, -1f, 0f);
            glTexCoord2f(1, 1);
            glVertex3f( 1f,  1f, 0f);
            // glVertex3f(-1f, -1f, 0f);
            // glVertex3f(-1f, 1f, 0f);
            // glVertex3f(1f, 1f, 0f);
            // glVertex3f(1f, -1f, 0f);
            glEnd();
            glPopMatrix();
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
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

        glTexCoord2f(uv_to.x, uv_to.y);
        glVertex3f(from.x, to.y, from.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(from.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(to.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(to.x, to.y, from.z);

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
        glVertex3f(to.x, from.y, to.z);
        glTexCoord2f(uv_to.x, uv_from.y);
        glVertex3f(to.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_from.y);
        glVertex3f(from.x, to.y, to.z);
        glTexCoord2f(uv_from.x, uv_to.y);
        glVertex3f(from.x, from.y, to.z);

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

        glEnd();
        glPopMatrix();
    }

    public void screenshot(){
        queue.add(()->{
            glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
            glReadBuffer(GL_COLOR_ATTACHMENT0);

            ByteBuffer buffer1 = ByteBuffer.allocateDirect(resolution * resolution * 4);

            glDrawPixels(32, 32, GL_RGBA, GL_UNSIGNED_BYTE, buffer1);

            ByteBuffer buffer = ByteBuffer.allocateDirect(resolution * resolution * 4);
    
            // glEnable(GL_ALPHA_TEST);
            // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            // glBlendFunc(GL_SRC_ALPHA_SATURATE, GL_ONE);
            // glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            glReadPixels(0, 0, resolution, resolution, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    
            // System.out.println(buffer.get(0));
            // System.out.println(buffer.get(1));
            // System.out.println(buffer.get(2));
            // System.out.println(buffer.get(3));
            screenshot(buffer);
            System.out.println("SCREENSHOT");
        });
    }

    void screenshot(ByteBuffer buffer){
        BufferedImage image = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);

        // for (int x = 0; x < image.getWidth(); x++) {
        //     for(int y = 0; y < image.getHeight(); y++){
        //         int i = (x + (image.getWidth() * y)) * 4;
        //         int r = buffer.get(i) & 0xFF;
        //         int g = buffer.get(i+1) & 0xFF;
        //         int b = buffer.get(i+2) & 0xFF;
        //         System.out.println(buffer.get(i+3));
        //         image.setRGB(x, image.getWidth() - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
        //     }
        // }

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setComposite(AlphaComposite.Src);

        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                int i = (x + (image.getWidth() * y)) * 4;
                System.out.println(x+" "+y+" "+( buffer.get(i+3) & 0xFF));
                graphics.setColor(new Color(buffer.get(i) & 0xFF, buffer.get(i+1) & 0xFF, buffer.get(i+2) & 0xFF, buffer.get(i+3) & 0xFF));
                graphics.fillRect(x, image.getWidth() - (y + 1), 1, 1);
            }
        }

        graphics.dispose();

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