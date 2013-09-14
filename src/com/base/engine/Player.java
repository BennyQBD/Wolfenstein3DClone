package com.base.engine;

public class Player
{
	private static final float MOUSE_SENSITIVITY = 0.5f;
	private static final float MOVE_SPEED = 8f;
	private static final float PLAYER_SIZE = 0.2f;
	private static final Vector3f zeroVector = new Vector3f(0,0,0);
	
	private Camera camera;

	private boolean mouseLocked = false;
	private Vector2f centerPosition = new Vector2f(Window.getWidth()/2, Window.getHeight()/2);
	private Vector3f movementVector;
	
	public Player(Vector3f position)
	{
		camera = new Camera(position, new Vector3f(0,0,1), new Vector3f(0,1,0));
	}
	
	public void input()
	{
		if(Input.getKey(Input.KEY_ESCAPE))
		{
			Input.setCursor(true);
			mouseLocked = false;
		}
		if(Input.getMouseDown(0))
		{
			Input.setMousePosition(centerPosition);
			Input.setCursor(false);
			mouseLocked = true;
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
