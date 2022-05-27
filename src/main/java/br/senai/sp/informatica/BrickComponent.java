package br.senai.sp.informatica;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.animationBuilder;

@SuppressWarnings("unused")
public class BrickComponent extends Component {
    private final Color color;
    private int lives = 2;

    public BrickComponent(Color color) {
        this.color = color;
    }

    public void onHit() {
        lives--;

        if (lives == 1) {
            playHitAnimation();

            entity.getViewComponent().clearChildren();

            var colorName = entity.getString("color");

            entity.getViewComponent().addChild(texture("brick_" + colorName + "_cracked.png"));
        } else if (lives == 0) {

            var colorName = entity.getString("color");
            var t = texture("brick_" + colorName + "_cracked.png");

            var textures = new ArrayList<Texture>();

            var t1 = t.subTexture(new Rectangle2D(0, 0, 32, 32));
            var t2 = t.subTexture(new Rectangle2D(32, 0, 32, 32));
            var t3 = t.subTexture(new Rectangle2D(32 + 32, 0, 32, 32));

            textures.add(t1);
            textures.add(t2);
            textures.add(t3);

            for (int i = 0; i < textures.size(); i++) {
                var te = textures.get(i);

                entityBuilder()
                        .at(entity.getPosition().add(i*32, 0))
                        .view(te)
                        .with(new ProjectileComponent(new Point2D(0, 1), random(550, 700)).allowRotation(false))
                        .with(new ExpireCleanComponent(Duration.seconds(0.7)).animateOpacity())
                        .buildAndAttach();
            }

            entity.removeFromWorld();
        }
    }

    private void playHitAnimation() {
        animationBuilder()
                .repeat(4)
                .autoReverse(true)
                .duration(Duration.seconds(0.02))
                .interpolator(Interpolators.BACK.EASE_OUT())
                .translate(entity)
                .from(entity.getPosition())
                .to(entity.getPosition().add(FXGLMath.random(5, 10), 0))
                .build();
    }
}
