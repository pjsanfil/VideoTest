
package cameraops;

/**
 * Used to send configuration commands to listening Threads.
 * @author pjsanfil
 * @param <T> The enum type which is the possible values for the command.
 */
public abstract class ConfigCommand<T extends Enum> {
    protected T m_cmd;
    public void set(T cmd) {
        m_cmd = cmd;
    }
    public T get() {
        return m_cmd;
    }
}

