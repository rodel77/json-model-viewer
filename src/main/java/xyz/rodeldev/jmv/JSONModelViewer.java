package xyz.rodeldev.jmv;

import static org.lwjgl.glfw.GLFW.glfwInit;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lwjgl.glfw.*;

import xyz.rodeldev.jmv.model.Model;

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

	public static String assets_folder = "";
	private static String block = "air";

	public static File models_folder;
	public static File textures_folder;

	// public String assets_folder = "C:\Users\Usuario\AppData\Roaming\.minecraft\versions\1.14.4\assets\minecraft";

    public void start() throws Exception{
		System.out.println("Starting...");

		renderer = new Renderer();
		
		GLFWErrorCallback.createPrint().set();
		if(!glfwInit()) {
			throw new IllegalStateException("Unable to init glfw");
		}

		models_folder = new File(assets_folder, "models");
		textures_folder = new File(assets_folder, "textures");

		Model model = new Model(new File(models_folder.getAbsolutePath(), "block/"+block+".json"));
		renderer.model = model;

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

    public static void main( String[] args ) {
		Options options = new Options();
		options.addRequiredOption("a", "assets", true, "Minecraft assets root folder");
		options.addOption("n", "name", true, "Block/item name");
	
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			assets_folder = cmd.getOptionValue("assets");

			if(cmd.hasOption("name")){
				block = cmd.getOptionValue("name");
			}
			
			instance = new JSONModelViewer();
			instance.start();
		} catch(ParseException e){
			HelpFormatter help = new HelpFormatter();
			help.printHelp("model_generator", options);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
