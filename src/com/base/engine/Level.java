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
	private ArrayList<Monster> monsters;
	private ArrayList<Medkit> medkits;
	private ArrayList<Vector3f> exitPoints;

	private ArrayList<Medkit> medkitsToRemove;

	private ArrayList<Vector2f> collisionPosStart;
	private ArrayList<Vector2f> collisionPosEnd;

	//WARNING: TEMP VARIABLE!
	//private Monster monster;

	public Player getPlayer()
	{
		return player;
	}

	public Level(String levelName, String textureName)
	{
		//this.player = player;
		medkitsToRemove = new ArrayList<Medkit>();
		level = new Bitmap(levelName).flipY();
		material = new Material(new Texture(textureName));
		transform = new Transform();
		
		shader = BasicShader.getInstance();

		exitPoints = new ArrayList<Vector3f>();

		generateLevel();
//		Transform tempTransform = new Transform();
//		tempTransform.setTranslation(new Vector3f(12,0,12));

		//monsters.add(new Monster(tempTransform));
		//door = new Door(tempTransform, material);
	}

	public void openDoors(Vector3f position, boolean tryExitLevel)
	{
		for(Door door : doors)
		{
			if(door.getTransform().getTranslation().sub(position).length() < OPEN_DISTANCE)
			{
				door.open();
			}
		}

		if(tryExitLevel)
		{
			for(Vector3f exitPoint : exitPoints)
				if(exitPoint.sub(position).length() < OPEN_DISTANCE)
					Game.loadNextLevel();
		}
	}

	public void damagePlayer(int amt)
	{
		player.damage(amt);
	}

	public void input()
	{
		player.input();
	}
	
	public void update()
	{
		for(Door door : doors)
			door.update();

		player.update();

		for(Medkit medkit : medkits)
			medkit.update();

		for(Monster monster : monsters)
			monster.update();

		for(Medkit medkit : medkitsToRemove)
			medkits.remove(medkit);
	}
	
	public void render()
	{
		shader.bind();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
		for(Door door : doors)
			door.render();

		for(Monster monster : monsters)
			monster.render();

		for(Medkit medkit : medkits)
			medkit.render();

		player.render();
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

	public Vector2f checkIntersections(Vector2f lineStart, Vector2f lineEnd, boolean hurtMonsters)
	{
		Vector2f nearestIntersection = null;

		for(int i = 0; i < collisionPosStart.size(); i++)
		{
			Vector2f collisionVector = lineIntersect(lineStart, lineEnd, collisionPosStart.get(i), collisionPosEnd.get(i));
			nearestIntersection = findNearestVector2f(nearestIntersection, collisionVector, lineStart);
		}

		for(Door door : doors)
		{
			Vector2f doorSize = door.getDoorSize();
			Vector3f doorPos3f = door.getTransform().getTranslation();
			Vector2f doorPos2f = new Vector2f(doorPos3f.getX(), doorPos3f.getZ());
			Vector2f collisionVector = lineIntersectRect(lineStart, lineEnd, doorPos2f, doorSize);

			nearestIntersection = findNearestVector2f(nearestIntersection, collisionVector, lineStart);
		}

		if(hurtMonsters)
		{
			Vector2f nearestMonsterIntersect = null;
			Monster nearestMonster = null;

			for(Monster monster : monsters)
			{
				Vector2f monsterSize = monster.getSize();
				Vector3f monsterPos3f = monster.getTransform().getTranslation();
				Vector2f monsterPos2f = new Vector2f(monsterPos3f.getX(), monsterPos3f.getZ());
				Vector2f collisionVector = lineIntersectRect(lineStart, lineEnd, monsterPos2f, monsterSize);

				nearestMonsterIntersect = findNearestVector2f(nearestMonsterIntersect, collisionVector, lineStart);

				if(nearestMonsterIntersect == collisionVector)
					nearestMonster = monster;
			}

			if(nearestMonsterIntersect != null && (nearestIntersection == null ||
					nearestMonsterIntersect.sub(lineStart).length() < nearestIntersection.sub(lineStart).length()))
			{
				if(nearestMonster != null)
					nearestMonster.damage(player.getDamage());
			}
		}

		return nearestIntersection;
	}

	private Vector2f findNearestVector2f(Vector2f a, Vector2f b, Vector2f positionRelativeTo)
	{
		if(b != null && (a == null ||
			a.sub(positionRelativeTo).length() > b.sub(positionRelativeTo).length()))
			return b;

		return a;
	}

	public Vector2f lineIntersectRect(Vector2f lineStart, Vector2f lineEnd, Vector2f rectPos, Vector2f rectSize)
	{
		Vector2f result = null;

		Vector2f collisionVector = lineIntersect(lineStart, lineEnd, rectPos, new Vector2f(rectPos.getX() + rectSize.getX(), rectPos.getY()));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, rectPos, new Vector2f(rectPos.getX(), rectPos.getY() + rectSize.getY()));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, new Vector2f(rectPos.getX(), rectPos.getY() + rectSize.getY()), rectPos.add(rectSize));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, new Vector2f(rectPos.getX() + rectSize.getX(), rectPos.getY()), rectPos.add(rectSize));
		result = findNearestVector2f(result, collisionVector, lineStart);

		return result;
	}

	private float Vector2fCross(Vector2f a, Vector2f b)
	{
		return a.getX() * b.getY() - a.getY() * b.getX();
	}

	//http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	private Vector2f lineIntersect(Vector2f lineStart1, Vector2f lineEnd1, Vector2f lineStart2, Vector2f lineEnd2)
	{
		Vector2f line1 = lineEnd1.sub(lineStart1);
		Vector2f line2 = lineEnd2.sub(lineStart2);

		//lineStart1 + line1 * a == lineStart2 + line2 * b

		float cross = Vector2fCross(line1, line2);

		if(cross == 0)
			return null;

		Vector2f distanceBetweenLineStarts = lineStart2.sub(lineStart1);

		float a = Vector2fCross(distanceBetweenLineStarts, line2) / cross;
		float b = Vector2fCross(distanceBetweenLineStarts, line1) / cross;

		if(0.0f < a && a < 1.0f && 0.0f < b && b < 1.0f)
			return lineStart1.add(line1.mul(a));

		return null;
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
		if(blueValue == 1)
			player = new Player(new Vector3f((x + 0.5f) * SPOT_WIDTH, 0.4375f, (y + 0.5f) * SPOT_LENGTH));
		if(blueValue == 128)
		{
			Transform monsterTransform = new Transform();
			monsterTransform.setTranslation(new Vector3f((x + 0.5f) * SPOT_WIDTH, 0, (y + 0.5f) * SPOT_LENGTH));
			monsters.add(new Monster(monsterTransform));
		}
		if(blueValue == 192)
			medkits.add(new Medkit(new Vector3f((x + 0.5f) * SPOT_WIDTH, 0, (y + 0.5f) * SPOT_LENGTH)));
		if(blueValue == 97)
			exitPoints.add(new Vector3f((x + 0.5f) * SPOT_WIDTH, 0, (y + 0.5f) * SPOT_LENGTH));
	}

	private void generateLevel()
	{
		doors = new ArrayList<Door>();
		monsters = new ArrayList<Monster>();
		medkits = new ArrayList<Medkit>();
		collisionPosStart = new ArrayList<Vector2f>();
		collisionPosEnd = new ArrayList<Vector2f>();

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
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, j * SPOT_LENGTH));
					addFace(indices, vertices.size(), false);
					addVertices(vertices, i, 0, j, true, true, false, texCoords);
				}
				if((level.getPixel(i,j + 1) & 0xFFFFFF) == 0)
				{
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					addFace(indices, vertices.size(), true);
					addVertices(vertices, i, 0, (j + 1), true, true, false, texCoords);
				}
				if((level.getPixel(i - 1,j) & 0xFFFFFF) == 0)
				{
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f(i * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					addFace(indices, vertices.size(), true);
					addVertices(vertices, 0, j, i, false, true, true, texCoords);
				}
				if((level.getPixel(i + 1,j) & 0xFFFFFF) == 0)
				{
					collisionPosStart.add(new Vector2f((i + 1) * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					addFace(indices, vertices.size(), false);
					addVertices(vertices, 0, j, (i + 1), false, true, true, texCoords);
				}
			}
		}

		//WARNING: DEBUG CODE!

//		vertices.clear();
//		indices.clear();
//
//		for(int i = 0; i < collisionPosStart.size(); i++)
//		{
//			Vector2f lineStart = collisionPosStart.get(i);
//			Vector2f lineEnd = collisionPosEnd.get(i);
//
//			indices.add(vertices.size() + 0);
//			indices.add(vertices.size() + 1);
//			indices.add(vertices.size() + 2);
//			indices.add(vertices.size() + 0);
//			indices.add(vertices.size() + 2);
//			indices.add(vertices.size() + 3);
//
//			indices.add(vertices.size() + 2);
//			indices.add(vertices.size() + 1);
//			indices.add(vertices.size() + 0);
//			indices.add(vertices.size() + 3);
//			indices.add(vertices.size() + 2);
//			indices.add(vertices.size() + 0);
//
//			vertices.add(new Vertex(new Vector3f(lineStart.getX(), 0, lineStart.getY()), new Vector2f(0, 0)));
//			vertices.add(new Vertex(new Vector3f(lineStart.getX(), 1, lineStart.getY()), new Vector2f(0, 1)));
//			vertices.add(new Vertex(new Vector3f(lineEnd.getX(), 1, lineEnd.getY()), new Vector2f(1, 1)));
//			vertices.add(new Vertex(new Vector3f(lineEnd.getX(), 0, lineEnd.getY()), new Vector2f(1, 0)));
//		}

		//END DEBUG CODE!

		Vertex[] vertArray = new Vertex[vertices.size()];
		Integer[] intArray = new Integer[indices.size()];
		
		vertices.toArray(vertArray);
		indices.toArray(intArray);
		
		mesh = new Mesh(vertArray, Util.toIntArray(intArray));
	}

	public void removeMedkit(Medkit medkit)
	{
		medkitsToRemove.add(medkit);
	}

	public Shader getShader()
	{
		return shader;
	}
}
