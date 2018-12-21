package pt.up.fc.dcc.asura.lightningpong.messaging;

/**
 * Result of the execution of a command
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public enum CommandResult {
    SUCCESS,
    FAILURE_FROZEN,
    FAILURE_RECHARGING,
    FAILURE_OUT_OF_BOUNDS
}
