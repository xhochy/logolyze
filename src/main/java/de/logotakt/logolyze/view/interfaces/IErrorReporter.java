package de.logotakt.logolyze.view.interfaces;

/**
 * This interface is implemented by classes that are in some way able to present an error message to a user.
 * @author s_paulss
 */
public interface IErrorReporter {
    /**
     * Present an error to the user.
     * @param err the error to report
     */
    void displayError(final String err);
}
