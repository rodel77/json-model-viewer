package xyz.rodeldev.jmv;

import java.awt.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.jawt.*;
import org.lwjgl.system.linux.*;

import java.awt.*;
import java.awt.event.*;
import java.nio.*;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.*;
import static org.lwjgl.opengl.GLX13.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.jawt.JAWTFunctions.*;

public class LWJGLCanvas extends Canvas {
    private final JAWT awt;

    private long context;

    private JAWTDrawingSurface drawingSurface;

    private GLCapabilities capabilities;

    private Renderer renderer;

    public LWJGLCanvas(Renderer renderer) {
        this.renderer = renderer;
        awt = JAWT.calloc();
        awt.version(JAWT_VERSION_1_4);
        if(!JAWT_GetAWT(awt)) throw new IllegalStateException("GetAWT failed");

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                if(context!=NULL){
                    paint();
                    renderer.resize(getWidth(), getHeight());
                }
            }
        });

        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){
                    case GLFW_KEY_O:
                        renderer.isOrtho = !renderer.isOrtho;
                        renderer.needsUpdate = true;
                        break;
                    case GLFW_KEY_D:
                        renderer.ry += 1;
                        break;
                    case GLFW_KEY_A:
                        renderer.ry -= 1;
                        break;
                    case GLFW_KEY_W:
                        renderer.rx += 1;
                        break;
                    case GLFW_KEY_S:
                        renderer.rx -= 1;
                        break;
                    case GLFW_KEY_Z:
                        renderer.screenshot();
                        break;
                    default: break;
                }
            }
        });

        addMouseWheelListener(e -> {
            renderer.zoom -= e.getWheelRotation();
        });
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        paint();
        repaint();
    }

    private void paint(){
        if(drawingSurface==null){
            drawingSurface = JAWT_GetDrawingSurface(this, awt.GetDrawingSurface());
            if(drawingSurface==null) throw new IllegalStateException("awt->GetDrawingSurface() failed");
        }

        int lock = JAWT_DrawingSurface_Lock(drawingSurface, drawingSurface.Lock());
        if((lock & JAWT_LOCK_ERROR)!=0) throw new IllegalStateException("ds->Lock() failed");

        try {
            JAWTDrawingSurfaceInfo drawingSurfaceInfo = JAWT_DrawingSurface_GetDrawingSurfaceInfo(drawingSurface, drawingSurface.GetDrawingSurfaceInfo());
            if(drawingSurfaceInfo == null) throw new IllegalStateException("ds->GetDrawingSurfaceInfo() failed");

            try {
                // In case you want to implement LINUX: https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/system/jawt/LWJGLCanvas.java
                switch (Platform.get()) {
                    case WINDOWS:
                        JAWTWin32DrawingSurfaceInfo drawingSurfaceInfoWin32 = JAWTWin32DrawingSurfaceInfo.create(drawingSurfaceInfo.platformInfo());

                        long hdc = drawingSurfaceInfoWin32.hdc();
                        if(hdc==NULL) break;

                        if(context==NULL){
                            createContextGLFW(drawingSurfaceInfoWin32);
                            renderer.createContext(context, this);
                        }else{
                            glfwMakeContextCurrent(context);
                            GL.setCapabilities(capabilities);
                        }

                        try(MemoryStack stack = stackPush()) {
                            IntBuffer pw = stack.mallocInt(1);
                            IntBuffer ph = stack.mallocInt(1);

                            glfwGetFramebufferSize(context, pw, ph);

                            render(pw.get(0), ph.get(0));
                        }

                        glfwSwapBuffers(context);
                        glfwMakeContextCurrent(context);
                        GL.setCapabilities(null);

                        break;
                    default:
                        throw new IllegalStateException("This OS is not supported!");
                }
            } finally {
                JAWT_DrawingSurface_FreeDrawingSurfaceInfo(drawingSurfaceInfo, drawingSurface.FreeDrawingSurfaceInfo());
            }

        } finally {
            JAWT_DrawingSurface_Unlock(drawingSurface, drawingSurface.Unlock());
        }
    }

    private void render(int width, int height){
        renderer.render(width, height);
        // System.out.println("Render");
    }

    private void createContextGLFW(JAWTWin32DrawingSurfaceInfo drawingSurfaceInfoWin32){
        context = glfwAttachWin32Window(drawingSurfaceInfoWin32.hwnd(), NULL);
        if(context==NULL) throw new IllegalStateException("Failed to attach win32 window.");

        glfwMakeContextCurrent(context);
        capabilities = GL.createCapabilities();
    }

    public void destroy(){
        JAWT_FreeDrawingSurface(drawingSurface, awt.FreeDrawingSurface());
        awt.free();
        if(context!=NULL){
            glfwDestroyWindow(context);
        }
    }
}