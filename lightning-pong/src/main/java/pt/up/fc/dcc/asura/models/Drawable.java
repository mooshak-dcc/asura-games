package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;

import java.util.Map;

/**
 * Interface implemented by game objects that can be drawn on the game movie
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public interface Drawable {

    /**
     * Draw the model on the game movie.
     *
     * @param builder {@link GameMovieBuilder} the game movie builder
     */
    void draw(GameMovieBuilder builder);
}
