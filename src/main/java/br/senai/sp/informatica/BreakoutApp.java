package br.senai.sp.informatica;

import static br.senai.sp.informatica.BreakoutType.BALL;
import static br.senai.sp.informatica.BreakoutType.BAT;
import static br.senai.sp.informatica.BreakoutType.BRICK;
import static br.senai.sp.informatica.BreakoutType.FLOOR;
import static br.senai.sp.informatica.BreakoutType.WALL;
import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.byType;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxgl.dsl.FXGL.getSettings;
import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import static com.almasb.fxgl.dsl.FXGL.geti;
import static com.almasb.fxgl.dsl.FXGL.getip;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.loopBGM;
import static com.almasb.fxgl.dsl.FXGL.onCollisionBegin;
import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGL.setLevelFromMap;
import static com.almasb.fxgl.dsl.FXGL.spawn;

import java.util.Map;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;


public class BreakoutApp extends GameApplication {
    private static final int NIVEL_MAXIMO = 2;
    private static final int NIVEL_INICIAL = 1;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("My Breakout App");
        settings.setVersion("1.0");
        settings.setWidth(14*96);
        settings.setHeight(22*32);
    }

    @Override
    protected void onPreInit() {
        getSettings().setGlobalMusicVolume(0.5);
        getSettings().setGlobalSoundVolume(0.5);

       
        loopBGM("BGM.mp3");
        
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                getBatControl().left();
            }

            @Override
            protected void onActionEnd() {
                getBatControl().stop();
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                getBatControl().right();
            }

            @Override
            protected void onActionEnd() {
                getBatControl().stop();
            }
        }, KeyCode.RIGHT);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("vidas", 10);
        vars.put("placar", 0);
        vars.put("nivel", NIVEL_INICIAL);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BreakoutFactory());

        initTela();
        configuraNivel(NIVEL_INICIAL);
    }

    private void initTela() {
        getGameScene().setBackgroundColor(Color.BLACK);

        // Cria a Parede a Esquerda
        entityBuilder()
            .type(WALL)
            .at(0, 0)
            .bbox(new HitBox(BoundingShape.box(4, getAppHeight())))
            .with(new IrremovableComponent())
            .with(new PhysicsComponent())
            .with(new CollidableComponent(true))
            .buildAndAttach();

        // Cria a Parede a Direira
        entityBuilder()
            .type(WALL)
            .at(getAppWidth() - 4, 0)
            .bbox(new HitBox(BoundingShape.box(4, getAppHeight())))
            .with(new PhysicsComponent())
            .with(new IrremovableComponent())
            .with(new CollidableComponent(true))
            .buildAndAttach();

        // Cria o Teto
        entityBuilder()
            .type(WALL)
            .at(0, 0)
            .bbox(new HitBox(BoundingShape.box(getAppWidth(), 4)))
            .with(new IrremovableComponent())
            .with(new PhysicsComponent())
            .with(new CollidableComponent(true))
            .buildAndAttach();

        // Cria o Piso
        entityBuilder()
            .type(FLOOR)
            .at(0, getAppHeight())
            .bbox(new HitBox(BoundingShape.box(getAppWidth(), getAppHeight() - 4)))
            .with(new IrremovableComponent())
            .with(new CollidableComponent(true))
            .buildAndAttach();
    }

    private void configuraNivel(int nivelInicial) {
        // Inicializa o Jogo
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
        // Carrega o Nivel
       setLevelFromMap("tmx/level" + nivelInicial + ".tmx");
        // Cria a Bola
       spawn("ball", getAppWidth() / 2f, getAppHeight() - 250);
        // Cria a Barra
       spawn("bat", getAppWidth() / 2f, getAppHeight() - 180);
        // TODO: Lança a Bola
        
        getBallControl().release();
        
    }

    private void proximoNivel() {
        inc("nivel", +1);
        var levelNum = geti("nivel");

        if (levelNum > NIVEL_MAXIMO) {
            getDialogService()
                    .showMessageBox("Game Over", getGameController()::exit);
            return;
        }

        configuraNivel(levelNum);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);

        // Colisão da Bola no Bloco
        onCollisionBegin(BALL, BRICK, (ball, brick) -> {
            play("hit.wav");
            ball.call("onHit");
            brick.call("onHit");
            spawn("sparks",
                    new SpawnData(ball.getPosition()).put("color", getBallControl().getColor()));
            inc("placar", +50);
        });

        // Colisão da Bola na Barra
        onCollisionBegin(BAT, BALL, (bat, ball) -> {
            play("hit_bat.wav");
            ball.call("applySlow");
            bat.call("onHit");
        });

        // Colisão da Bola na Parede
        onCollisionBegin(BALL, WALL, (ball, wall) -> {
            play("hit_wall.wav");
            getGameScene().getViewport().shakeTranslational(1.5);
        });

        // Colisão da Bola no Piso
        onCollisionBegin(BALL, FLOOR, (ball, floor) -> {
            play("explosion.wav");
            inc("vidas", -1);
            if(geti("vidas") <= 0) {
                getDialogService()
                    .showMessageBox("Game Over", getGameController()::exit);
            } else {
                getGameWorld().getSingleton(BALL).removeFromWorld();
                spawn("ball", getAppWidth() / 2f, getAppHeight() - 250);
                getBallControl().release();
            }
        });
    }

    @Override
    protected void initUI() {
        // Apresenta o Placar e Vidas
        var placar = getUIFactoryService()
                .newText(getip("placar").asString());
        placar.textProperty().bind(
                getip("placar").asString("Pontos: [%s]"));
        addUINode(placar, 20, getAppHeight() - 20);
        var vidas = getUIFactoryService()
                .newText(getip("vidas").asString());
        vidas.textProperty().bind(
                getip("vidas").asString("Vidas: [%s]"));
        addUINode(vidas, 220, getAppHeight() - 20);
    }

    @Override
    protected void onUpdate(double tpf) {
       if (byType(BRICK).isEmpty()) {
           proximoNivel();
       } else {
           Point2D point = getGameWorld().getSingleton(BALL).getPosition();
           if(point.getY() < 0) {
               getGameWorld().getSingleton(BALL).removeFromWorld();
               spawn("ball", getAppWidth() / 2f, getAppHeight() - 250);
               getBallControl().release();
           }
       }
    }

    private BatComponent getBatControl() {
        return getGameWorld().getSingleton(BAT).getComponent(BatComponent.class);
    }

    private BallComponent getBallControl() {
        return getGameWorld().getSingleton(BALL).getComponent(BallComponent.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
