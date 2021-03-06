package com.siondream.libgdxjam.ecs.components.agents;

import com.badlogic.ashley.core.Component;
import com.siondream.libgdxjam.utils.Direction;

public class GruntComponent implements Component 
{
	public float sightDistance;
	
	public float walkSpeed;
	public Direction direction = Direction.CLOCKWISE;
	
	public float leftWalkableArea;
	public float center;
	public float rightWalkableArea;
	
	public float leftAreaWaitSeconds;
	public float rightAreaWaitSeconds;
	
	public boolean isAwake;
	public boolean isAlerted;
}
