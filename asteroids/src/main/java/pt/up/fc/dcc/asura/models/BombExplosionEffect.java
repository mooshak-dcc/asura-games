package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

public class BombExplosionEffect extends EffectActor implements HasTeam {
    private static final String SPRITE_ID_FORMAT = "be%d";
    private static final String[] SPRITES = {
            "be1.png", "be2.png", "be3.png", "be4.png"
    };
    private static final int SPRITE_SIZE = 192;
    private static final int ANIMATION_LENGTH = 20;

    private int teamNr;
    private int heading;

    public BombExplosionEffect(int teamNr, Vector position, int heading) {
        this(teamNr, position, heading, new Vector(0, 0));
    }

    public BombExplosionEffect(int teamNr, Vector position, int heading, Vector velocity) {
        super(String.format(SPRITE_ID_FORMAT, teamNr), SPRITE_SIZE, position,
                velocity, ANIMATION_LENGTH * 2);
        this.teamNr = teamNr;
        this.position = position;
        this.heading = heading;
        this.velocity = velocity;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
        this.animationSpeed = 1;
    }

    @Override
    public int getTeamNr() {
        return teamNr;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, 100, heading);
    }

    /**
     * Load all sprites to the {@link GameMovieBuilder} builder
     *
     * @param builder {@link GameMovieBuilder} builder
     */
    public static void loadSprites(GameMovieBuilder builder) {
        for (int i = 0; i < SPRITES.length; i++) {
            builder.addSprite(String.format(SPRITE_ID_FORMAT, i), SPRITES[i]);
        }
    }
}
