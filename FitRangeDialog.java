import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for layer settings */
public class FitRangeDialog extends JDialog {
    private boolean succesful;
    private JTextField minField, valField, maxField;
    private FitValue v;
    private double scale;
    private void initialize(String propertyName, String unitName)
    {
        String propertyDesc = propertyName;
        if(unitName != null) {
            propertyDesc += " ("+unitName+")";
        }

        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel(propertyDesc),c);
        gridPanel.add(new JLabel("min"),c);
        minField = new JTextField("1",7);
        minField.setMinimumSize(minField.getPreferredSize());
        gridPanel.add(minField,c);
        gridPanel.add(new JLabel("value"),c);
        valField = new JTextField("2",7);
        valField.setMinimumSize(valField.getPreferredSize());
        gridPanel.add(valField,c);
        gridPanel.add(new JLabel("max"),c);
        maxField = new JTextField("2",7);
        maxField.setMinimumSize(maxField.getPreferredSize());
        gridPanel.add(maxField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);


        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                double deltaPerRho, betaPerRho;
                try {
                    double min,val,max;
                    min = Double.parseDouble(minField.getText())/scale;
                    val = Double.parseDouble(valField.getText())/scale;
                    max = Double.parseDouble(maxField.getText())/scale;

                    if(min > val || val > max || min > max)
                        throw new IllegalArgumentException();

                    v.setValues(min,val,max,v.getEnabled());

                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid boundary values", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                succesful = false;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public FitRangeDialog(Frame f, String propertyName, String unitName)
    {
        super(f,"Layer "+propertyName,true);
        initialize(propertyName, unitName);
    }
    public FitRangeDialog(Dialog d, String propertyName, String unitName)
    {
        super(d,"Layer "+propertyName,true);
        initialize(propertyName, unitName);
    }
    public boolean call(FitValue v, double scale) {
        this.succesful = false;
        this.v = v;
        this.scale = scale;
        minField.setText(String.format(Locale.US,"%.6g",v.getMin()*scale));
        valField.setText(String.format(Locale.US,"%.6g",v.getExpected()*scale));
        maxField.setText(String.format(Locale.US,"%.6g",v.getMax()*scale));
        setVisible(true);
        return succesful;
    }
}
