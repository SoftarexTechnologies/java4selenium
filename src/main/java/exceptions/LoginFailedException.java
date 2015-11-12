package exceptions;

import exceptions.base.AbstractSeleniumException;

/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 */
public class LoginFailedException extends AbstractSeleniumException {
    /**
     * Constructs an <code>LoginFailedException</code> with no
     * detail message.
     */
    public LoginFailedException() {
        super();
    }

    /**
     * Constructs an <code>LoginFailedException</code> with the
     * specified detail message.
     *
     * @param   s   the detail message.
     */
    public LoginFailedException(String s) {
        super(s);
    }
}
