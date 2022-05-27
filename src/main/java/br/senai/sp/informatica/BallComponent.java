package br.senai.sp.informatica;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.Effect;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BallComponent extends Component {
    private static final Color[] COLORS = new Color[] {
            Color.WHITE, Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN
    };
    private static final int BALL_MIN_SPEED = 400;
    private static final int BALL_SLOW_SPEED = 100;
    private final Texture[] textures = new Texture[COLORS.length];
    private int colorIndex = 0;
    private PhysicsComponent physics;
    private EffectComponent effectComponent;
    private boolean checkVelocityLimit = false;
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.WHITE);

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    @Override
    public void onUpdate(double tpf) {
        if (checkVelocityLimit) {
            limitVelocity();
        }
    }

    private void limitVelocity() {
        if (abs(physics.getVelocityX()) < BALL_MIN_SPEED) {
            double signX = signum(physics.getVelocityX());

            if (signX == 0.0)
                signX = 1.0;

            physics.setVelocityX(signX * BALL_MIN_SPEED);

        }

        if (abs(physics.getVelocityY()) < BALL_MIN_SPEED) {
            double signY = signum(physics.getVelocityY());

            if (signY == 0.0)
                signY = -1.0;

            physics.setVelocityY(signY * BALL_MIN_SPEED);
        }
    }

    public void changeColorToNext() {
        entity.getViewComponent().removeChild(textures[colorIndex]);

        colorIndex++;

        if (colorIndex == textures.length)
            colorIndex = 0;

        setColor(COLORS[colorIndex]);

        if (!FXGL.getSettings().isNative()) {
            var emitter = entity.getComponent(ParticleComponent.class).getEmitter();
            emitter.setSourceImage(textures[colorIndex]);
        }

        entity.getViewComponent().addChild(textures[colorIndex]);
    }

    public Color getNextColor() {
        int nextIndex = colorIndex + 1;

        if (nextIndex == textures.length) {
            nextIndex = 0;
        }

        return COLORS[nextIndex];
    }

    public void release() {
        checkVelocityLimit = true;

        physics.setBodyLinearVelocity(new Vec2(5, 5));
    }

    public void onHit() {
        effectComponent.startEffect(new HighlightEffect());
    }

    public void applySlow() {
        effectComponent.startEffect(new SlowEffect());
    }

    public class HighlightEffect extends Effect {
        public HighlightEffect() {
            super(Duration.seconds(0.05));
        }

        @Override
        public void onStart(Entity entity) {
        }

        @Override
        public void onEnd(Entity entity) {
        }
    }

    public class SlowEffect extends Effect {
        public SlowEffect() {
            super(Duration.seconds(0.15));
        }

        @Override
        public void onStart(Entity entity) {
            checkVelocityLimit = false;
        }

        @Override
        public void onUpdate(Entity entity, double tpf) {
            double signX = signum(physics.getVelocityX());
            double signY = signum(physics.getVelocityY());

            physics.setLinearVelocity(signX * BALL_SLOW_SPEED, signY * BALL_SLOW_SPEED);
        }

        @Override
        public void onEnd(Entity entity) {
            checkVelocityLimit = true;
        }
    }
}
