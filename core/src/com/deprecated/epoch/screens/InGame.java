package com.epochgames.epoch.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.epochgames.epoch.Epoch;
import com.ender.games.epoch.GameManager;
import com.epochgames.epoch.dialogue.DialogueEngine;
import com.epochgames.epoch.entities.EntityFactory;
import com.epochgames.epoch.entities.Planet;
import com.epochgames.epoch.entities.Ship;
import com.epochgames.epoch.entities.systems.*;
import com.epochgames.epoch.screens.stages.TiledMapStage;
import com.epochgames.epoch.util.Assets;
import com.epochgames.epoch.util.EpochMath;
import com.epochgames.epoch.util.HexMapRender.HexMapRenderer;
import com.epochgames.epoch.util.HexagonGrid;
import com.epochgames.epoch.util.PathManager;
import com.epochgames.epoch.util.hexlib.HexSatelliteData;
import com.epochgames.epoch.util.hexlib.HexagonGridUtil;
import org.hexworks.mixite.core.api.CubeCoordinate;
import org.hexworks.mixite.core.api.Hexagon;
import org.hexworks.zircon.api.*;
import org.hexworks.zircon.api.component.CheckBox;
import org.hexworks.zircon.api.component.ColorTheme;
import org.hexworks.zircon.api.component.Header;
import org.hexworks.zircon.api.component.Panel;
import org.hexworks.zircon.api.resource.BuiltInCP437TilesetResource;
import org.hexworks.zircon.api.screen.Screen;
import org.hexworks.zircon.internal.application.LibgdxApplication;

public class InGame extends ScreenAdapter {

    public Stage tileActorStage;
    public GameManager gameManager;

    public RenderingSystem renderingSystem;
    public MovementSystem movementSystem;
    public TurnSystem turnSystem;
    public StorageSystem storageSystem;

    public Engine engine;

    public Epoch game;

    public DialogueEngine dialogueEngine;
    public LibgdxApplication zirconApplication;

    public HexagonGrid hexagonGrid;
    public HexMapRenderer mapRenderer;

    public float targetCameraZoom;
    public float camDeltaX;
    public float camDeltaY;

    public GameManager.Actions currentAction;

    public ShapeRenderer renderer;

    /*
	Move this player location to an actual class that handles the player. For now,
	this is used just to determine which Hexagons to render (view range)
	 */
    public CubeCoordinate playerPos;

