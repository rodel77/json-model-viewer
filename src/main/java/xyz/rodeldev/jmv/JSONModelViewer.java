package xyz.rodeldev.jmv;

import static org.lwjgl.glfw.GLFW.glfwInit;

import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Hello world!
 *
 */
public class JSONModelViewer {

	public static JSONModelViewer instance;
	public LWJGLCanvas canvas;
	private Renderer renderer;

	private boolean glfw = true;
	private boolean headless = false;

    public void start(){
		System.out.println("Starting...");

		renderer = new Renderer();
		
		GLFWErrorCallback.createPrint().set();
		if(!glfwInit()) {
			throw new IllegalStateException("Unable to init glfw");
		}

		if(glfw){
			GLFWCanvas canvas = new GLFWCanvas();
			// System.out.println(glfwGetCurrentContext());
			long window = canvas.initWindow();
			glfwMakeContextCurrent(window);
			renderer.createContext(window);
			if(headless){
				renderer.screenshot();
				renderer.render(640, 640);
			}else{
				while(!glfwWindowShouldClose(window)){
					glfwSetKeyCallback(window, new GLFWKeyCallback(){
						@Override
						public void invoke(long window, int key, int scancode, int action, int mods) {
							if(action==GLFW_PRESS){
								if(key==GLFW_KEY_Z){
									renderer.screenshot();
								}
								if(key==GLFW_KEY_ESCAPE){
									glfwSetWindowShouldClose(window, true);
								}
							}
						}
					});
					glfwPollEvents();
					renderer.render(640, 640);
					glfwSwapBuffers(window);
				}
			}
			glfwTerminate();
		}else{
			JFrame frame = new JFrame("JSONModelViewer");
	
			canvas = new LWJGLCanvas(renderer);
			canvas.setSize(640, 640);
	
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter(){
				@Override
				public void windowClosed(WindowEvent e) {
					canvas.destroy();
				}
			});
	
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE && e.getID()==KeyEvent.KEY_PRESSED){
					frame.dispose();
					glfwTerminate();
					Objects.requireNonNull(glfwSetErrorCallback(null)).free();
					return true;
				}
				return false;
			});
	
			frame.setLayout(new BorderLayout());
			frame.add(canvas, BorderLayout.CENTER);
	
			frame.pack();
			frame.setVisible(true);
		}
	}

    public static void main( String[] args )
    {
        instance = new JSONModelViewer();
        instance.start();
    }
}
