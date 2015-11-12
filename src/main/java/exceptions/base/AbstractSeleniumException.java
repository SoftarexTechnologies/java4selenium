package exceptions.base;

/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 */
public class AbstractSeleniumException extends Exception {
    /**
     * Constructs an <code>AbstractSeleniumException</code> with no
     * detail message.
     */
    public AbstractSeleniumException() {
        super();
    }

    /**
     * Constructs an <code>AbstractSeleniumException</code> with the
     * specified detail message.
     *
     * @param   s   the detail message.
     */
    public AbstractSeleniumException(String s) {
        super(s);
    }
}
