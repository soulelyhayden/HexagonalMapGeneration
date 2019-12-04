package com.map.generation.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.map.generation.objects.tiles.*;
import com.map.generation.objects.*;
import com.map.generation.objects.world.*;

import java.util.ArrayList;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.*;

public class main extends ApplicationAdapter {

	private Environment environment;

	private PerspectiveCamera cam;

	private Color lightCol = new Color(0.6f,0.6f,0.6f, 1f);
	private Vector3 lightDir = new Vector3(0.8f,-0.95f,0.8f);
	private DirectionalShadowLight shadowLight;
	private ModelBatch shadowBatch;

	private ModelBatch modelBatch;
	private Ground ground;
	private Sky sky;

	private final static int scaleVal = 10;
	private final static int raiseHeight = 1;
	private final static int MAP_SIZE = 50;
	private final static double TILE_SIZE = 0.5;
	private static Map biome = new Colours().getBiome("standard");
	private static TileSet tileSet = new TileSet(MAP_SIZE,TILE_SIZE, scaleVal, raiseHeight, biome);
	private ArrayList<Tile> tiles = new ArrayList<>();
	private ArrayList<ModelInstance> tileInstance = new ArrayList<>();

	private Input input = new Input();



	@Override
	public void create () {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));

		shadowLight = new DirectionalShadowLight(10000, 10000, (float)(MAP_SIZE * 3 * TILE_SIZE), (float)(MAP_SIZE * 3 * TILE_SIZE), 0.1f, 1200f);
		shadowLight.getCamera().update();

		shadowLight.set(lightCol, lightDir);
		shadowLight.direction.rotate(Vector3.Y, 180);
		environment.add(shadowLight);
		environment.shadowMap = shadowLight;

		modelBatch = new ModelBatch();
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set((int)(MAP_SIZE * TILE_SIZE), (int)(scaleVal * TILE_SIZE * 5), (int)(MAP_SIZE * TILE_SIZE) * 2);
		cam.direction.y -= Math.sin(90);

		cam.near = 1f;
		cam.far = 1000f;
		cam.update();

		tiles = tileSet.getTileSet();

		ground = new Ground(MAP_SIZE, TILE_SIZE, biome);
		sky = new Sky(MAP_SIZE, TILE_SIZE, biome);

		Gdx.input.setInputProcessor(input);
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glCullFace(GL_NONE);
		Gdx.gl.glFrontFace(GL_CW);


		camControl();
		update();
		lightRender();
		instanceRender();
	}

	private void instanceRender() {
		modelBatch.begin(cam);

		ModelInstance groundModel = new ModelInstance(ground.getGround());
		groundModel.materials.get(0).set(ColorAttribute.createDiffuse(ground.getCol()));
		modelBatch.render(groundModel, environment);

		ModelInstance skyModel = new ModelInstance(sky.getSky(), (int)(MAP_SIZE * TILE_SIZE), 0, (int)(MAP_SIZE * TILE_SIZE));
		skyModel.materials.get(0).set(ColorAttribute.createDiffuse(sky.getCol()));
		modelBatch.render(skyModel,environment);

		for (ModelInstance model : tileInstance) {
			modelBatch.render(model, environment);
		}
		modelBatch.end();
	}

	private void lightRender() {
		shadowLight.begin(Vector3.Zero, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());


		for (ModelInstance model : tileInstance) {
			shadowBatch.render(model);
		}
		shadowBatch.end();
		shadowLight.end();
	}

	private void update() {
		tileInstance.clear();
		Tile[][] tempTiles = new Tile[MAP_SIZE + 1][MAP_SIZE + 1];

		for (Tile tile : tiles) {
			tile.update();

			ModelInstance tileModel = new ModelInstance(tile.getModel());
			tileModel.transform.setToScaling(1,(float)tile.getScale(),1);
			tileModel.materials.get(0).set(ColorAttribute.createDiffuse(tile.getCol()));

			tileInstance.add(tileModel);
			tempTiles[tile.getX()][tile.getZ()] = tile;
		}

		tileSet.setTilesAdjacent(tempTiles);
	}

	private void camControl() {
		if (input.forward) {
			Vector3 v = cam.direction.cpy();
			cam.translate(v);
			cam.update();
		}
		if (input.back) {
			Vector3 v = cam.direction.cpy();
			v.y*=-1;
			v.x *= -1;
			v.z *= -1;
			cam.translate(v);
			cam.update();
		}
		if (input.left) {
			Vector3 v = cam.direction.cpy();
			v.y=0f;
			v.rotate(Vector3.Y, 90);
			cam.translate(v);
			cam.update();
		}
		if (input.right) {
			Vector3 v = cam.direction.cpy();
			v.y=0f;
			v.rotate(Vector3.Y, -90);
			cam.translate(v);
			cam.update();
		}
		if (input.up) {
			Vector3 v = new Vector3(0,1,0);
			cam.translate(v);
			cam.update();
		}
		if (input.down) {
			Vector3 v = new Vector3(0,-1,0);
			cam.translate(v);
			cam.update();
		}

		if (input.turn) {
			float xTurn = Gdx.input.getDeltaX();
			 float yTurn = Gdx.input.getDeltaY();
			cam.rotate(Vector3.Y, -xTurn / 2);
			cam.direction.y -= Math.sin(yTurn / 180f) * 3;
			cam.update();
		}
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		for (Tile tile : tiles) {
			tile.getModel().dispose();
		}
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update();
	}

	@Override
	public void pause () {
	}

	public static int getScale() {
		return scaleVal;
	}

	public static TileSet getTileSet() {
		return tileSet;
	}
}
