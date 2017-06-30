import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/*
 * Needs to be added to a GridBagLayout with 8 columns
 */
public class SingleScrollbarUpdater implements ValueListener {
    private final FitValue val;
    private final String prefix;
    private final double multiplier;

    private JLabel valLabel;
    private JButton minButton;
    private JButton minx2Button;
    private JLabel minLabel;
    private JSlider slider;
    private JLabel maxLabel;
    private JButton maxButton;
    private JButton maxx2Button;
    private JButton rangeButton;
    private JCheckBox enableCheck;

    private boolean valueChangeLock = false;
    private boolean viewChangeLock = false;

    private final static int SLIDER_STEPS = 1000;

    private boolean noRecursion = false;

    private boolean minIsZero;

    public SingleScrollbarUpdater(final FitValue val, final String prefix, final double multiplier, boolean minIsZero) {
        this.val = val;
        this.prefix = prefix;
        this.multiplier = multiplier;
        this.valLabel = new JLabel("");
        this.minLabel = new JLabel("");
        this.maxLabel = new JLabel("");
        this.minIsZero = minIsZero;

        this.valLabel.setText(prefix + " = " + String.format(Locale.US,"%.6g",-1e-4)+" ");
        this.minLabel.setText(String.format(Locale.US,"%.6g",-1e-4)+" ");
        this.maxLabel.setText(String.format(Locale.US,"%.6g",-1e-4)+" ");

        this.valLabel.setPreferredSize(new Dimension(this.valLabel.getMinimumSize()));
        this.minLabel.setPreferredSize(new Dimension(this.minLabel.getMinimumSize()));
        this.maxLabel.setPreferredSize(new Dimension(this.maxLabel.getMinimumSize()));

        this.enableCheck = new JCheckBox("fit");
        this.enableCheck.setEnabled(val.isSupported());
        this.minButton = new JButton("<");
        this.minButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                val.setValues(val.getExpected(),val.getExpected(),val.getMax(),val.getEnabled());
            }
        });
        this.minx2Button = new JButton("2");
        this.minx2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                double newMin = val.getMin()-(val.getMax()-val.getMin());
                if (val.getMax() == val.getMin())
                {
                    newMin = val.getMin() - 1/multiplier;
                }
                if (minIsZero && newMin < 0)
                {
                    newMin = 0;
                }
                val.setValues(newMin,val.getExpected(),val.getMax(),val.getEnabled());
            }
        });
        this.maxButton = new JButton(">");
        this.maxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                val.setValues(val.getMin(),val.getExpected(),val.getExpected(),val.getEnabled());
            }
        });
        this.maxx2Button = new JButton("2");
        this.maxx2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                double newMax = val.getMax()+(val.getMax()-val.getMin());
                if (val.getMax() == val.getMin())
                {
                    newMax = val.getMax() + 1/multiplier;
                }
                val.setValues(val.getMin(),val.getExpected(),newMax,val.getEnabled());
            }
        });
        this.rangeButton = new JButton("Edit");
        this.rangeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Dialog ownerDialog = DialogUtil.getOwnerDialog(rangeButton);
                FitRangeDialog dialog;
                if(ownerDialog != null)
                    dialog = new FitRangeDialog(ownerDialog,prefix, null); /* TODO: frame */
                else
                    dialog = new FitRangeDialog(DialogUtil.getOwnerFrame(rangeButton),prefix, null); /* TODO: frame */
                dialog.call(val,multiplier);
                dialog.dispose();
            }
        });

        this.slider = new JSlider(0,SLIDER_STEPS);


        this.slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sliderChanged();
            }
        });

        this.enableCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    viewChangeLock = true;
                    if(!valueChangeLock && val.getEnabled() != enableCheck.isSelected())
                        val.setEnabled(enableCheck.isSelected());
                }
                finally {
                    viewChangeLock = false;
                }
            }
        });
        val.addValueListener(this);

        valueChanged(null); /* must call this to set lock */
    }

    /*
     * When the slider is adjusted, we modify the value. This causes the slider
     * to be readjusted, remodifying the value etc. It is an infinite loop which
     * must be prevented.
     *
     * The loop is prevented by adding two locks, valueChangeLock and viewChangeLock.
     * The value is never modified in any listener if the valueChangeLock is set. The
     * same also applies to viewChangeLock.
     *
     * value changed
     * -> set valueChangeLock
     * -> change labels
     * -> change sliders
     *    -> slider changed, does not change the value because the valueChangeLock is set
     *
     * slider changed
     * -> set viewChangeLock
     * -> change value
     *    -> value changed
     *      -> change labels
     *      -> does not change the slider because the viewChangeLock is set
     *
     * The locking works perfectly with multiple copies of the same layer.
     */

    private void sliderChanged() {
        if(!valueChangeLock) {
            double newval = slider.getValue() * (val.getMax()-val.getMin())/SLIDER_STEPS+val.getMin();
            try {
                viewChangeLock = true;
                /* here we could add a slider.getValueIsAdjusting() check, but then
                 * we would have to update the label manually */
                val.setExpected(newval);
            }
            finally {
                viewChangeLock = false;
            }
        }
    }
    public void valueChanged(ValueEvent ev) {
        try {
            valueChangeLock = true;
            valueChanged();
        }
        finally {
            valueChangeLock = false;
        }
    }

    private void valueChanged() {
        valLabel.setText(prefix + " = " + String.format(Locale.US,"%.6g",val.getExpected()*multiplier));
        minLabel.setText(String.format(Locale.US,"%.6g",val.getMin()*multiplier));
        maxLabel.setText(String.format(Locale.US,"%.6g",val.getMax()*multiplier));
        if(!viewChangeLock) {
            enableCheck.setSelected(val.getEnabled());
            slider.setValue((int)(SLIDER_STEPS*(val.getExpected()-val.getMin())/(val.getMax()-val.getMin()) + 0.5));
        }
    }



    public void addToGridBag(Container sliders) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        sliders.add(valLabel,c);
        sliders.add(minx2Button,c);
        sliders.add(minButton,c);
        sliders.add(minLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.gridwidth = GridBagConstraints.RELATIVE;
        c.gridwidth = 1;
        c.weightx = 2;

        sliders.add(slider,c);

        c.fill = GridBagConstraints.NONE;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0;
        sliders.add(maxLabel,c);
        sliders.add(maxButton,c);
        sliders.add(maxx2Button,c);
        sliders.add(rangeButton,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        sliders.add(enableCheck,c);
    }

    /** must remove listeners manually */
    public void cleanup() {
        val.removeValueListener(this);
    }
}



