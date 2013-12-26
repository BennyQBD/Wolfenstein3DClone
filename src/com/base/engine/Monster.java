package com.base.engine;

public class Monster
{
	public static final float SCALE = 0.7f;
	public static final float SIZEY = SCALE;
	public static final float SIZEX = (float)((double)SIZEY / (1.9310344827586206896551724137931 * 2.0));
	public static final float START = 0;

	public static final float OFFSET_X = 0.0f; //0.05f
	public static final float OFFSET_Y = 0.0f; //0.01f
	public static final float OFFSET_FROM_GROUND = 0.0f; //-0.075f

	public static final float TEX_MIN_X = -OFFSET_X;
	public static final float TEX_MAX_X = -1 - OFFSET_X;
	public static final float TEX_MIN_Y = -OFFSET_Y;
	public static final float TEX_MAX_Y = 1 - OFFSET_Y;

	public static final int STATE_IDLE = 0;
	public static final int STATE_CHASE = 1;
	public static final int STATE_ATTACK = 2;
	public static final int STATE_DYING = 3;
	public static final int STATE_DEAD = 4;

	public static final float MOVE_SPEED = 1.0f; //TODO: Make this value appropriate
	public static final float MOVEMENT_STOP_DISTANCE = 1.5f;
	public static final float MONSTER_WIDTH = 0.2f;
	public static final float MONSTER_LENGTH = 0.2f;

	private static Mesh mesh;
	private Material material;
	private Transform transform;
	private int state;

	public Monster(Transform transform)
	{
		this.transform = transform;
		this.state = STATE_CHASE;
		material = new Material(new Texture("SSWVA1.png"));

		if(mesh == null)
		{
			Vertex[] vertices = new Vertex[]{new Vertex(new Vector3f(-SIZEX,START,START), new Vector2f(TEX_MAX_X,TEX_MAX_Y)),
											 new Vertex(new Vector3f(-SIZEX,SIZEY,START), new Vector2f(TEX_MAX_X,TEX_MIN_Y)),
											 new Vertex(new Vector3f(SIZEX,SIZEY,START), new Vector2f(TEX_MIN_X,TEX_MIN_Y)),
											 new Vertex(new Vector3f(SIZEX,START,START), new Vector2f(TEX_MIN_X,TEX_MAX_Y))};

			int[] indices = new int[]{0,1,2,
									  0,2,3};

			mesh = new Mesh(vertices, indices);
		}
	}

	private void idleUpdate(Vector3f orientation, float distance)
	{

	}

	private void chaseUpdate(Vector3f orientation, float distance)
	{
		if(distance > MOVEMENT_STOP_DISTANCE)
		{
			float moveAmount = -MOVE_SPEED * (float) Time.getDelta();

			Vector3f oldPos = transform.getTranslation();
			Vector3f newPos = transform.getTranslation().add(orientation.mul(moveAmount));

			Vector3f collisionVector = Game.getLevel().checkCollision(oldPos, newPos, MONSTER_WIDTH, MONSTER_LENGTH);

			Vector3f movementVector = collisionVector.mul(orientation);

			if(movementVector.length() > 0)
				transform.setTranslation(transform.getTranslation().add(movementVector.mul(moveAmount)));

			if(movementVector.sub(orientation).length() != 0)
				Game.getLevel().openDoors(transform.getTranslation());
		}
	}

	private void attackUpdate(Vector3f orientation, float distance)
	{

	}

	private void dyingUpdate(Vector3f orientation, float distance)
	{

	}

	private void deadUpdate(Vector3f orientation, float distance)
	{

	}

	private void alignWithGround()
	{
		transform.getTranslation().setY(OFFSET_FROM_GROUND);
	}

	private void faceCamera(Vector3f directionToCamera)
	{
		float angleToFaceTheCamera = (float)Math.toDegrees(Math.atan(directionToCamera.getZ() / directionToCamera.getX()));

		if(directionToCamera.getX() > 0)
			angleToFaceTheCamera += 180;

		transform.getRotation().setY(angleToFaceTheCamera + 90);
	}

	public void update()
	{
		Vector3f directionToCamera = transform.getTranslation().sub(Transform.getCamera().getPos());

		float distance = directionToCamera.length();
		Vector3f orientation = directionToCamera.div(distance);

		alignWithGround();
		faceCamera(orientation);

		switch(state)
		{
			case STATE_IDLE: idleUpdate(orientation, distance); break;
			case STATE_CHASE: chaseUpdate(orientation, distance); break;
			case STATE_ATTACK: attackUpdate(orientation, distance); break;
			case STATE_DYING: dyingUpdate(orientation, distance); break;
			case STATE_DEAD: deadUpdate(orientation, distance); break;
		}
	}

	public void render()
	{
		Shader shader = Game.getLevel().getShader();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
	}
}
