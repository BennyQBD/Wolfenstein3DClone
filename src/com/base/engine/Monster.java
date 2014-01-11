package com.base.engine;

import java.util.ArrayList;
import java.util.Random;

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

	public static final float MOVE_SPEED = 1.0f;
	public static final float MOVEMENT_STOP_DISTANCE = 1.5f;
	public static final float MONSTER_WIDTH = 0.2f;
	public static final float MONSTER_LENGTH = 0.2f;

	public static final float SHOOT_DISTANCE = 1000.0f;
	public static final float SHOT_ANGLE = 10.0f;
	public static final float ATTACK_CHANCE = 0.5f;
	public static final int MAX_HEALTH = 100;
	public static final int DAMAGE_MIN = 5;
	public static final int DAMAGE_MAX = 30;

	private static Mesh mesh;
	private static ArrayList<Texture> animations;

	private Material material;
	private Transform transform;
	private Random rand;
	private int state;
	private int health;
    private boolean canLook;
	private boolean canAttack;
	private double deathTime;

	public Monster(Transform transform)
	{
		if(animations == null)
		{
			animations = new ArrayList<Texture>();

			animations.add(new Texture("SSWVA1.png"));
			animations.add(new Texture("SSWVB1.png"));
			animations.add(new Texture("SSWVC1.png"));
			animations.add(new Texture("SSWVD1.png"));

			animations.add(new Texture("SSWVE0.png"));
			animations.add(new Texture("SSWVF0.png"));
			animations.add(new Texture("SSWVG0.png"));

			animations.add(new Texture("SSWVH0.png"));

			animations.add(new Texture("SSWVI0.png"));
			animations.add(new Texture("SSWVJ0.png"));
			animations.add(new Texture("SSWVK0.png"));
			animations.add(new Texture("SSWVL0.png"));

			animations.add(new Texture("SSWVM0.png"));
		}

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

		this.transform = transform;
		this.state = STATE_IDLE;
		this.canAttack = false;
		this.canLook = false;
		this.health = MAX_HEALTH;
		this.rand = new Random();
		this.material = new Material(animations.get(0));
		this.deathTime = 0;
	}

	public Transform getTransform()
	{
		return transform;
	}

	public Vector2f getSize()
	{
		return new Vector2f(MONSTER_WIDTH, MONSTER_LENGTH);
	}

	public void damage(int amt)
	{
		if(state == STATE_IDLE)
			state = STATE_CHASE;

		health -= amt;

		if(health <= 0)
			state = STATE_DYING;
	}

	private void idleUpdate(Vector3f orientation, float distance)
	{
        double time = ((double)Time.getTime())/((double)Time.SECOND);
        double timeDecimals = time - (double)((int)time);

        if(timeDecimals < 0.5)
        {
            canLook = true;
			material.setTexture(animations.get(0));
        }
        else
        {
			material.setTexture(animations.get(1));

			if(canLook)
			{
				Vector2f lineStart = new Vector2f(transform.getTranslation().getX(), transform.getTranslation().getZ());
				Vector2f castDirection = new Vector2f(orientation.getX(), orientation.getZ());
				Vector2f lineEnd = lineStart.add(castDirection.mul(SHOOT_DISTANCE));

				Vector2f collisionVector = Game.getLevel().checkIntersections(lineStart, lineEnd, false);

				Vector2f playerIntersectVector = new Vector2f(Transform.getCamera().getPos().getX(), Transform.getCamera().getPos().getZ());

				if(collisionVector == null ||
					playerIntersectVector.sub(lineStart).length() < collisionVector.sub(lineStart).length())
				{
					state = STATE_CHASE;
				}

				canLook = false;
			}
		}
    }

	private void chaseUpdate(Vector3f orientation, float distance)
	{
		double time = ((double)Time.getTime())/((double)Time.SECOND);
		double timeDecimals = time - (double)((int)time);

		if(timeDecimals < 0.25)
			material.setTexture(animations.get(0));
		else if(timeDecimals < 0.5)
			material.setTexture(animations.get(1));
		else if(timeDecimals < 0.75)
			material.setTexture(animations.get(2));
		else
			material.setTexture(animations.get(3));

		if(rand.nextDouble() < ATTACK_CHANCE * Time.getDelta())
			state = STATE_ATTACK;

		if(distance > MOVEMENT_STOP_DISTANCE)
		{
			float moveAmount = MOVE_SPEED * (float) Time.getDelta();

			Vector3f oldPos = transform.getTranslation();
			Vector3f newPos = transform.getTranslation().add(orientation.mul(moveAmount));

			Vector3f collisionVector = Game.getLevel().checkCollision(oldPos, newPos, MONSTER_WIDTH, MONSTER_LENGTH);

			Vector3f movementVector = collisionVector.mul(orientation);

			if(movementVector.length() > 0)
				transform.setTranslation(transform.getTranslation().add(movementVector.mul(moveAmount)));

			if(movementVector.sub(orientation).length() != 0)
				Game.getLevel().openDoors(transform.getTranslation(), false);
		}
		else
			state = STATE_ATTACK;
	}

	private void attackUpdate(Vector3f orientation, float distance)
	{
		double time = ((double)Time.getTime())/((double)Time.SECOND);
		double timeDecimals = time - (double)((int)time);

		if(timeDecimals < 0.25)
			material.setTexture(animations.get(4));
		else if(timeDecimals < 0.5)
			material.setTexture(animations.get(5));
		else if(timeDecimals < 0.75)
		{
			material.setTexture(animations.get(6));
			if(canAttack)
			{
				Vector2f lineStart = new Vector2f(transform.getTranslation().getX(), transform.getTranslation().getZ());
				Vector2f castDirection = new Vector2f(orientation.getX(), orientation.getZ()).rotate((rand.nextFloat() - 0.5f) * SHOT_ANGLE);
				Vector2f lineEnd = lineStart.add(castDirection.mul(SHOOT_DISTANCE));

				Vector2f collisionVector = Game.getLevel().checkIntersections(lineStart, lineEnd, false);

				Vector2f playerIntersectVector = Game.getLevel().lineIntersectRect(lineStart, lineEnd,
													new Vector2f(Transform.getCamera().getPos().getX(), Transform.getCamera().getPos().getZ()),
													new Vector2f(Player.PLAYER_SIZE, Player.PLAYER_SIZE));

				if(playerIntersectVector != null && (collisionVector == null ||
					playerIntersectVector.sub(lineStart).length() < collisionVector.sub(lineStart).length()))
				{
					Game.getLevel().damagePlayer(rand.nextInt(DAMAGE_MAX - DAMAGE_MIN) + DAMAGE_MIN);
				}

				canAttack = false;
				state = STATE_CHASE;
			}
		}
		else
		{
			material.setTexture(animations.get(5));
			canAttack = true;
		}

	}

	private void dyingUpdate(Vector3f orientation, float distance)
	{
		double time = ((double)Time.getTime())/((double)Time.SECOND);
		double timeDecimals = time - (double)((int)time);

		if(deathTime == 0)
			deathTime = time;

		final float time1 = 0.1f;
		final float time2 = 0.3f;
		final float time3 = 0.45f;
		final float time4 = 0.6f;

		if(time < deathTime + time1)
		{
			material.setTexture(animations.get(8));
			transform.setScale(1,0.96428571428571428571428571428571f,1);
		}
		else if(time < deathTime + time2)
		{
			material.setTexture(animations.get(9));
			transform.setScale(1.7f,0.9f,1);
		}
		else if(time < deathTime + time3)
		{
			material.setTexture(animations.get(10));
			transform.setScale(1.7f,0.9f,1);
		}
		else if(time < deathTime + time4)
		{
			material.setTexture(animations.get(11));
			transform.setScale(1.7f,0.5f,1);
		}
		else
		{
			state = STATE_DEAD;
		}
	}

	private void deadUpdate(Vector3f orientation, float distance)
	{
		material.setTexture(animations.get(12));
		transform.setScale(1.7586206896551724137931034482759f,0.28571428571428571428571428571429f,1);
	}

	private void alignWithGround()
	{
		transform.getTranslation().setY(OFFSET_FROM_GROUND);
	}

	private void faceCamera(Vector3f directionToCamera)
	{
		float angleToFaceTheCamera = (float)Math.toDegrees(Math.atan(directionToCamera.getZ() / directionToCamera.getX()));

		if(directionToCamera.getX() < 0)
			angleToFaceTheCamera += 180;

		transform.getRotation().setY(angleToFaceTheCamera + 90);
	}

	public void update()
	{
		Vector3f directionToCamera = Transform.getCamera().getPos().sub(transform.getTranslation());

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
