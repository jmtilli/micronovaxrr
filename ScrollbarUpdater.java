import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/** Updates the slider GUI and its sliders when the model is modified; handles
 * scrollbar events.
 *
 * <p>
 *
 * Unfortunately this complex piece of Java code is not commented well.
 * Furthermore, it uses lots of difficult Java programming tricks, such as
 * inner classes. You have to try to understand this code without comments.
 *
 */

public class ScrollbarUpdater implements ListDataListener {
    private LayerStack ls;
    private JTabbedPane sliderPane;
    //private JSlider convolutionSlider;
    //private JLabel convolutionLabel;
    //private JSlider normalSlider;
    //private JLabel normalLabel;
    //private JSlider sumSlider;
    //private JLabel sumLabel;
    //private static final double maxConv = 0.2;
    //private static final double minNorm = -25;
    //private static final double maxNorm = 25;
    //private static final double minSum = -80;
    //private static final double maxSum = -30;
    private static final double FWHM_SCALE = 2*Math.sqrt(2*Math.log(2));

    /* XXX: according to Java documentation, this should do nothing if set to true.
     * However, it seems to work */
    private static boolean REAL_TIME = true;

    /* XXX: this is a part of a particularly ugly hack */
    /* If lock is set, we must ignore all change events */
    private boolean lock = false;
    private XRRApp xrr;

    /** Constructor.
     *
     * <p>
     *
     * Creates a slider-based interface for fine-tuning a layer model.
     *
     * @param ls the layer stack to create the slider-based user interface for
     * @param sliderPanel a user interface component to create the slider interface in
     * 
     */
    public ScrollbarUpdater(XRRApp xrr, LayerStack ls, JComponent sliderPanel) {
        sliderPanel.setLayout(new BorderLayout(5,5));
        final ScrollbarUpdater thisUpdater = this;

        this.xrr = xrr;
        this.ls = ls;
        this.sliderPane = new JTabbedPane();

        JPanel global = new StackSliderPanel(xrr, ls);
        sliderPane.insertTab("Global",null,global,"Global settings",0);

        sliderPanel.add(sliderPane, BorderLayout.CENTER);
    }


    public void contentsChanged(ListDataEvent e) {
        if(e.getSource() == this)
            return; /* It is an event sent by this object, so we won't want to process it */
        /* XXX: ugly hack */
        intervalRemoved(e);
        intervalAdded(e);
    }
    public void intervalAdded(ListDataEvent e) {
        int i0 = e.getIndex0(), i1 = e.getIndex1() + 1;
        final ScrollbarUpdater thisUpdater = this;

        if(e.getSource() == thisUpdater)
            return; /* It is an event sent by this object, so we won't want to process it */

        for(int i=i0; i<i1; i++) {
            final Layer l = ls.getElementAt(i);
            JPanel sliders = new JPanel();
            //sliders.setLayout(new BoxLayout(sliders,BoxLayout.PAGE_AXIS));


            GridBagConstraints c = new GridBagConstraints();
            sliders.setLayout(new GridBagLayout());

            c.insets = new Insets(1,1,1,1);
            c.ipadx = c.ipady = 1;
            c.anchor = GridBagConstraints.WEST;

            /* thickness */
            final JSlider dSlider = new JSlider(0,1000);
            final FitValue d = l.getThickness();

            final JLabel dValLabel = new JLabel("d (nm) = 0.00000000e-1",SwingConstants.LEFT);
            dValLabel.setPreferredSize(new Dimension(dValLabel.getPreferredSize().width,dValLabel.getPreferredSize().height));
            dValLabel.setMinimumSize(dValLabel.getPreferredSize());
            dValLabel.setMaximumSize(dValLabel.getPreferredSize());
            dValLabel.setText("d (nm) = " + String.format(Locale.US,"%.6g",d.getExpected()*1e9));

            dSlider.setValue((int)(1000*(d.getExpected()-d.getMin())/(d.getMax()-d.getMin()) + 0.5));
            final ChangeListener dChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    /* If lock is set, we must ignore all change events */
                    if(!lock) {
                        FitValue d2 = l.getThickness(); /* we can't use d: it might be a reference to an old object */
                        double newval = dSlider.getValue()*(d2.getMax()-d2.getMin())/1000+d2.getMin();
                        dValLabel.setText("d (nm) = " + String.format(Locale.US,"%.6g",newval*1e9));
                        if(!dSlider.getValueIsAdjusting() || REAL_TIME) {
                            d2.setExpected(newval);
                            ls.invalidate(thisUpdater); /* it is critical to send this from thisUpdater */
                        }
                    }
                }
            };
            dSlider.addChangeListener(dChangeListener);
            d.addValueListener(new ValueListener() {
              public void valueChanged(ValueEvent ev) {
                lock = true;
                dSlider.setValue((int)(1000*(d.getExpected()-d.getMin())/(d.getMax()-d.getMin()) + 0.5));
                dValLabel.setText("d (nm) = " + String.format(Locale.US,"%.6g",d.getExpected()*1e9));
                lock = false;
              }
            });

