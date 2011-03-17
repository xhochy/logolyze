package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Iterator;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This validator tests whether a request is valid regarding the hierarchies sharing columns.
 * With such hierarchies present, of course a request may only select a value in one of those
 * hierarchies.
 */
public class ColumnDuplicationValidator implements Validator {

    @Override
    public void validate(final Request r) throws RequestValidationFailedException {
        Iterator<IConstraint> cit;
        Constraint c;
        HashMap<String, Hierarchy> columnMap;
        String column;

        cit = r.iterator();
        columnMap = new HashMap<String, Hierarchy>();

        while (cit.hasNext()) {
            c = (Constraint) cit.next();

            column = ((Hierarchy) c.getHierarchy()).getGraphColumn();

            if (columnMap.keySet().contains(column)
                    && (((Hierarchy) c.getHierarchy()).getGraphColumnValue() != null || columnMap.get(column) != ((Hierarchy) c.getHierarchy()))) {
                throw new RequestValidationFailedException(
                        "This Request containts constraints in two or more hierarchies sharing one graph column."
                        + " The column is: " + column);
            }

            columnMap.put(column, ((Hierarchy) c.getHierarchy()));
        }
    }
}
