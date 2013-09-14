package com.base.engine;

public class Game 
{
	private static Level level;
	private Player player;
	
	public Game()
	{
		level = new Level("level1.png", "WolfCollection.png");
		player = new Player(new Vector3f(7,0.4375f,7));
		
		Transform.setProjection(70, Window.getWidth(), Window.getHeight(), 0.01f, 1000f);
		Transform.setCamera(player.getCamera());
	}
	
	public void input()
	{
		level.input();
		player.input();
	}
	
	public void update()
	{
		level.update();
		player.update();
	}
	
	public void render()
	{
		level.render();
		player.render();
	}
	
	public static Level getLevel()
	{
		return level;
	}
}