            final JLabel dMinLabel = new JLabel(/*"min = " + */String.format(Locale.US,"%.6g",d.getMin()*1e9));
            final JLabel dMaxLabel = new JLabel(/*"max = " + */String.format(Locale.US,"%.6g",d.getMax()*1e9));


            JButton minx2Button = new JButton("2");
            minx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getThickness();
                    double newMin = val.getMin()-(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMin = val.getMin() - 1/1e9;
                    }
                    if (newMin < 0)
                    {
                        newMin = 0;
                    }
                    val.setValues(newMin,val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    dSlider.setValue(newValue);
                    lock = false;
                    dChangeListener.stateChanged(null);
                    dMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    dMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            minx2Button.setPreferredSize(new Dimension((int)(minx2Button.getMinimumSize().width),minx2Button.getMinimumSize().height));
            minx2Button.setMinimumSize(minx2Button.getPreferredSize());
            minx2Button.setMaximumSize(minx2Button.getPreferredSize());

	    /* This button changes the minimum value of the parameter range to the current value */
            JButton minButton = new JButton("<");
            minButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getThickness();
                    val.setValues(val.getExpected(),val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    dSlider.setValue(newValue);
                    lock = false;
                    dChangeListener.stateChanged(null);
                    dMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    dMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            minButton.setMinimumSize(minButton.getPreferredSize());

	    /* current label || min button || minimum value || slider || maximum label */
            c.gridwidth = 1;
            sliders.add(dValLabel,c);
            sliders.add(minx2Button,c);
            sliders.add(minButton,c);
            sliders.add(dMinLabel,c);
            c.fill = GridBagConstraints.HORIZONTAL;
            //c.gridwidth = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.weightx = 2;
            sliders.add(dSlider,c);
            c.fill = GridBagConstraints.NONE;
            //c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;
            sliders.add(dMaxLabel,c);

	    /* This button changes the maximum value of the parameter range to the current value */
            JButton maxButton = new JButton(">");
            maxButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getThickness();
                    val.setValues(val.getMin(),val.getExpected(),val.getExpected());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    dSlider.setValue(newValue);
                    lock = false;
                    dChangeListener.stateChanged(null);
                    dMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    dMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            maxButton.setMinimumSize(maxButton.getPreferredSize());
            sliders.add(maxButton,c);

            JButton maxx2Button = new JButton("2");
            maxx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getThickness();
                    double newMax = val.getMax()+(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMax = val.getMax() + 1/1e9;
                    }
                    val.setValues(val.getMin(),val.getExpected(),newMax);

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    dSlider.setValue(newValue);
                    lock = false;
                    dChangeListener.stateChanged(null);
                    dMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    dMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            maxx2Button.setPreferredSize(new Dimension((int)(maxx2Button.getMinimumSize().width),maxx2Button.getMinimumSize().height));
            maxx2Button.setMinimumSize(maxx2Button.getPreferredSize());
            maxx2Button.setMaximumSize(maxx2Button.getPreferredSize());
            sliders.add(maxx2Button,c);

            JButton errButton = new JButton("E");
            errButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LayerStack.Pair pair = ls.deepCopy(l.getThickness());
                    LayerStack ls = pair.stack;
                    FitValue val = pair.value;
                    double min = val.getMin(), max = val.getMax();
                    double[] mids = new double[1001];
                    double[] errs = new double[1001];
                    for (int i = 0; i <= 1000; i++)
                    {
                        double mid = min + (max-min)/1000.0 * i;
                        val.setExpected(mid);
                        GraphData gd2 = xrr.croppedGd().simulate(ls).normalize(ls);
                        double err = xrr.func().getError(gd2.meas, gd2.simul);
                        mids[i] = mid;
                        errs[i] = err;
                    }
                    ArrayList<NamedArray> yarrays = new ArrayList<NamedArray>();
                    yarrays.add(new NamedArray(1, errs, ""));
                    new ChartFrame(xrr,"Error scan", 600, 400, false,
                        new DataArray(1e9, mids), "d (nm)", yarrays, "error", 0, 0, null).setVisible(true);
                }
            });
            sliders.add(errButton,c);
            JButton rangeButton = new JButton("Edit");
            rangeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getThickness();
                    FitRangeDialog dialog = new FitRangeDialog((JFrame)null,"d","nm");
                    dialog.call(val,1e9);

                    /* We must send ONE change event every time, even if the physical position of the slider
                     * didn't change
                     * XXX: this is an ugly hack, Java really should offer an API to control change events */
                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    dSlider.setValue(newValue); /* this will send a change event, but it is ignored because of the lock */
                    lock = false;
                    dChangeListener.stateChanged(null);
                    dMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    dMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                    dialog.dispose();
                }
            });
            rangeButton.setMinimumSize(rangeButton.getPreferredSize());
            sliders.add(rangeButton,c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            final JCheckBox dEnableCheck = new JCheckBox("fit");
            dEnableCheck.setSelected(l.getThickness().getEnabled());
            dEnableCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    l.getThickness().setEnabled(dEnableCheck.isSelected());
                }
            });
            sliders.add(dEnableCheck,c);

            /* roughness */
            final JSlider rSlider = new JSlider(0,1000);
            final FitValue r = l.getRoughness();

            final JLabel rValLabel = new JLabel("r (nm) = 0.00000000e-1",SwingConstants.LEFT);
            rValLabel.setPreferredSize(new Dimension(rValLabel.getPreferredSize().width,rValLabel.getPreferredSize().height));
            rValLabel.setMinimumSize(rValLabel.getPreferredSize());
            rValLabel.setMaximumSize(rValLabel.getPreferredSize());
            rValLabel.setText("r (nm) = " + String.format(Locale.US,"%.6g",r.getExpected()*1e9));

            rSlider.setValue((int)(1000*(r.getExpected()-r.getMin())/(r.getMax()-r.getMin()) + 0.5));
            final ChangeListener rChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    /* If lock is set, we must ignore all change events */
                    if(!lock) {
                        FitValue r2 = l.getRoughness(); /* we can't use r: it might be a reference to an old object */
                        double newval = rSlider.getValue()*(r2.getMax()-r2.getMin())/1000+r2.getMin();
                        rValLabel.setText("r (nm) = " + String.format(Locale.US,"%.6g",newval*1e9));
                        if(!rSlider.getValueIsAdjusting() || REAL_TIME) {
                            r2.setExpected(newval);
                            ls.invalidate(thisUpdater); /* it is critical to send this from thisUpdater */
                        }
                    }
                }
            };
            rSlider.addChangeListener(rChangeListener);
            r.addValueListener(new ValueListener() {
              public void valueChanged(ValueEvent ev) {
                lock = true;
                rSlider.setValue((int)(1000*(r.getExpected()-r.getMin())/(r.getMax()-r.getMin()) + 0.5));
                rValLabel.setText("r (nm) = " + String.format(Locale.US,"%.6g",r.getExpected()*1e9));
                lock = false;
              }
            });

            final JLabel rMinLabel = new JLabel(/*"min = " + */String.format(Locale.US,"%.6g",r.getMin()*1e9));
            final JLabel rMaxLabel = new JLabel(/*"max = " + */String.format(Locale.US,"%.6g",r.getMax()*1e9));

            minx2Button = new JButton("2");
            minx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getRoughness();
                    double newMin = val.getMin()-(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMin = val.getMin() - 1/1e9;
                    }
                    if (newMin < 0)
                    {
                        newMin = 0;
                    }
                    val.setValues(newMin,val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rSlider.setValue(newValue);
                    lock = false;
                    rChangeListener.stateChanged(null);
                    rMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    rMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            minx2Button.setPreferredSize(new Dimension((int)(minx2Button.getMinimumSize().width),minx2Button.getMinimumSize().height));
            minx2Button.setMinimumSize(minx2Button.getPreferredSize());
            minx2Button.setMaximumSize(minx2Button.getPreferredSize());

	    /* This button changes the minimum value of the parameter range to the current value */
            minButton = new JButton("<");
            minButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getRoughness();
                    val.setValues(val.getExpected(),val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rSlider.setValue(newValue);
                    lock = false;
                    rChangeListener.stateChanged(null);
                    rMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    rMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            minButton.setMinimumSize(minButton.getPreferredSize());

            c.gridwidth = 1;
            sliders.add(rValLabel,c);
            sliders.add(minx2Button,c);
            sliders.add(minButton,c);
            sliders.add(rMinLabel,c);
            c.fill = GridBagConstraints.HORIZONTAL;
            //c.gridwidth = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.weightx = 2;
            sliders.add(rSlider,c);
            c.fill = GridBagConstraints.NONE;
            //c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;
            sliders.add(rMaxLabel,c);

            maxButton = new JButton(">");
            maxButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getRoughness();
                    val.setValues(val.getMin(),val.getExpected(),val.getExpected());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rSlider.setValue(newValue);
                    lock = false;
                    rChangeListener.stateChanged(null);
                    rMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    rMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            maxButton.setMinimumSize(maxButton.getPreferredSize());
            sliders.add(maxButton,c);

            maxx2Button = new JButton("2");
            maxx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getRoughness();
                    double newMax = val.getMax()+(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMax = val.getMax() + 1/1e9;
                    }
                    val.setValues(val.getMin(),val.getExpected(),newMax);

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rSlider.setValue(newValue);
                    lock = false;
                    rChangeListener.stateChanged(null);
                    rMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    rMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                }
            });
            maxx2Button.setPreferredSize(new Dimension((int)(maxx2Button.getMinimumSize().width),maxx2Button.getMinimumSize().height));
            maxx2Button.setMinimumSize(maxx2Button.getPreferredSize());
            maxx2Button.setMaximumSize(maxx2Button.getPreferredSize());
            sliders.add(maxx2Button,c);

            errButton = new JButton("E");
            errButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LayerStack.Pair pair = ls.deepCopy(l.getRoughness());
                    LayerStack ls = pair.stack;
                    FitValue val = pair.value;
                    double min = val.getMin(), max = val.getMax();
                    double[] mids = new double[1001];
                    double[] errs = new double[1001];
                    for (int i = 0; i <= 1000; i++)
                    {
                        double mid = min + (max-min)/1000.0 * i;
                        val.setExpected(mid);
                        GraphData gd2 = xrr.croppedGd().simulate(ls).normalize(ls);
                        double err = xrr.func().getError(gd2.meas, gd2.simul);
                        mids[i] = mid;
                        errs[i] = err;
                    }
                    ArrayList<NamedArray> yarrays = new ArrayList<NamedArray>();
                    yarrays.add(new NamedArray(1, errs, ""));
                    new ChartFrame(xrr,"Error scan", 600, 400, false,
                        new DataArray(1e9, mids), "r (nm)", yarrays, "error", 0, 0, null).setVisible(true);
                }
            });
            sliders.add(errButton,c);
            rangeButton = new JButton("Edit");
            rangeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getRoughness();
                    FitRangeDialog dialog = new FitRangeDialog((JFrame)null,"r","nm");
                    dialog.call(val,1e9);

                    /* We must send ONE change event every time, even if the physical position of the slider
                     * didn't change
                     * XXX: this is an ugly hack, Java really should offer an API to control change events */
                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rSlider.setValue(newValue);
                    lock = false;
                    rChangeListener.stateChanged(null);
                    rMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()*1e9));
                    rMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()*1e9));
                    dialog.dispose();
                }
            });
            rangeButton.setMinimumSize(rangeButton.getPreferredSize());
            sliders.add(rangeButton,c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            final JCheckBox rEnableCheck = new JCheckBox("fit");
            rEnableCheck.setSelected(l.getRoughness().getEnabled());
            rEnableCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    l.getRoughness().setEnabled(rEnableCheck.isSelected());
                }
            });
            sliders.add(rEnableCheck,c);

            /* density */
            final JSlider rhoSlider = new JSlider(0,1000);
            final FitValue rho = l.getDensity();

            final JLabel rhoValLabel = new JLabel("rho (g/cm^3) = 0.00000000e-1",SwingConstants.LEFT);
            rhoValLabel.setPreferredSize(new Dimension(rhoValLabel.getPreferredSize().width,rhoValLabel.getPreferredSize().height));
            rhoValLabel.setMinimumSize(rhoValLabel.getPreferredSize());
            rhoValLabel.setMaximumSize(rhoValLabel.getPreferredSize());
            rhoValLabel.setText("rho (g/cm^3) = " + String.format(Locale.US,"%.6g",rho.getExpected()/1e3));

            rhoSlider.setValue((int)(1000*(rho.getExpected()-rho.getMin())/(rho.getMax()-rho.getMin()) + 0.5));
            final ChangeListener rhoChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    /* If lock is set, we must ignore all change events */
                    if(!lock) {
                        FitValue rho2 = l.getDensity(); /* we can't use rho: it might be a reference to an old object */
                        double newval = rhoSlider.getValue()*(rho2.getMax()-rho2.getMin())/1000+rho2.getMin();
                        rhoValLabel.setText("rho (g/cm^3) = "+String.format(Locale.US,"%.6g",newval/1e3));
                        if(!rhoSlider.getValueIsAdjusting() || REAL_TIME) {
                            rho2.setExpected(newval);
                            ls.invalidate(thisUpdater); /* it is critical to send this from thisUpdater */
                        }
                    }
                }
            };
            rhoSlider.addChangeListener(rhoChangeListener);
            rho.addValueListener(new ValueListener() {
              public void valueChanged(ValueEvent ev) {
                lock = true;
                rhoSlider.setValue((int)(1000*(rho.getExpected()-rho.getMin())/(rho.getMax()-rho.getMin()) + 0.5));
                rhoValLabel.setText("rho (g/cm^3) = " + String.format(Locale.US,"%.6g",rho.getExpected()/1e3));
                lock = false;
              }
            });

            final JLabel rhoMinLabel = new JLabel(/*"min = " + */String.format(Locale.US,"%.6g",rho.getMin()/1e3));
            final JLabel rhoMaxLabel = new JLabel(/*"max = " + */String.format(Locale.US,"%.6g",rho.getMax()/1e3));

            minx2Button = new JButton("2");
            minx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getDensity();
                    double newMin = val.getMin()-(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMin = val.getMin() - 1e3;
                    }
                    if (newMin < 0)
                    {
                        newMin = 0;
                    }
                    val.setValues(newMin,val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rhoSlider.setValue(newValue);
                    lock = false;
                    rhoChangeListener.stateChanged(null);
                    rhoMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()/1e3));
                    rhoMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()/1e3));
                }
            });
            minx2Button.setPreferredSize(new Dimension((int)(minx2Button.getMinimumSize().width),minx2Button.getMinimumSize().height));
            minx2Button.setMinimumSize(minx2Button.getPreferredSize());
            minx2Button.setMaximumSize(minx2Button.getPreferredSize());

	    /* This button changes the minimum value of the parameter range to the current value */
            minButton = new JButton("<");
            minButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getDensity();
                    val.setValues(val.getExpected(),val.getExpected(),val.getMax());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rhoSlider.setValue(newValue);
                    lock = false;
                    rhoChangeListener.stateChanged(null);
                    rhoMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()/1e3));
                    rhoMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()/1e3));
                }
            });
            minButton.setMinimumSize(minButton.getPreferredSize());

            c.gridwidth = 1;
            sliders.add(rhoValLabel,c);
            sliders.add(minx2Button,c);
            sliders.add(minButton,c);
            sliders.add(rhoMinLabel,c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 2;
            //c.gridwidth = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            sliders.add(rhoSlider,c);
            c.fill = GridBagConstraints.NONE;
            //c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;
            sliders.add(rhoMaxLabel,c);

            maxButton = new JButton(">");
            maxButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getDensity();
                    val.setValues(val.getMin(),val.getExpected(),val.getExpected());

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rhoSlider.setValue(newValue);
                    lock = false;
                    rhoChangeListener.stateChanged(null);
                    rhoMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()/1e3));
                    rhoMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()/1e3));
                }
            });
            maxButton.setMinimumSize(maxButton.getPreferredSize());
            sliders.add(maxButton,c);

            maxx2Button = new JButton("2");
            maxx2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getDensity();
                    double newMax = val.getMax()+(val.getMax()-val.getMin());
                    if (val.getMax() == val.getMin())
                    {
                        newMax = val.getMax() + 1e3;
                    }
                    val.setValues(val.getMin(),val.getExpected(),newMax);

                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rhoSlider.setValue(newValue);
                    lock = false;
                    rhoChangeListener.stateChanged(null);
                    rhoMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()/1e3));
                    rhoMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()/1e3));
                }
            });
            maxx2Button.setPreferredSize(new Dimension((int)(maxx2Button.getMinimumSize().width),maxx2Button.getMinimumSize().height));
            maxx2Button.setMinimumSize(maxx2Button.getPreferredSize());
            maxx2Button.setMaximumSize(maxx2Button.getPreferredSize());
            sliders.add(maxx2Button,c);

            errButton = new JButton("E");
            errButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LayerStack.Pair pair = ls.deepCopy(l.getDensity());
                    LayerStack ls = pair.stack;
                    FitValue val = pair.value;
                    double min = val.getMin(), max = val.getMax();
                    double[] mids = new double[1001];
                    double[] errs = new double[1001];
                    for (int i = 0; i <= 1000; i++)
                    {
                        double mid = min + (max-min)/1000.0 * i;
                        val.setExpected(mid);
                        GraphData gd2 = xrr.croppedGd().simulate(ls).normalize(ls);
                        double err = xrr.func().getError(gd2.meas, gd2.simul);
                        mids[i] = mid;
                        errs[i] = err;
                    }
                    ArrayList<NamedArray> yarrays = new ArrayList<NamedArray>();
                    yarrays.add(new NamedArray(1, errs, ""));
                    new ChartFrame(xrr,"Error scan", 600, 400, false,
                        new DataArray(1e-3, mids), "rho (g/cm^3)", yarrays, "error", 0, 0, null).setVisible(true);
                }
            });
            sliders.add(errButton,c);
            rangeButton = new JButton("Edit");
            rangeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FitValue val = l.getDensity();
                    FitRangeDialog dialog = new FitRangeDialog((JFrame)null,"rho","g/cm^3");
                    dialog.call(val,1e-3);

                    /* We must send ONE change event every time, even if the physical position of the slider
                     * didn't change
                     * XXX: this is an ugly hack, Java really should offer an API to control change events */
                    int newValue = (int)(((val.getExpected()-val.getMin())/(val.getMax()-val.getMin()))*1000+0.5);
                    lock = true;
                    rhoSlider.setValue(newValue);
                    lock = false;
                    rhoChangeListener.stateChanged(null);
                    rhoMinLabel.setText(/*"min = " + */String.format(Locale.US,"%.6g",val.getMin()/1e3));
                    rhoMaxLabel.setText(/*"max = " + */String.format(Locale.US,"%.6g",val.getMax()/1e3));
                    dialog.dispose();
                }
            });
            rangeButton.setMinimumSize(rangeButton.getPreferredSize());
            sliders.add(rangeButton,c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            final JCheckBox rhoEnableCheck = new JCheckBox("fit");
            rhoEnableCheck.setSelected(l.getDensity().getEnabled());
            rhoEnableCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    l.getDensity().setEnabled(rhoEnableCheck.isSelected());
                }
            });
            sliders.add(rhoEnableCheck,c);

            /* composition */
            final JSlider fSlider = new JSlider(0,1000);

            final JLabel fValLabel = new JLabel("f = 0.00000000",SwingConstants.LEFT);
            fValLabel.setPreferredSize(new Dimension(fValLabel.getPreferredSize().width,fValLabel.getPreferredSize().height));
            fValLabel.setMinimumSize(fValLabel.getPreferredSize());
            fValLabel.setMaximumSize(fValLabel.getPreferredSize());
            fValLabel.setText("f = " + String.format(Locale.US,"%.6g",l.getF()));

            fSlider.setValue((int)(1000*l.getF() + 0.5));
            fSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    /* If lock is set, we must ignore all change events */
                    if(!lock) {
                        double newval = fSlider.getValue()/1000.0;
                        fValLabel.setText("f = " + String.format(Locale.US,"%.6g",newval));
                        if(!fSlider.getValueIsAdjusting() || REAL_TIME) {
                            l.setF(newval);
                            ls.invalidate(thisUpdater); /* it is critical to send this from thisUpdater */
                        }
                    }
                }
            });
            c.gridwidth = 1;
            sliders.add(fValLabel,c);
            c.gridwidth = 3;
            sliders.add(new JLabel(l.getCompound1().toString()),c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 2;
            c.weightx = 2;
            //c.gridwidth = GridBagConstraints.RELATIVE;
            sliders.add(fSlider,c);
            c.fill = GridBagConstraints.NONE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;
            //c.gridwidth = 1;
            sliders.add(new JLabel(l.getCompound2().toString()),c);

            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weighty = 1;
            sliders.add(new JPanel(), c);

            sliderPane.insertTab(l.getName(),null,sliders,l.getName()/*tip*/,i);
        }
    }
    public void intervalRemoved(ListDataEvent e) {
        int i0 = e.getIndex0(), i1 = e.getIndex1() + 1;
        if(e.getSource() == this)
            return; /* It is an event sent by this object, so we won't want to process it */
        if(i1 > sliderPane.getTabCount())
            i1 = sliderPane.getTabCount();
        if(i0 < 0)
          i0 = 0;
        for(int i=i1-1; i>=i0; i--)
            sliderPane.removeTabAt(i);
    }
};
