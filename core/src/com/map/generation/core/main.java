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

import static com.badlogic.gdx.graphics.GL20.*;

public class main extends ApplicationAdapter {

	private Environment environment;

	private PerspectiveCamera cam;

	private Color lightCol = new Color(0.6f,0.6f,0.6f, 1f);
	private Vector3 lightDir = new Vector3(0.8f,-0.95f,0.8f);

	//Using depreciated shadowlight because I didn't have time to make custom shaders and LibGDX hasn't offered anything better for 3D shadows- seems to work okay... besides not great quality
	private DirectionalShadowLight shadowLight;

	//model stuff
	private ModelBatch shadowBatch;
	private ModelBatch modelBatch;
	private Ground ground;
	private Sky sky;

	//everything important to change most of the stuff
	private final static int scaleVal = 10; //how much to scale the noise map by
	private final static int raiseHeight = 2; // how much to raise water level by basically
	private final static int MAP_SIZE = 50; //map size, bigger maps can be slow
	private final static double TILE_SIZE = 0.5; //best range is 0.3 - 1. Should not exceed 1, not good for performance or lighting
	private static Biome biome = new Biome("standard"); //choose your biome, determines colours, standard colour deviation, water speed, and even erotion speed (if set for the biome)

	//tile stuff
	private static TileSet tileSet = new TileSet(MAP_SIZE,TILE_SIZE, scaleVal, raiseHeight, biome);
	private ArrayList<Tile> tiles = new ArrayList<>();
	private ArrayList<ModelInstance> tileInstance = new ArrayList<>();

	//my input class
	private Input input = new Input();



	@Override
	public void create () {
	    //setup environment, add base light
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));

		//setup shadow light and point it in the right direction, the shadow camera values adjust to the map size and tile size so everything always gets shadows
        //larger maps dont' have as nice shading because of this
		shadowLight = new DirectionalShadowLight(10000, 10000, (float)(MAP_SIZE * 3 * TILE_SIZE), (float)(MAP_SIZE * 3 * TILE_SIZE), 0.1f, 1200f);
		shadowLight.getCamera().update();

		shadowLight.set(lightCol, lightDir);
		shadowLight.direction.rotate(Vector3.Y, 180);
		environment.add(shadowLight);
		environment.shadowMap = shadowLight;

		//setup render stuff
		shadowBatch = new ModelBatch(new DepthShaderProvider());
		modelBatch = new ModelBatch();

		//setup camera and initial position to be looking at the map from a nice pov
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set((int)(MAP_SIZE * TILE_SIZE), (int)(scaleVal * TILE_SIZE * 5), (int)(MAP_SIZE * TILE_SIZE) * 2);
		cam.direction.y -= Math.sin(90);
		cam.near = 1f;
		cam.far = 1000f;
		cam.update();

		//filling the tile ArrayList
		tiles = tileSet.getTileSet();

		//init environment models
		ground = new Ground(MAP_SIZE, TILE_SIZE, biome);
		sky = new Sky(MAP_SIZE, TILE_SIZE, biome);

		//set my input processor
		Gdx.input.setInputProcessor(input);
	}

	@Override
	public void render () {
        super.render();

        //basic render stuff to make sure everything works properly
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glCullFace(GL_NONE);
		Gdx.gl.glFrontFace(GL_CW);

        //render methods
		camControl();
		update();
		lightRender();
		instanceRender();
	}

	//render the tiles, environment models, and environment here
	private void instanceRender() {
		modelBatch.begin(cam);

		//render ground
		ModelInstance groundModel = new ModelInstance(ground.getGround());
		groundModel.materials.get(0).set(ColorAttribute.createDiffuse(ground.getCol()));
		modelBatch.render(groundModel, environment);

		//render sky, and move to correct position
		ModelInstance skyModel = new ModelInstance(sky.getSky(), (int)(MAP_SIZE * TILE_SIZE), 0, (int)(MAP_SIZE * TILE_SIZE));
		skyModel.materials.get(0).set(ColorAttribute.createDiffuse(sky.getCol()));
		modelBatch.render(skyModel,environment);

		//iterate through and render tiles
		for (ModelInstance model : tileInstance) {
			modelBatch.render(model, environment);
		}
		modelBatch.end();
	}

	//renders the shadowlight
	private void lightRender() {
		shadowLight.begin(Vector3.Zero, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());

        //iterate through and render shadow for each tile
		for (ModelInstance model : tileInstance) {
			shadowBatch.render(model);
		}
		shadowBatch.end();
		shadowLight.end();
	}

	//all none render updates
	private void update() {
	    //clear old instance models
		tileInstance.clear();

		//filling a temp array with the updated tiles to update the tile array in tileSet so tiles always see their neighbours properly
		Tile[][] tempTiles = new Tile[MAP_SIZE + 1][MAP_SIZE + 1];

		//iterate through tiles, updating them and building instances for render
		for (Tile tile : tiles) {
			tile.update();

			ModelInstance tileModel = new ModelInstance(tile.getModel());
			tileModel.transform.setToScaling(1,(float)tile.getScale(),1);
			tileModel.materials.get(0).set(ColorAttribute.createDiffuse(tile.getCol()));

			tileInstance.add(tileModel);
			tempTiles[tile.getX()][tile.getZ()] = tile;
		}

        //updating tile array
		tileSet.setTilesAdjacent(tempTiles);
	}

	//camera control stuff reading from the input
	//camera movement speed is proportional to the tile size so you always move a decent speed
	private void camControl() {
		if (input.forward) {
			Vector3 v = new Vector3();
			v.mulAdd(cam.direction, (float)TILE_SIZE);
			cam.translate(v);
			cam.update();
		}
		if (input.back) {
			Vector3 v = cam.direction.cpy();
			v.y *= -(float)TILE_SIZE;
			v.x *= -(float)TILE_SIZE;
			v.z *= -(float)TILE_SIZE;
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
			Vector3 v = new Vector3(0,(float)TILE_SIZE,0);
			cam.translate(v);
			cam.update();
		}
		if (input.down) {
			Vector3 v = new Vector3(0,-(float)TILE_SIZE,0);
			cam.translate(v);
			cam.update();
		}

		//rotate camera with mouse drag
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

	    //garbage collection done by LibGDX
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

	    //make sure things don't get wonky
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update();
	}

	@Override
	public void pause () {
	}

	//getters
	public static int getScale() {
		return scaleVal;
	}

	public static TileSet getTileSet() {
		return tileSet;
	}
}
