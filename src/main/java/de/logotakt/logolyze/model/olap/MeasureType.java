package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;

/**
 * This class represents a type of measures, i.e. the name and the
 * class of a measure, and whether it is associated to nodes or
 * edges.
 */
public class MeasureType implements
        IMeasureType {

    private final MeasureAssociation ma;
    private final String key;
    private final MeasureClass mc;
    private final String column;

    MeasureType(final String key, final MeasureAssociation ma, final MeasureClass mc, final String column) {
        this.key = key;
        this.ma = ma;
        this.mc = mc;
        this.column = column;
    }

    String getColumn() {
        return this.column;
    }

    @Override
    public MeasureAssociation getAssoc() {
        return this.ma;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public MeasureClass getMeasureClass() {
        return this.mc;
    }
}
