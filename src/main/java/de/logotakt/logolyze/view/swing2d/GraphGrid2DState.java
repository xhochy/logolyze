package de.logotakt.logolyze.view.swing2d;


/**
 * Class to externalise the state of a {@link GraphGrid2D}.
 */
public class GraphGrid2DState implements IState {
    private IState xAxis;
    private IState yAxis;
    private IState measureState;
    private Double xAxisOffset;
    private Double yAxisOffset;

    /**
     * Get the saved offset of the X Axis.
     * @return The scroll offset of the X axis.
     */
    public Double getxAxisOffset() {
        return xAxisOffset;
    }

    /**
     * Set the offset of the X Axis.
     * @param xAxisOffset The offset of the X Axis.
     */
    public void setxAxisOffset(final Double xAxisOffset) {
        this.xAxisOffset = xAxisOffset;
    }

    /**
     * Get the saved offset of the Y Axis.
     * @return The scroll offset of the Y axis.
     */
    public Double getyAxisOffset() {
        return yAxisOffset;
    }

    /**
     * Set the offset of the Y Axis.
     * @param yAxisOffset The offset of the Y Axis.
     */
    public void setyAxisOffset(final Double yAxisOffset) {
        this.yAxisOffset = yAxisOffset;
    }


    /**
     * return the state of the measure config view.
     * @return the state of the measure config view.
     */
    public IState getMeasureState() {
        return measureState;
    }

    /**
     * set the state of the measure config view.
     * @param measureState the state to set.
     */
    public void setMeasureState(final IState measureState) {
        this.measureState = measureState;
    }

    /**
     * return the state of the X-axis view.
     * @return the state of the X-axis view.
     */
    public IState getxAxis() {
        return xAxis;
    }

    /**
     * set the state of the X-axis view.
     * @param xAxis the state of the X-axis view.
     */
    public void setxAxis(final IState xAxis) {
        this.xAxis = xAxis;
    }

    /**
     * return the state of the Y-axis view.
     * @return the state of the Y-axis view.
     */
    public IState getyAxis() {
        return yAxis;
    }

    /**
     * set the state of the Y-axis view.
     * @param yAxis the state of the Y-axis view.
     */
    public void setyAxis(final IState yAxis) {
        this.yAxis = yAxis;
    }
}
