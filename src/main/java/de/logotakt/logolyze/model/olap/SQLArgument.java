package de.logotakt.logolyze.model.olap;

/**
 * Class to store a SQL argument.
 */
class SQLArgument {
    private ArgumentType type;
    private String val;

    public SQLArgument(final ArgumentType type, final String val) {
        this.setType(type);
        this.setVal(val);
    }

    void setType(final ArgumentType type) {
        this.type = type;
    }

    ArgumentType getType() {
        return type;
    }

    void setVal(final String val) {
        this.val = val;
    }

    String getVal() {
        return val;
    }
}
