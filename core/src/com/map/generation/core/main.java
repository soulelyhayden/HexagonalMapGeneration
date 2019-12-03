package com.map.generation.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.map.generation.tiles.Tile;
import com.map.generation.tiles.TileSet;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.GL20.*;

public class main extends ApplicationAdapter {

	private Environment environment;

	private PerspectiveCamera cam;
	private Vector3 point = new Vector3(0,0,0);
	private Vector3 camDistance = new Vector3(0,50,0);

	private PointLight light;
	private float lightIntesity = 5000f;
	private Color lightCol = new Color(0.8f,0.8f,0.8f, 1f);
	private Vector3 lightDir = new Vector3(-1f,-1f,-0.2f);
	private DirectionalShadowLight shadowLight;
	private ModelBatch shadowBatch;

	private ModelBatch modelBatch;

	private final static int scaleVal = 8;
	private final static int raiseHeight = 1;
	private TileSet tileSet = new TileSet(50,2, scaleVal, raiseHeight);
	private ArrayList<Tile> tiles = new ArrayList<>();

	private Input input = new Input();



	@Override
	public void create () {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		//light = new DirectionalLight().set(lightCol, lightDir);
		light = new PointLight().set(lightCol, point, lightIntesity);

		environment.add(light);
		/*environment.add((shadowLight = new DirectionalShadowLight(10000, 10000, 1000f, 1000f, 1f, 1000f))
				.set(0.3f, 0.3f, 0.3f, -1f, -0.8f,
						-0.2f));
		environment.shadowMap = shadowLight;

		modelBatch = new ModelBatch();
		shadowBatch = new ModelBatch(new DepthShaderProvider());*/

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(point.add(camDistance));
		cam.lookAt(point);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		tiles = tileSet.getTileSet();

		//model = new Tile(0,0,0,1, 1,Color.GREEN).model;


		//instance = new ModelInstance(model);

		//instance.transform.scl(1,2f,1);



		//camController = new CameraInputController(cam);
		//Gdx.input.setInputProcessor(camController);

		Gdx.input.setInputProcessor(input);
	}

	@Override
	public void render () {

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glCullFace(GL_NONE);
		Gdx.gl.glFrontFace(GL_CW);




		Vector3 dir = new Vector3(0,0,0);
		dir.add(cam.position);
		dir.add(camDistance);
		dir.mulAdd(camDistance,2);
		//double new_y = (current_x-center_x) * Math.sin(angle) + (current_y-center_y) * Math.cos(angle) + center_y;


		light.position.lerp(dir, 0.08f);

		/*shadowLight.begin(Vector3.Zero, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());

		for (ModelInstance in : tileInstances) {
			shadowBatch.render(in);
		}

		shadowBatch.end();
		shadowLight.end();*/

		modelBatch.begin(cam);


		for (Tile tile : tiles) {
			tile.update();

			ModelInstance tileModel = new ModelInstance(tile.getModel());
			tileModel.transform.setToScaling(1,(float)tile.getScale(),1);
			tileModel.materials.get(0).set(ColorAttribute.createDiffuse(tile.getCol()));
			modelBatch.render(tileModel, environment);
		}

		modelBatch.end();

		update();
		camControl();
	}

	public static int getScale() {
		return scaleVal;
	}

	private void update() {

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
}
