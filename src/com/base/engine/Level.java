package com.base.engine;

import java.util.ArrayList;

public class Level
{
	private static final float SPOT_WIDTH = 1;
	private static final float SPOT_LENGTH = 1;
	private static final float SPOT_HEIGHT = 1;
	
	private static final int NUM_TEX_EXP = 4;
	private static final int NUM_TEXTURES = (int)Math.pow(2, NUM_TEX_EXP);
	private static final float OPEN_DISTANCE = 1.0f;
	private static final float DOOR_OPEN_MOVEMENT_AMOUNT = 0.9f;

	private Mesh mesh;
	private Bitmap level;
	private Shader shader;
	private Material material;
	private Transform transform;
	private Player player;
	private ArrayList<Door> doors;

	//WARNING: TEMP VARIABLE!
	private Monster monster;

	
	public Level(String levelName, String textureName, Player player)
	{
		this.player = player;
		level = new Bitmap(levelName).flipY();
		material = new Material(new Texture(textureName));
		transform = new Transform();
		
		shader = BasicShader.getInstance();
		doors = new ArrayList<Door>();

		generateLevel();
		Transform tempTransform = new Transform();
		tempTransform.setTranslation(new Vector3f(8,0,8));

		monster = new Monster(tempTransform);
		//door = new Door(tempTransform, material);

	}
	
	public void input()
	{
		if(Input.getKeyDown(Input.KEY_E))
		{
			for(Door door : doors)
			{
				if(door.getTransform().getTranslation().sub(player.getCamera().getPos()).length() < OPEN_DISTANCE)
				{
					door.open();
				}
			}
		}

		player.input();
	}
	
	public void update()
	{
		for(Door door : doors)
			door.update();

		player.update();
		monster.update();
	}
	
	public void render()
	{
		shader.bind();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
		for(Door door : doors)
			door.render();

		player.render();
		monster.render();
	}
	
	public Vector3f checkCollision(Vector3f oldPos, Vector3f newPos, float objectWidth, float objectLength)
	{
		Vector2f collisionVector = new Vector2f(1,1);
		Vector3f movementVector = newPos.sub(oldPos);
		
		if(movementVector.length() > 0)
		{
			Vector2f blockSize = new Vector2f(SPOT_WIDTH, SPOT_LENGTH);
			Vector2f objectSize = new Vector2f(objectWidth, objectLength);
			
			Vector2f oldPos2 = new Vector2f(oldPos.getX(), oldPos.getZ());
			Vector2f newPos2 = new Vector2f(newPos.getX(), newPos.getZ());
			
			for(int i = 0; i < level.getWidth(); i++)
				for(int j = 0; j < level.getHeight(); j++)
					if((level.getPixel(i,j) & 0xFFFFFF) == 0)
						collisionVector = collisionVector.mul(rectCollide(oldPos2, newPos2, objectSize, blockSize.mul(new Vector2f(i,j)), blockSize));


			for(Door door : doors)
			{
				Vector2f doorSize = door.getDoorSize();
				Vector3f doorPos3f = door.getTransform().getTranslation();
				Vector2f doorPos2f = new Vector2f(doorPos3f.getX(), doorPos3f.getZ());
				collisionVector = collisionVector.mul(rectCollide(oldPos2, newPos2, objectSize, doorPos2f, doorSize));
			}
		}
		
		return new Vector3f(collisionVector.getX(), 0, collisionVector.getY());
	}
	
	private Vector2f rectCollide(Vector2f oldPos, Vector2f newPos, Vector2f size1, Vector2f pos2, Vector2f size2)
	{
		Vector2f result = new Vector2f(0,0);
		
		if(newPos.getX() + size1.getX() < pos2.getX() ||
		   newPos.getX() - size1.getX() > pos2.getX() + size2.getX() * size2.getX() ||
		   oldPos.getY() + size1.getY() < pos2.getY() ||
		   oldPos.getY() - size1.getY() > pos2.getY() + size2.getY() * size2.getY())
			result.setX(1);
		
		if(oldPos.getX() + size1.getX() < pos2.getX() ||
		   oldPos.getX() - size1.getX() > pos2.getX() + size2.getX() * size2.getX() ||
		   newPos.getY() + size1.getY() < pos2.getY() ||
		   newPos.getY() - size1.getY() > pos2.getY() + size2.getY() * size2.getY())
			result.setY(1);
		
		return result;
	}
	
	private void addFace(ArrayList<Integer> indices, int startLocation, boolean direction)
	{
		if(direction)
		{
			indices.add(startLocation + 2);
			indices.add(startLocation + 1);
			indices.add(startLocation + 0);
			indices.add(startLocation + 3);
			indices.add(startLocation + 2);
			indices.add(startLocation + 0);
		}
		else
		{
			indices.add(startLocation + 0);
			indices.add(startLocation + 1);
			indices.add(startLocation + 2);
			indices.add(startLocation + 0);
			indices.add(startLocation + 2);
			indices.add(startLocation + 3);
		}
	}
	
