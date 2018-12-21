package pt.up.fc.dcc.asura.lightningpong.models;

/**
 * Interface implemented by actors that can be frozen
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public interface Freezable {

    boolean isFrozen();

    void freeze();
}
