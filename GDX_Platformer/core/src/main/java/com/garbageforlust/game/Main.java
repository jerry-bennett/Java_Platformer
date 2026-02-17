package com.garbageforlust.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private LDTKWorld world;
    private LDTKLevel level;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        
        // Load the world file from your assets
        world = new LDTKWorld(Gdx.files.internal("map.ldtk"));
        
        // Get the first level
        level = world.getLevel(0);
        
        // Match camera to level size (or a fixed zoom)
        camera.setToOrtho(false, 800, 450); 
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // This one line renders your entire LDtk level!
        world.render(level); 
    }

}
