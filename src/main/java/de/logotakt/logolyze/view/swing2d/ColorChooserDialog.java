package de.logotakt.logolyze.view.swing2d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This dialog allows the User to select a Color using three sliders.
 */
public class ColorChooserDialog extends JDialog {

    private static final long serialVersionUID = -1200872429493327656L;
    private final JPanel contentPanel = new JPanel();
    private final JPanel previewPanel;
    private final JLabel labelCode;
    private final JSlider sliderR;
    private final JSlider sliderG;
    private final JSlider sliderB;
    private List<ChangeListener> listeners;

    /**
     * Return the color that is currently selected by the user.
     * @return The current color.
     */
    public Color getColor() {
        return new Color(this.sliderR.getValue(), this.sliderG.getValue(), this.sliderB.getValue());
    }

    private void updateColor() {
        Color c;
        String fillR, fillG, fillB;

        c = new Color(this.sliderR.getValue(), this.sliderG.getValue(), this.sliderB.getValue());
        this.previewPanel.setBackground(c);

        if (this.sliderR.getValue() < 0x10) {
            fillR = "0";
        } else {
            fillR = "";
        }

        if (this.sliderG.getValue() < 0x10) {
            fillG = "0";
        } else {
            fillG = "";
        }

        if (this.sliderB.getValue() < 0x10) {
            fillB = "0";
        } else {
            fillB = "";
        }

        this.labelCode.setText("0x"
                + fillR + Integer.toHexString(this.sliderR.getValue())
                + fillG + Integer.toHexString(this.sliderG.getValue())
                + fillB + Integer.toHexString(this.sliderB.getValue()));
    }

