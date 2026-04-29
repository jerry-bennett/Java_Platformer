package com.garbageforlust.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenu implements Screen {
    private final Main game; 
    private Stage stage;
    private Skin skin;

    public MainMenu(Main game) {
        this.game = game; // Store the reference to our Main class
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Ensure you have uiskin.json in assets/ui/
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("PLATFORM GAME", skin);
        table.add(titleLabel).padBottom(50).row();

        TextButton newGameBtn = new TextButton("New Game", skin);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Switch to GameScreen, passing the Main instance 'game'
                game.setScreen(new GameScreen());
            }
        });
        table.add(newGameBtn).fillX().uniformX().pad(10).row();

        TextButton exitBtn = new TextButton("Exit", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(exitBtn).fillX().uniformX().pad(10);
    }

    // REMOVED: The second duplicate constructor that was here

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override 
    public void dispose() { 
        stage.dispose(); 
        skin.dispose(); 
    }
}