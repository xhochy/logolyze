package de.logotakt.logolyze.view.swing2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Graphical representation of an axis.
 */
@SuppressWarnings("serial")
public class Axis extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private AxisOrientation orientation;
    private List<String> values;
    private boolean highlighted = false;
    private double panelwidth = 0;

    private double offset = 0;
    private double offsetBeforeDrag = 0;
    private Point dragStart = null;

    private List<OffsetChangeListener> offsetChangeListeners;

    private static final Color HIGHLIGHT_COLOR = Color.LIGHT_GRAY;
    private static final Double SCROLL_AMOUNT = 0.05;

    /**
     * Create a new Axis instance without any values.
     * @param orientation The orientation of this axis.
     */
    public Axis(final AxisOrientation orientation) {
        this.orientation = orientation;
        values = new ArrayList<String>();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.offsetChangeListeners = new LinkedList<OffsetChangeListener>();
    }

    /**
     * Creates a new axis instance.
     * @param values The values displayed on this axis.
     */
    public Axis(final List<String> values) {
        this.values = values;
        setMinimumSize(getMinimumSize());
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.offsetChangeListeners = new LinkedList<OffsetChangeListener>();
    }

    /**
     * Creates a new axis instance with already known orientation.
     * @param values The values displayed on this axis.
     * @param orientation The orientation of this axis, this is used for drawing.
     */
    public Axis(final List<String> values, final AxisOrientation orientation) {
        this(values);
        this.orientation = orientation;
    }

    /**
     * Set the width of the panel.
     * @param width The width of the panel.
     */
    public void setPanelWidth(final double width) {
        this.panelwidth = width;
        this.offsetChanged();
        this.repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(50, 50);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50, 50);
    }

    /**
     * Add a listener that reacts to a change of the offset.
     * @param l The listener that will be notitied on offset changes.
     */
    public void addOffsetChangeListener(final OffsetChangeListener l) {
        this.offsetChangeListeners.add(l);
    }

    private void paintArrow(final Graphics g) {
        int fieldOffset = 0;
        Dimension dim;
        dim = this.getSize();

        // Draw the axis
        if (this.orientation == AxisOrientation.XAxis) {
            g.drawLine(0, 1, (int) dim.getWidth(), 1);

            if (!this.values.isEmpty()) {
                g.drawLine((int) offset, 1, (int) offset, (int) (this.getHeight() * 0.7));
                g.drawLine((int) (this.values.size() * this.panelwidth + offset - 1), 1, (int) (this.values.size()
                        * this.panelwidth + offset - 1), (int) (this.getHeight() * 0.7));
            }
        } else {
            g.drawLine((int) (dim.getWidth() - 1), 1, (int) (dim.getWidth() - 1), (int) (dim.getHeight()));

            if (!this.values.isEmpty()) {
                g.drawLine((int) (dim.getWidth() - 1), (int) (offset + 1), (int) (this.getWidth() * 0.3),
                        (int) (offset + 1));

                g.drawLine((int) (dim.getWidth() - 1), (int) (this.values.size() * this.panelwidth + offset - 1),
                        (int) (this.getWidth() * 0.3), (int) (this.values.size() * this.panelwidth + offset - 1));
            }
        }

        // Draw the little stubs below the axis
        for (int i = 0; i < this.values.size(); i++) {

            fieldOffset = (int) (i * this.panelwidth);

            if (this.orientation == AxisOrientation.XAxis) {
                g.drawLine((int) (offset + fieldOffset + (this.panelwidth / 2.0)), 1,
                        (int) (offset + fieldOffset + (int) (this.panelwidth / 2.0)), (int) (dim.getHeight() * 0.1));
            } else {
                g.drawLine((int) (dim.getWidth() - 1), (int) (offset + fieldOffset + (int) (this.panelwidth / 2.0)),
                        (int) (dim.getWidth() * 0.9), (int) (offset + fieldOffset + +(int) (this.panelwidth / 2.0)));
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_UNCONFIRMED_CAST")
    private void paintLabels(final Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        FontMetrics fm = gr.getFontMetrics();

        for (int i = 0; i < this.values.size(); i++) {
            int stringwidth = fm.stringWidth(this.values.get(i));

            if (this.orientation == AxisOrientation.XAxis) {
                gr.drawString(this.values.get(i),
                        (int) (this.offset + (i * this.panelwidth) + 0.5 * (this.panelwidth - stringwidth)),
                        (int) (this.getHeight() * 0.5));
            } else {
                gr.translate(0.5 * this.getWidth(),
                        (this.offset + (i * this.panelwidth) + 0.5 * (this.panelwidth - stringwidth)));
                gr.rotate(Math.PI / 2);

                gr.drawString(this.values.get(i), 0, 0);

                gr.rotate(-1 * Math.PI / 2);
                gr.translate(-0.5 * this.getWidth(), -1
                        * (this.offset + (i * this.panelwidth) + 0.5 * (this.panelwidth - stringwidth)));
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_UNCONFIRMED_CAST")
    public void paintComponent(final Graphics g) {
        Graphics2D gr = (Graphics2D) g;

        if (this.highlighted) {
            Color old;

            old = gr.getColor();
            gr.setColor(Axis.HIGHLIGHT_COLOR);
            gr.fillRect(0, 0, this.getWidth(), this.getHeight());
            gr.setColor(old);
        } else {
            Color old;

            old = gr.getColor();
            gr.setColor(UIManager.getColor("Panel.background"));
            gr.fillRect(0, 0, this.getWidth(), this.getHeight());
            gr.setColor(old);

        }

        this.paintArrow(g);
        this.paintLabels(g);
    }

    /**
     * Get the orientation of this axis.
     * @return The orientation of this axis.
     */
    public AxisOrientation getOrientation() {
        return orientation;
    }

    /**
     * Set the orientation of this axis.
     * @param orientation The new orientation.
     */
    public void setOrientation(final AxisOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Set the list of shown values.
     * @param values The shown values.
     */
    public void setValues(final List<String> values) {
        OffsetEvent e;

        this.values = values;

        // Also, reset the offset
        this.offset = 0;
        e = new OffsetEvent(0);
        for (OffsetChangeListener l : this.offsetChangeListeners) {
            l.offsetChanged(e);
        }

        repaint();
    }

    /**
     * Get the list of shown values.
     * @return The list of shown values.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Get the index of the string on the Axis.
     * @param object The searched string.
     * @return The position where the string is placed.
     */
    public int getIndexOf(final String object) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(object)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Does the axis display any values?
     * @return True, if the axis does not display any values.
     */
    public boolean isBlank() {
        return values.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // Highlight it.
        if (!this.highlighted) {
            this.highlighted = true;
            this.repaint();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        // Un-highlight it.
        if (this.highlighted) {
            this.highlighted = false;
            this.repaint();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        this.offsetBeforeDrag = this.offset;
        this.dragStart = e.getPoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        this.dragStart = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (this.dragStart == null) {
            return;
        }

        if (this.orientation == AxisOrientation.XAxis) {
            this.offset = this.offsetBeforeDrag + (e.getX() - this.dragStart.getX());
        } else {
            this.offset = this.offsetBeforeDrag + (e.getY() - this.dragStart.getY());
        }

        this.offsetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(final MouseEvent arg0) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (this.orientation == AxisOrientation.XAxis) {
            this.offset += -1 * e.getWheelRotation() * this.getWidth() * Axis.SCROLL_AMOUNT;
        } else {
            this.offset += -1 * e.getWheelRotation() * this.getHeight() * Axis.SCROLL_AMOUNT;
        }

        this.offsetChanged();
    }
    
    private void offsetChanged() {
        OffsetEvent oe;
        double vspace;
        
        if (this.offset > 0) {
            this.offset = 0;
        }
        
        // Catch the case that nothing is selected, in which case Logolyze will show an 'empty graph' anyways.
        
        if (this.values.size() == 0) {
        	if (this.orientation == AxisOrientation.XAxis) {
        		vspace = this.getWidth();
        	} else {
        		vspace = this.getHeight();
        	}
        } else {
        	vspace = this.values.size() * this.panelwidth;
        }

        if (this.orientation == AxisOrientation.XAxis) {
            if (this.offset < -1 * ((vspace - this.getWidth()))) {
                this.offset = -1 * (vspace - this.getWidth());
            }
        } else {
            if (this.offset < -1 * ((vspace - this.getHeight()))) {
                this.offset = -1 * (vspace - this.getHeight());
            }
        }

        oe = new OffsetEvent(this.offset);
        for (OffsetChangeListener l : this.offsetChangeListeners) {
            l.offsetChanged(oe);
        }

        this.repaint();
    }

    /**
     * Get the offset of the Axis (how far the user scrolled to the side).
     * @return The offset of the Axis.
     */
    public double getOffset() {
        return this.offset;
    }

    /**
     * Get the offset, measured in panelwidths. (i.e. how many panels the user scrolled)
     * @return the offset, measured in panelwidths
     */
    public double getPanelOffset() {
        return -1 * (this.offset / this.panelwidth);
    }

    /**
     * Sets the offset of the axis, measured in panelwidhts.
     * @param panels how many panels the axis should be scrolled over
     */
    public void setPanelOffset(final double panels) {
        this.offset = -1 * this.panelwidth * panels;
        this.offsetChanged();
    }

    /**
     * Set the offset of the Axis.
     * @param offset The new offset of the axis.
     */
    public void setOffset(final double offset) {
        this.offset = offset;
        this.offsetChanged();
    }
}
