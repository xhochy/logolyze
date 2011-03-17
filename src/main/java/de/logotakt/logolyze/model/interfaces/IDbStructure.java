package de.logotakt.logolyze.model.interfaces;

/**
 * This is the interface of a database structure returned
 * by the metadata parser. It exports anything that any
 * class outside the model should ever need from the
 * database structure.
 *
 * This interface extends Iterable, so that you are able to iterate over
 * all the cubes in this DbStructure.
 */
public interface IDbStructure
	extends Iterable<ICube> {
        /**
         * Returns a cube with the specified name, or null if there is no such
         * cube.
         *
         * @param name The name of the cube that should be returned
         * @return A cube with the given name, or null if such a cube does not exist
         */
	ICube getCube(String name);
}