    public InGame(Epoch game) {
        this.game = game;

        //Get the game manager and create a map to start on
        gameManager = GameManager.getInstance();
        //TODO this needs to be based off the state in the save file (Game manager)

        //Create our hexgrid, which will act as a way to place objects "on" our tilemap
        hexagonGrid = new HexagonGrid();
        mapRenderer = HexMapRenderer.instance;
        mapRenderer.setHexGrid(hexagonGrid, game, Assets.MANAGER.get(Assets.Textures.HEX_TILE));

        HexagonGridUtil.init(hexagonGrid);

        //Start our engine and add all the necessary systems
        engine = new Engine();

        renderingSystem = new RenderingSystem(game.batch);
        movementSystem = new MovementSystem(game, hexagonGrid);
        turnSystem = new TurnSystem(gameManager);
        storageSystem = new StorageSystem(game, hexagonGrid);

        engine.addSystem(renderingSystem);
        engine.addSystem(movementSystem);
        engine.addSystem(storageSystem);

        //Create a stage for the clickable things
        tileActorStage = new TiledMapStage(hexagonGrid.hexGrid);
        tileActorStage.setViewport(game.viewport);

        //Initialize the Entity Factory so we can create entities OTF
        EntityFactory.init(game);

        dialogueEngine = new DialogueEngine();
        BuiltInCP437TilesetResource tileset = BuiltInCP437TilesetResource.WANDERLUST_16X16;
        ColorTheme colorTheme = ColorThemes.afterTheHeist();
        zirconApplication = LibgdxApplications.buildApplication(
                AppConfigs.newConfig()
                        .withDefaultTileset(tileset)
                        .withSize(Sizes.create(
                                23,//game.screenWidth / tileset.getWidth(),
                                63))//game.screenHeight / tileset.getHeight()))
                        .build()
        );
        zirconApplication.start();
        setupZircon();

        //Temp
        playerPos = CubeCoordinate.fromCoordinates(5, 5);
        engine.addEntity(EntityFactory.createShip(playerPos, hexagonGrid, new Ship(GameManager.Ships.ALACRON, false), true));
        engine.addEntity(EntityFactory.createShip(CubeCoordinate.fromCoordinates(0, 0), hexagonGrid, new Ship(GameManager.Ships.CONTREX, false), false));
        CubeCoordinate ereasPos = CubeCoordinate.fromCoordinates(10, 10);
        engine.addEntity(EntityFactory.createPlanet(ereasPos, hexagonGrid, new Planet(GameManager.Planets.EREAS)));
        CubeCoordinate onathPos = CubeCoordinate.fromCoordinates(7, 2);
        engine.addEntity(EntityFactory.createPlanet(onathPos, hexagonGrid, new Planet(GameManager.Planets.ONATH)));
        currentAction = GameManager.Actions.INTERACT;
        renderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        //GL Stuff
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Update and set projection matrix
        game.batch.setProjectionMatrix(game.camera.combined);

        //Handle camera zoom
        game.camera.zoom = Interpolation.fade.apply(game.camera.zoom, targetCameraZoom, GameManager.ZOOM_SPEED);

        //Handle moving the camera
        camDeltaX = Interpolation.smoother.apply(camDeltaX, 0, GameManager.MOVE_SPEED);
        camDeltaY = Interpolation.smoother.apply(camDeltaY, 0, GameManager.MOVE_SPEED);
        game.camera.translate(camDeltaX, camDeltaY);

        //Draw the tile actors
        tileActorStage.draw();

        game.batch.begin();
        {
            //Render the hexgrid at the player's location
            switch (gameManager.getLocation()) {
                case OPEN_SPACE:
                    mapRenderer.renderHexGrid();
                    break;
                case PLANETARY_ORBIT:
                    break;
                case ON_PLANET:
                    break;
                default:
                    Gdx.app.error("Error", "No map to be loaded because the location doesn't exist!");
                    break;
            }

            if(Epoch.debug) {
                for (Hexagon<HexSatelliteData> hexagon : hexagonGrid.hexGrid.getHexagons()) {
                    //game.font.draw(game.batch, hexagon.getCubeCoordinate().toAxialKey(), (float) hexagon.getCenterX(), (float) hexagon.getCenterY());
                }
            }

            engine.update(delta);
        }
        game.batch.end();


        if(Epoch.debug) {
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setProjectionMatrix(game.camera.combined);
            if (PathManager.isInitialized()){
                Vector2[] points = PathManager.points;
                for (int i = 0; i < points.length; i++) {
                    renderer.circle(points[i].x, points[i].y, 5);
                }
            }
            renderer.end();
        }

        //Draw the GUI
        game.guiBatch.begin();
        {
            if(Epoch.debug) {
                //draw FPS counter
                game.font.setColor(Color.GREEN);
                game.font.draw(game.guiBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 50, game.viewport.getScreenHeight() - 50);
                game.font.setColor(Color.WHITE);
            }
        }
        game.guiBatch.end();

        zirconApplication.render();

        game.camera.update();
    }

    @Override
    public void show() {
        //Cool effect that zooms in on our grid when the game is initialized
        game.camera.zoom = 6.0f;
        targetCameraZoom = GameManager.START_ZOOM;
    }

    @Override
    public void dispose() {
        tileActorStage.dispose();
    }

    /**
     * Zooms the camera based off mousewheel input. The zoom is clamped to prevent
     * zooming too far/close
     * @param delta the amount to change the zoom
     */
    public void zoom(float delta) {
        targetCameraZoom = (float)EpochMath.clamp(targetCameraZoom + delta, GameManager.MIN_ZOOM, GameManager.MAX_ZOOM);
    }

    public void scroll(float deltaX, float deltaY) {
        camDeltaX += deltaX * GameManager.MOVE_FACTOR * game.camera.zoom / 2.0f;
        camDeltaY += deltaY * GameManager.MOVE_FACTOR * game.camera.zoom / 2.0f;
    }

    public void setupZircon() {
        final Screen screen = Screens.createScreenFor(zirconApplication.getTileGrid());

        Panel panel = Components.panel()
                .wrapWithBox(true)
                .withTitle("Test Window")
                .withSize(Sizes.create(20, 60))
                .withPosition(Positions.create(3, 3))
                .build();

        final Header header = Components.header()
                .withPosition(Positions.offset1x1())
                .withText("Header")
                .build();

        final CheckBox checkBox = Components.checkBox()
                .withText("Check me!")
                .withPosition(Positions.create(0, 1)
                        .relativeToBottomOf(header))
                .build();

        panel.addComponent(header);
        panel.addComponent(checkBox);
        screen.addComponent(panel);
        panel.applyColorTheme(ColorThemes.afterTheHeist());
        screen.display();
    }
}