    /**
     * Create the dialog.
     * @param initial the initial color to display.
     */
    public ColorChooserDialog(final Color initial) {
        setTitle("Choose a Color - Logolyze");
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gblContentPane = new GridBagLayout();
        gblContentPane.columnWidths = new int[]{200, 200, 0};
        gblContentPane.rowHeights = new int[]{16, 0, 0, 0, 0, 0, 0};
        gblContentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gblContentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gblContentPane);
        {
            JLabel labelR = new JLabel("Red:");
            GridBagConstraints gbcLabelR = new GridBagConstraints();
            gbcLabelR.insets = new Insets(0, 0, 5, 5);
            gbcLabelR.gridx = 0;
            gbcLabelR.gridy = 0;
            contentPanel.add(labelR, gbcLabelR);
        }
        {
            this.sliderR = new JSlider();
            sliderR.setMaximum(255);
            sliderR.setName("sliderR");
            GridBagConstraints gbcSliderR = new GridBagConstraints();
            gbcSliderR.fill = GridBagConstraints.HORIZONTAL;
            gbcSliderR.anchor = GridBagConstraints.NORTH;
            gbcSliderR.insets = new Insets(0, 0, 5, 0);
            gbcSliderR.gridx = 1;
            gbcSliderR.gridy = 0;
            contentPanel.add(sliderR, gbcSliderR);

            sliderR.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent arg0) {
                    ColorChooserDialog.this.updateColor();
                }
            });
        }
        {
            JLabel labelG = new JLabel("Green:");
            GridBagConstraints gbcLabelG = new GridBagConstraints();
            gbcLabelG.insets = new Insets(0, 0, 5, 5);
            gbcLabelG.gridx = 0;
            gbcLabelG.gridy = 1;
            contentPanel.add(labelG, gbcLabelG);
        }
        {
            this.sliderG = new JSlider();
            sliderG.setName("sliderG");
            sliderG.setMaximum(255);
            GridBagConstraints gbcSliderG = new GridBagConstraints();
            gbcSliderG.fill = GridBagConstraints.HORIZONTAL;
            gbcSliderG.insets = new Insets(0, 0, 5, 0);
            gbcSliderG.anchor = GridBagConstraints.NORTH;
            gbcSliderG.gridx = 1;
            gbcSliderG.gridy = 1;
            contentPanel.add(sliderG, gbcSliderG);

            sliderG.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent arg0) {
                    ColorChooserDialog.this.updateColor();
                }
            });
        }
        {
            JLabel labelB = new JLabel("Blue");
            GridBagConstraints gbcLabelB = new GridBagConstraints();
            gbcLabelB.insets = new Insets(0, 0, 5, 5);
            gbcLabelB.gridx = 0;
            gbcLabelB.gridy = 2;
            contentPanel.add(labelB, gbcLabelB);
        }
        {
            this.sliderB = new JSlider();
            sliderB.setMaximum(255);
            sliderB.setName("sliderB");
            GridBagConstraints gbcSliderB = new GridBagConstraints();
            gbcSliderB.fill = GridBagConstraints.HORIZONTAL;
            gbcSliderB.insets = new Insets(0, 0, 5, 0);
            gbcSliderB.gridx = 1;
            gbcSliderB.gridy = 2;
            contentPanel.add(sliderB, gbcSliderB);

            sliderB.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent arg0) {
                    ColorChooserDialog.this.updateColor();
                }
            });
        }
        {
            JLabel labelRGB = new JLabel("RGB Code");
            GridBagConstraints gbcLabelRGB = new GridBagConstraints();
            gbcLabelRGB.insets = new Insets(0, 0, 5, 5);
            gbcLabelRGB.gridx = 0;
            gbcLabelRGB.gridy = 4;
            contentPanel.add(labelRGB, gbcLabelRGB);
        }
        {
            this.labelCode = new JLabel("");
            GridBagConstraints gbcLabelCode = new GridBagConstraints();
            gbcLabelCode.insets = new Insets(0, 0, 5, 0);
            gbcLabelCode.gridx = 1;
            gbcLabelCode.gridy = 4;
            contentPanel.add(labelCode, gbcLabelCode);
        }
        {
            JLabel labelPreview = new JLabel("Preview");
            GridBagConstraints gbcLabelPreview = new GridBagConstraints();
            gbcLabelPreview.insets = new Insets(0, 0, 0, 5);
            gbcLabelPreview.gridx = 0;
            gbcLabelPreview.gridy = 5;
            contentPanel.add(labelPreview, gbcLabelPreview);
        }
        {
            this.previewPanel = new JPanel();
            GridBagConstraints gbcPanel = new GridBagConstraints();
            gbcPanel.fill = GridBagConstraints.BOTH;
            gbcPanel.gridx = 1;
            gbcPanel.gridy = 5;
            contentPanel.add(previewPanel, gbcPanel);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton applyButton = new JButton("Apply");
                applyButton.setActionCommand("Apply");
                applyButton.setName("apply");
                buttonPane.add(applyButton);

                applyButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        ChangeEvent ev;

                        ev = new ChangeEvent(ColorChooserDialog.this);
                        for (ChangeListener l : ColorChooserDialog.this.listeners) {
                            l.stateChanged(ev);
                        }
                    }

                });
            }
            {
                JButton okButton = new JButton("OK");
                okButton.setActionCommand("OK");
                okButton.setName("ok");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);

                okButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        ChangeEvent ev;

                        ev = new ChangeEvent(ColorChooserDialog.this);
                        for (ChangeListener l : ColorChooserDialog.this.listeners) {
                            l.stateChanged(ev);
                        }

                        ColorChooserDialog.this.dispose();
                    }

                });
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                cancelButton.setName("cancel");
                buttonPane.add(cancelButton);

                cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        ColorChooserDialog.this.dispose();
                    }

                });
            }

            this.sliderR.setValue(initial.getRed());
            this.sliderG.setValue(initial.getGreen());
            this.sliderB.setValue(initial.getBlue());

            this.updateColor();
        }

        this.listeners = new LinkedList<ChangeListener>();
    }

    /**
     * Add a listener for color changes in this dialog.
     * @param l The ChangeListener instance to add.
     */
    public void addChangeListener(final ChangeListener l) {
        this.listeners.add(l);
    }

    /**
     * Remove a listener for color changes in this dialog.
     * @param l The ChangeListener instance to remove.
     */
    public void removeChangeListneer(final ChangeListener l) {
        this.listeners.remove(l);
    }
}
