package br.senai.sp.informatica;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getSettings;
import static com.almasb.fxgl.dsl.FXGL.texture;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.TimeComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyDef;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;

import javafx.geometry.Point2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

enum BreakoutType {
    BAT, BALL, BRICK, WALL, FLOOR
}

public class BreakoutFactory implements EntityFactory {
    @Spawns("ball")
    public Entity newBall(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().restitution(1f).density(0.03f));

        var bd = new BodyDef();
        bd.setType(BodyType.DYNAMIC);
        bd.setFixedRotation(true);

        physics.setBodyDef(bd);

        var e = entityBuilder(data)
                .type(BreakoutType.BALL)
                .bbox(new HitBox(BoundingShape.circle(64)))
                .view("ball.png")
                .collidable()
                .with(physics)
                .with(new TimeComponent())
                .with(new EffectComponent())
                .with(new BallComponent())
                .scaleOrigin(0, 0)
                .scale(0.1, 0.1)
                .build();

        var emitter = ParticleEmitters.newFireEmitter();
        emitter.setSourceImage(texture("ball.png"));
        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setNumParticles(1);
        emitter.setEmissionRate(1);
        emitter.setSpawnPointFunction(i -> new Point2D(0, 0));
        emitter.setScaleFunction(i -> new Point2D(-0.1, -0.1));
        emitter.setExpireFunction(i -> Duration.seconds(1));

        emitter.setEntityScaleFunction(() -> new Point2D(0.1, 0.1));
        emitter.setScaleOriginFunction(i -> new Point2D(0, 0));

        emitter.minSizeProperty().bind(e.getTransformComponent().scaleXProperty().multiply(60));
        emitter.maxSizeProperty().bind(e.getTransformComponent().scaleXProperty().multiply(60));

        e.addComponent(new ParticleComponent(emitter));

        return e;
    }

    @Spawns("bat")
    public Entity newBat(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        return entityBuilder(data)
                .type(BreakoutType.BAT)
                .at(getAppWidth() / 2.0 - 50, getAppHeight() - 70)
                .viewWithBBox(texture("bat.png", 464 / 3.0, 102 / 3.0))
                .scaleOrigin(464 / 3.0 / 2, 0)
                .collidable()
                .with(physics)
                .with(new EffectComponent())
                .with(new BatComponent(texture("bat_hit.png", 464 / 3.0, 102 / 3.0)))
                .build();
    }

    @Spawns("brick")
    public Entity newBrick(SpawnData data) {
        var color = Color.valueOf(data.<String>get("color").toUpperCase());

        return entityBuilder(data)
                .type(BreakoutType.BRICK)
                .bbox(new HitBox(BoundingShape.box(96, 32)))
                .collidable()
                .with(new PhysicsComponent())
                .with(new BrickComponent(color))
                .build();
    }

    @Spawns("sparks")
    public Entity newSparks(SpawnData data) {
        Color color = data.get("color");

        var e = entityBuilder(data)
                .with(new ExpireCleanComponent(Duration.seconds(1.5)))
                .build();

        if (!getSettings().isNative()) {
            var emitter = ParticleEmitters.newExplosionEmitter(24);
            emitter.setSourceImage(texture("particles/smoke_06.png", 16, 16).multiplyColor(color));
            emitter.setSize(4, 16);
            emitter.setMaxEmissions(1);
            emitter.setExpireFunction(i -> Duration.seconds(FXGLMath.random(0.25, 1.0)));
            emitter.setBlendMode(BlendMode.ADD);
            emitter.setNumParticles(20);

            e.addComponent(new ParticleComponent(emitter));
        }

        return e;
    }
}
