package com.base.engine;

import java.util.Random;

public class Player
{
	private static final float MOUSE_SENSITIVITY = 0.33f;
	private static final float MOVE_SPEED = 5f;
	public static final float PLAYER_SIZE = 0.2f;
	public static final float SHOOT_DISTANCE = 1000.0f;
	private static final Vector3f zeroVector = new Vector3f(0,0,0);
	public static final int DAMAGE_MIN = 20;
	public static final int DAMAGE_MAX = 60;
	public static final int MAX_HEALTH = 100;
	
	private Camera camera;
	private Random rand;
	private int health;

	private boolean mouseLocked = false;
	private Vector2f centerPosition = new Vector2f(Window.getWidth()/2, Window.getHeight()/2);
	private Vector3f movementVector;
	
	public Player(Vector3f position)
	{
		camera = new Camera(position, new Vector3f(0,0,1), new Vector3f(0,1,0));
		rand = new Random();
		health = MAX_HEALTH;
	}

	public void damage(int amt)
	{
		health -= amt;

		if(health > MAX_HEALTH)
			health = MAX_HEALTH;

		System.out.println(health);

		if(health <= 0)
		{
			Game.setIsRunning(false);
			System.out.println("You just died! GAME OVER!");
		}
	}

	public int getDamage()
	{
		return rand.nextInt(DAMAGE_MAX - DAMAGE_MIN) + DAMAGE_MIN;
	}
	
	public void input()
	{
		if(Input.getKeyDown(Input.KEY_E))
		{
			Game.getLevel().openDoors(camera.getPos());
		}

		if(Input.getKey(Input.KEY_ESCAPE))
		{
			Input.setCursor(true);
			mouseLocked = false;
		}
		if(Input.getMouseDown(0))
		{
			if(!mouseLocked)
			{
				Input.setMousePosition(centerPosition);
				Input.setCursor(false);
				mouseLocked = true;
			}
			else
			{
				Vector2f lineStart = new Vector2f(camera.getPos().getX(), camera.getPos().getZ());
				Vector2f castDirection = new Vector2f(camera.getForward().getX(), camera.getForward().getZ()).normalized();
				Vector2f lineEnd = lineStart.add(castDirection.mul(SHOOT_DISTANCE));

				Game.getLevel().checkIntersections(lineStart, lineEnd, true);
			}
		}
		
		movementVector = zeroVector;
		
		if(Input.getKey(Input.KEY_W))
			movementVector = movementVector.add(camera.getForward());//camera.move(camera.getForward(), movAmt);
		if(Input.getKey(Input.KEY_S))
			movementVector = movementVector.sub(camera.getForward());//camera.move(camera.getForward(), -movAmt);
		if(Input.getKey(Input.KEY_A))
			movementVector = movementVector.add(camera.getLeft());//camera.move(camera.getLeft(), movAmt);
		if(Input.getKey(Input.KEY_D))
			movementVector = movementVector.add(camera.getRight());//camera.move(camera.getRight(), movAmt);
		
		if(mouseLocked)
		{
			Vector2f deltaPos = Input.getMousePosition().sub(centerPosition);
			
			boolean rotY = deltaPos.getX() != 0;
			boolean rotX = deltaPos.getY() != 0;
			
			if(rotY)
				camera.rotateY(deltaPos.getX() * MOUSE_SENSITIVITY);
			if(rotX)
				camera.rotateX(-deltaPos.getY() * MOUSE_SENSITIVITY);
				
			if(rotY || rotX)
				Input.setMousePosition(centerPosition);
		}
	}
	
	public void update()
	{
		float movAmt = (float)(MOVE_SPEED * Time.getDelta());
		
		movementVector.setY(0);
		
		if(movementVector.length() > 0)
			movementVector = movementVector.normalized();
		
		Vector3f oldPos = camera.getPos();
		Vector3f newPos = oldPos.add(movementVector.mul(movAmt));
		
		Vector3f collisionVector = Game.getLevel().checkCollision(oldPos, newPos, PLAYER_SIZE, PLAYER_SIZE);
		movementVector = movementVector.mul(collisionVector);
		
		//TODO: Make sure movementVector is greater than 0
		camera.move(movementVector, movAmt);
	}
	
	public void render()
	{
		
	}
	
	public Camera getCamera()
	{
		return camera;
	}
}
