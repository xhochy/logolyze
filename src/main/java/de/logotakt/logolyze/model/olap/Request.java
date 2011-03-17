package de.logotakt.logolyze.model.olap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This class represents a request to fetch a certain set of graphs from the database.
 */
public class Request implements IRequest {

    private List<IConstraint> constraints;
    private final Collection<Validator> validators;
    private List<MeasureType> measures;
    private Cube cube;

    /**
     * Creates a new, empty Request with the given validators.
     * @param validators The validators that should be used to validate this request
     */
    Request(final ICube c, final Collection<Validator> validators) {
        this.validators = validators;

        this.constraints = new LinkedList<IConstraint>();
        this.measures = new LinkedList<MeasureType>();
        this.cube = (Cube) c;
    }

    Collection<Validator> getValidators() {
        return this.validators;
    }

    /**
     * Checks whether this request is valid, based on the validators associated with this request.
     * @throws RequestValidationFailedException If any of the validators failed, this exception will be thrown with an
     *         error message
     */
    public void validate() throws RequestValidationFailedException {
        Iterator<Validator> it;

        it = this.validators.iterator();
        while (it.hasNext()) {
            it.next().validate(this);
        }

    }

    /**
     * Returns an Iterator over the Constraints of this Request., The resulting iterator is read-only.
     * @return an Iterator over the Constraints of this Request.
     */
    @Override
    public Iterator<IConstraint> iterator() {
        return Collections.unmodifiableCollection(constraints).iterator();
    }

    /**
     * Returns the MeasureTypes associated with this Request, i.e. the types of measures that should be included in the
     * graph fetched from the database.
     * @return The measuretypes belonging to this request
     */
    public Collection<MeasureType> getMeasures() {
        return this.measures;
    }

    @Override
    public ICube getCube() {
        return this.cube;
    }

    @Override
    public void addConstraint(final IConstraint constraint) {
        this.constraints.add((Constraint) constraint);
    }

    @Override
    public void addMeasureType(final IMeasureType mt) {
        this.measures.add((MeasureType) mt);
    }

    @Override
    public void removeConstraint(final IConstraint constraint) {
        if (!this.constraints.remove(constraint)) {
            throw new IllegalArgumentException("You tried to remove a constraint that was never there.");
        }
    }

    @Override
    public void removeMeasureType(final IMeasureType mt) {
        if (!this.measures.remove((MeasureType) mt)) {
            throw new IllegalArgumentException("You tried to remove a measure that was never there.");
        }
    }

}
