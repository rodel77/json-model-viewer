package xyz.rodeldev.jmv;

import java.io.InputStream;
import java.nio.ByteBuffer;


import de.matthiasmann.twl.utils.PNGDecoder;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL40.*;

public class TextureLoader {
    public static int loadTexture(InputStream inputStream){
        try {
            PNGDecoder decoder = new PNGDecoder(inputStream);

            ByteBuffer buffer = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());

            decoder.decode(buffer, decoder.getWidth()*4, PNGDecoder.Format.RGBA);

            buffer.flip();

            int id = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, id);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            glGenerateMipmap(GL_TEXTURE_2D);

            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int loadTexture(String localPath){
        return loadTexture(TextureLoader.class.getResourceAsStream(localPath));
    }
}