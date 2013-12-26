package com.base.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glTexImage2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class Texture
{
	private int id;
	
	public Texture(String fileName)
	{
		this(loadTexture(fileName));
	}
	
	public Texture(int id)
	{
		this.id = id;
	}
	
	public void bind()
	{
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public int getID()
	{
		return id;
	}
	
	private static int loadTexture(String fileName)
	{
		String[] splitArray = fileName.split("\\.");
		String ext = splitArray[splitArray.length - 1];
		
		try
		{
			BufferedImage image = ImageIO.read(new File("./res/textures/" + fileName));

			boolean hasAlpha = image.getColorModel().hasAlpha();

			int[] pixels = image.getRGB(0, 0, image.getWidth(),
										image.getHeight(), null, 0, image.getWidth());

			ByteBuffer buffer = Util.createByteBuffer(image.getWidth() * image.getHeight() * 4);

			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					int pixel = pixels[y * image.getWidth() + x];

					buffer.put((byte) ((pixel >> 16) & 0xFF));
					buffer.put((byte) ((pixel >> 8) & 0xFF));
					buffer.put((byte) ((pixel >> 0) & 0xFF));
					if (hasAlpha)
						buffer.put((byte) ((pixel >> 24) & 0xFF));
					else
						buffer.put((byte) (0xFF));
				}
			}

			buffer.flip();

//			this.width = image.getWidth();
//			this.height = image.getHeight();
//			this.id = Engine.getRenderer().createTexture(width, height, buffer, true, true);
//			this.frameBuffer = 0;
//			this.pixels = null;

			int texture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, texture);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

			return texture;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
}
