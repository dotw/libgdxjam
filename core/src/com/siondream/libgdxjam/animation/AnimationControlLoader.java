package com.siondream.libgdxjam.animation;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.JsonIterator;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonDataLoader.SkeletonDataLoaderParameter;

public class AnimationControlLoader extends AsynchronousAssetLoader<AnimationControl, AnimationControlLoader.Parameter>{
	private Logger logger = new Logger(
			AnimationControlLoader.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	FileHandleResolver resolver;
	private JsonReader reader = new JsonReader();
	private Tags tags;
	private AnimationControl data;
	private SkeletonData skeleton;
	
	public AnimationControlLoader(FileHandleResolver resolver, Tags tags) {
		super(resolver);
		
		logger.info("initialize");
		this.resolver = resolver;
		this.tags = tags;
	}

	static public class Parameter extends AssetLoaderParameters<AnimationControl> {
		
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName,
			FileHandle file, Parameter parameter) {
		data = new AnimationControl();
		loadData(manager, fileName, file, parameter);
	}

	@Override
	public AnimationControl loadSync(AssetManager manager,
			String fileName, FileHandle file, Parameter parameter) {
		return data;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName,
			FileHandle file, Parameter parameter) {
		
		JsonValue root = reader.parse(file);
		String spineFile = root.getString("spine");
		
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		dependencies.add(new AssetDescriptor(
			spineFile,
			SkeletonData.class,
			getSkeletonParameters(spineFile)
		));
		
		return dependencies;
	}
	
	private SkeletonDataLoaderParameter getSkeletonParameters(String spineFile) {
		FileHandle spine = resolver.resolve(spineFile);
		
		SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter();
		parameter.atlasName = spine.parent().path() + "/" + spine.nameWithoutExtension() + ".atlas";
		parameter.scale = Env.UI_TO_WORLD;
		return parameter;
	}
	
	private void loadData(AssetManager manager, String fileName,
			FileHandle file, Parameter parameter) {

		JsonValue root = reader.parse(file);
		
		skeleton = manager.get(root.getString("spine"), SkeletonData.class);
		
		loadTagGroups(root.get("tags"));
		loadLayers(root.get("layers"));
		loadTransitions(root.get("transitions"));
	}
	
	private void loadTagGroups(JsonValue value) {
		JsonIterator iterator = value.iterator();
		
		while(iterator.hasNext()) {
			loadTagGroup(iterator.next());
		}
	}
	
	private void loadTagGroup(JsonValue value) {
		TagGroup group = new TagGroup(
			value.name,
			loadTags(value)
		);
		
		data.mutableGroups.add(group);
		
		for (int tag : group.tags) {
			data.groupsByTag.put(tag, group);
		}
	}
	
	private Array<Integer> loadTags(JsonValue value) {
		Array<Integer> tags = new Array<Integer>();
		
		for (String tagName : value.asStringArray()) {
			tags.add(this.tags.get(tagName));
		}
		
		return tags;
	}
	
	private void loadLayers(JsonValue value) {
		JsonIterator iterator = value.iterator();
		int track = 0;
		
		while(iterator.hasNext()) {
			loadLayer(iterator.next(), track++);
		}
	}
	
	private void loadLayer(JsonValue value, int track) {
		String name = value.name;
		Array<Entry> entries = new Array<Entry>();
		
		JsonIterator iterator = value.iterator();
		
		while(iterator.hasNext()) {
			entries.add(loadEntry(iterator.next()));
		}
		
		data.mutableLayers.add(new Layer(name, entries, track));
	}
	
	private Entry loadEntry(JsonValue value) {
		String name = value.getString("name");
		Animation animation = skeleton.findAnimation(name);
		
		if (animation == null) {
			logger.error("animation not found: " + name);
		}
		
		return new Entry(
			animation,
			loadTags(value.get("tags")),
			value.getBoolean("loop")
		);
	}
	
	private void loadTransitions(JsonValue value) {
		data.defaultDuration = value.getFloat("defaultDuration");
		
		if (!value.has("list")) { return; }
		
		JsonIterator iterator = value.get("list").iterator();
		
		while (iterator.hasNext()) {
			loadTransition(iterator.next());
		}
	}
	
	private void loadTransition(JsonValue value) {
		String fromName = value.getString("from");
		String toName = value.getString("to");
		Animation from = skeleton.findAnimation(fromName);
		Animation to = skeleton.findAnimation(toName);
		
		if (from == null) {
			logger.error("from animation not found: " + fromName);
			return;
		}
		
		if (to == null) {
			logger.error("to animation not found: " + to);
			return;
		}
		
		float duration = value.getFloat("duration");
		
		if (!data.transitions.containsKey(from)) {
			data.transitions.put(from, new ObjectMap<Animation, Float>());
		}
		
		data.transitions.get(from).put(to, duration);
	}
}
