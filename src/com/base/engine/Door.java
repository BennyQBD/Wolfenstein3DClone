package com.base.engine;

public class Door
{
	//NOTE: You may need to add top/bottom face depending on how you set these variables
	public static final float LENGTH = 1;
	public static final float HEIGHT = 1;
	public static final float WIDTH = 0.125f;
	public static final float START = 0;
	
	private static Mesh mesh;
	private Material material;
	private Transform transform;
	
	public Door(Transform transform, Material material)
	{
		this.transform = transform;
		this.material = material;
		if(mesh == null)
		{
			
			
			Vertex[] vertices = new Vertex[]{new Vertex(new Vector3f(START,START,START), new Vector2f(0.5f,1)),
											 new Vertex(new Vector3f(START,HEIGHT,START), new Vector2f(0.5f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,HEIGHT,START), new Vector2f(0.75f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,START,START), new Vector2f(0.75f,1)),
											 
											 new Vertex(new Vector3f(START,START,START), new Vector2f(0.73f,1)),
											 new Vertex(new Vector3f(START,HEIGHT,START), new Vector2f(0.73f,0.75f)),
											 new Vertex(new Vector3f(START,HEIGHT,WIDTH), new Vector2f(0.75f,0.75f)),
											 new Vertex(new Vector3f(START,START,WIDTH), new Vector2f(0.75f,1)),
											 
											 new Vertex(new Vector3f(START,START,WIDTH), new Vector2f(0.5f,1)),
											 new Vertex(new Vector3f(START,HEIGHT,WIDTH), new Vector2f(0.5f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,HEIGHT,WIDTH), new Vector2f(0.75f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,START,WIDTH), new Vector2f(0.75f,1)),
											 
											 new Vertex(new Vector3f(LENGTH,START,START), new Vector2f(0.73f,1)),
											 new Vertex(new Vector3f(LENGTH,HEIGHT,START), new Vector2f(0.73f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,HEIGHT,WIDTH), new Vector2f(0.75f,0.75f)),
											 new Vertex(new Vector3f(LENGTH,START,WIDTH), new Vector2f(0.75f,1))};
			
			int[] indices = new int[]{0,1,2,
									  0,2,3,
									  
									  6,5,4,
									  7,6,4,
									  
									  10,9,8,
									  11,10,8,
									  
									  12,13,14,
									  12,14,15};
			
			mesh = new Mesh(vertices, indices);
		}
	}
	
	public void update()
	{
		
	}
	
	public void render()
	{
		Shader shader = Game.getLevel().getShader();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
	}
	
	public Transform getTransform()
	{
		return transform;
	}
}