	private float[] calcTexCoords(int value)
	{
		int texX = value / NUM_TEXTURES;
		int texY = texX % NUM_TEX_EXP;
		texX /= NUM_TEX_EXP;
		
		float[] result = new float[4];
		
		result[0] = 1f - (float)texX/(float)NUM_TEX_EXP;
		result[1] = result[0] - 1f/(float)NUM_TEX_EXP;
		result[3] = 1f - (float)texY/(float)NUM_TEX_EXP;
		result[2] = result[3] - 1f/(float)NUM_TEX_EXP;
		
		return result;
	}
	
	private void addVertices(ArrayList<Vertex> vertices, int i, int j, float offset, boolean x, boolean y, boolean z, float[] texCoords)
	{
		if(x && z)
		{
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, offset * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, offset * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, offset * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, offset * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		}
		else if(x && y)
		{
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, j * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, j * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, (j + 1) * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, (j + 1) * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		}
		else if(y && z)
		{
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, i * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, i * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, (i + 1) * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, (i + 1) * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		}
		else
		{
			System.err.println("Invalid plane used in level generator");
			new Exception().printStackTrace();
			System.exit(1);
		}
	}

	private void addDoor(int x, int y)
	{
		Transform doorTransform = new Transform();

		boolean xDoor = (level.getPixel(x, y - 1) & 0xFFFFFF) == 0 && (level.getPixel(x, y + 1) & 0xFFFFFF) == 0;
		boolean yDoor = (level.getPixel(x - 1, y) & 0xFFFFFF) == 0 && (level.getPixel(x + 1, y) & 0xFFFFFF) == 0;

		if(!(xDoor ^ yDoor))
		{
			System.err.println("Level Generation has failed! :( You placed a door in an invalid location at " + x + ", " + y);
			new Exception().printStackTrace();
			System.exit(1);
		}

		Vector3f openPosition = null;

		if(yDoor)
		{
			doorTransform.setTranslation(x, 0, y + SPOT_LENGTH / 2);
			openPosition = doorTransform.getTranslation().sub(new Vector3f(DOOR_OPEN_MOVEMENT_AMOUNT, 0.0f, 0.0f));
		}

		if(xDoor)
		{
			doorTransform.setTranslation(x + SPOT_WIDTH / 2, 0, y);
			doorTransform.setRotation(0, 90, 0);
			openPosition = doorTransform.getTranslation().sub(new Vector3f(0.0f, 0.0f, DOOR_OPEN_MOVEMENT_AMOUNT));
		}

		doors.add(new Door(doorTransform, material, openPosition));
	}

	private void addSpecial(int blueValue, int x, int y)
	{
		if(blueValue == 16)
			addDoor(x, y);
	}

	private void generateLevel()
	{
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for(int i = 0; i < level.getWidth(); i++)
		{
			for(int j = 0; j < level.getHeight(); j++)
			{
				if((level.getPixel(i,j) & 0xFFFFFF) == 0)
					continue;
				
				float[] texCoords = calcTexCoords((level.getPixel(i,j) & 0x00FF00) >> 8);

				addSpecial((level.getPixel(i,j) & 0x0000FF), i , j);

				//Generate Floor
				addFace(indices, vertices.size(), true);
				addVertices(vertices, i, j, 0, true, false, true, texCoords);
				
				//Generate Ceiling
				addFace(indices, vertices.size(), false);
				addVertices(vertices, i, j, 1, true, false, true, texCoords);
			
				//Generate Walls
				texCoords = calcTexCoords((level.getPixel(i,j) & 0xFF0000) >> 16);
				
				if((level.getPixel(i,j - 1) & 0xFFFFFF) == 0)
				{
					addFace(indices, vertices.size(), false);
					addVertices(vertices, i, 0, j, true, true, false, texCoords);
				}
				if((level.getPixel(i,j + 1) & 0xFFFFFF) == 0)
				{
					addFace(indices, vertices.size(), true);
					addVertices(vertices, i, 0, (j + 1), true, true, false, texCoords);
				}
				if((level.getPixel(i - 1,j) & 0xFFFFFF) == 0)
				{
					addFace(indices, vertices.size(), true);
					addVertices(vertices, 0, j, i, false, true, true, texCoords);
				}
				if((level.getPixel(i + 1,j) & 0xFFFFFF) == 0)
				{
					addFace(indices, vertices.size(), false);
					addVertices(vertices, 0, j, (i + 1), false, true, true, texCoords);
				}
			}
		}
		
		Vertex[] vertArray = new Vertex[vertices.size()];
		Integer[] intArray = new Integer[indices.size()];
		
		vertices.toArray(vertArray);
		indices.toArray(intArray);
		
		mesh = new Mesh(vertArray, Util.toIntArray(intArray));
	}
	
	public Shader getShader()
	{
		return shader;
	}
}
