package pt.up.fc.dcc.asura.models.effects;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.models.HasTeam;
import pt.up.fc.dcc.asura.utils.Vector;

public class BulletHitEffect extends EffectActor implements HasTeam {
    private static final String SPRITE_ID_FORMAT = "bh%d";
    private static final String[] SPRITES = {
            "bh1.png", "bh2.png", "bh3.png", "bh4.png"
    };
    private static final int SPRITE_SIZE = 64;
    private static final int ANIMATION_LENGTH = 4;

    private int teamNr;
    private int heading;

    public BulletHitEffect(long startTime, int teamNr, Vector position, int heading) {
        this(startTime, teamNr, position, heading, new Vector(0, 0));
    }

    public BulletHitEffect(long startTime, int teamNr, Vector position, int heading, Vector velocity) {
        super(startTime, String.format(SPRITE_ID_FORMAT, teamNr), SPRITE_SIZE, position,
                velocity, ANIMATION_LENGTH * 2);
        this.teamNr = teamNr;
        this.position = position;
        this.heading = heading;
        this.velocity = velocity;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
        this.animationSpeed = 0.5;
    }

    @Override
    public int getTeamNr() {
        return teamNr;
    }

    @Override
    public void draw(long time, GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, 16, heading);
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
