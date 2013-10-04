import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


class DFTOptions {
    double minAngle, maxAngle;
    double minThickness, maxThickness;
    boolean useSimul;
    boolean useWindow;
    int multiplier;
    public DFTOptions(double minAngle, double maxAngle, double minThickness, double maxThickness, boolean useSimul, boolean useWindow, int multiplier) {
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.minThickness = minThickness;
        this.maxThickness = maxThickness;
        this.useSimul = useSimul;
        this.useWindow = useWindow;
        this.multiplier = multiplier;
    }
}

class DFTOptionDialog extends JDialog {
    private JTextField minAngleF, maxAngleF, minThicknessF, maxThicknessF, multiplierF;
    private JCheckBox useSimulB;
    private JCheckBox useWindowB;
    DFTOptions options;
    public DFTOptionDialog(JFrame f, double defaultMinAngle, double defaultMaxAngle, double defaultMinThickness, final double defaultMaxThickness)
    {
        super(f,"DFT options",true);
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        gridPanel.add(new JLabel("angle min (degrees)"),c);
        minAngleF = new JTextField(String.format(Locale.US,"%.4f",defaultMinAngle),7);
        minAngleF.setMinimumSize(minAngleF.getPreferredSize());
        gridPanel.add(minAngleF,c);
        gridPanel.add(new JLabel("angle max (degrees)"),c);
        maxAngleF = new JTextField(String.format(Locale.US,"%.4f",defaultMaxAngle),7); 
        maxAngleF.setMinimumSize(maxAngleF.getPreferredSize());
        gridPanel.add(maxAngleF,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("thickness min (nm)"),c);
        minThicknessF = new JTextField(String.format(Locale.US,"%.4f",defaultMinThickness),7);
        minThicknessF.setMinimumSize(minThicknessF.getPreferredSize());
        gridPanel.add(minThicknessF,c);
        gridPanel.add(new JLabel("thickness max (nm)"),c);
        maxThicknessF = new JTextField(String.format(Locale.US,"%.4f",defaultMaxThickness),7); 
        maxThicknessF.setMinimumSize(maxThicknessF.getPreferredSize());
        gridPanel.add(maxThicknessF,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        gridPanel.add(new JLabel("multiplier"));
        multiplierF = new JTextField(""+1,7);
        multiplierF.setMinimumSize(multiplierF.getPreferredSize());
        gridPanel.add(multiplierF);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);

        useSimulB = new JCheckBox("Use simulation instead of measurement");
        JPanel useSimulPanel = new JPanel();
        useSimulPanel.add(useSimulB);
        useSimulPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,useSimulPanel.getPreferredSize().height));
        dialog.add(useSimulPanel);

        useWindowB = new JCheckBox("Use Hann window function");
        JPanel useWindowPanel = new JPanel();
        useWindowPanel.add(useWindowB);
        useWindowPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,useWindowPanel.getPreferredSize().height));
        dialog.add(useWindowPanel);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE,p.getPreferredSize().height));
        dialog.add(p);

        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    double minAngle = Double.parseDouble(minAngleF.getText());
                    double maxAngle = Double.parseDouble(maxAngleF.getText());
                    double minThickness = Double.parseDouble(minThicknessF.getText());
                    double maxThickness = Double.parseDouble(maxThicknessF.getText());
                    int multiplier = Integer.parseInt(multiplierF.getText());
                    if(minAngle > maxAngle || minAngle < 0 || maxAngle > 90 || minThickness > maxThickness || minThickness < 0 || multiplier <= 0)
                        throw new IllegalArgumentException();
                    options = new DFTOptions(minAngle, maxAngle, minThickness, maxThickness, useSimulB.isSelected(), useWindowB.isSelected(), multiplier);
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid values", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                options = null;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public DFTOptions call() {
        this.options = null;
        setVisible(true);
        return this.options;
    }
}
