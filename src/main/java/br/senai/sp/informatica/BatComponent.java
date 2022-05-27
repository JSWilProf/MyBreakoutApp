package br.senai.sp.informatica;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.effect.Effect;
import javafx.scene.effect.MotionBlur;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BatComponent extends Component {
    private static final int BAT_SPEED = 2050;
    private static final float BOUNCE_FACTOR = 1.5f;
    private static final float SPEED_DECAY = 0.66f;

    private PhysicsComponent physics;
    private float speed = 0;

    private Vec2 velocity = new Vec2();

    private Effect blur = new MotionBlur();

    private Texture textureOnHit;

    public BatComponent(Texture textureOnHit) {
        this.textureOnHit = textureOnHit;
    }

    @Override
    public void onUpdate(double tpf) {
        speed = BAT_SPEED * (float) tpf;

        velocity.mulLocal(SPEED_DECAY);

        if (entity.getX() < 0) {
            velocity.set(BOUNCE_FACTOR * (float) -entity.getX(), 0);
        } else if (entity.getRightX() > getAppWidth()) {
            velocity.set(BOUNCE_FACTOR * (float) -(entity.getRightX() - getAppWidth()), 0);
        }

        physics.setBodyLinearVelocity(velocity);
    }

    public void left() {
        velocity.set(-speed, 0);
        applyMoveEffects();
    }

    public void right() {
        velocity.set(speed, 0);
        applyMoveEffects();
    }

    private void applyMoveEffects() {
        entity.setScaleX(1.05);
        entity.setScaleY(1 / entity.getScaleX());
        entity.getViewComponent().getParent().setEffect(blur);
    }

    public void stop() {
        entity.setScaleX(1);
        entity.setScaleY(1);
        entity.getViewComponent().getParent().setEffect(null);
    }

    public void onHit() {
        entity.getComponent(EffectComponent.class).startEffect(new HitEffect());
    }

    public class HitEffect extends com.almasb.fxgl.dsl.components.Effect {

        public HitEffect() {
            super(Duration.seconds(0.1));
        }

        @Override
        public void onStart(Entity entity) {
            entity.getViewComponent().addChild(textureOnHit);
        }

        @Override
        public void onEnd(Entity entity) {
            entity.getViewComponent().removeChild(textureOnHit);
        }
    }
}
