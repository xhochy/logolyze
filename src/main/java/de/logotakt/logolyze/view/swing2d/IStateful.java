package de.logotakt.logolyze.view.swing2d;

/**
 * Objects that are able to externalize their state.
 */
public interface IStateful {

    /**
     * Return a newly created State object.
     * @return The State object.
     */
    IState getState();

    /**
     * Set a state.
     * @param state The State to set.
     */
    void setState(IState state);
}
