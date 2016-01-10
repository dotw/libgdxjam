package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.AnimationSelectionComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.animation.AnimationSelectionData;

public class PlayerPlugin implements OverlapLoaderPlugin {
	private CameraSystem cameraSystem;
	
	public PlayerPlugin(CameraSystem cameraSystem) {
		this.cameraSystem = cameraSystem;
	}
	
	@Override
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> value) {
		PhysicsComponent physics = new PhysicsComponent();
		PlayerComponent player = new PlayerComponent();
		SpineComponent spine = new SpineComponent();
		SizeComponent size = new SizeComponent();
		AnimationSelectionComponent selection = new AnimationSelectionComponent();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		SkeletonData skeletonData = assetManager.get("./spine/Player.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		spine.state.setAnimation(0, "Idle", true);
		spine.state.getData().setDefaultMix(0.1f);
		
		selection.data = assetManager.get("./anims/player.json", AnimationSelectionData.class);
		
		size.width = 0.5f;
		size.height = 1.4f;
		
		entity.add(physics);
		entity.add(player);
		entity.add(spine);
		entity.add(size);
		entity.add(selection);
		
		cameraSystem.setTarget(entity);
	}
}
