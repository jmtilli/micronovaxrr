import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/* A dialog for layer settings */
public class SplitRoughnessDialog extends JDialog {
    private boolean succesful;
    private JTextField stddevsField, nField;
    private JCheckBox roughBox;
    private double stddevs;
    private int n;
    private boolean includeRoughness;
    public SplitRoughnessDialog(JFrame f)
    {
        super(f,"Split roughness",true);

        final SplitRoughnessDialog thisDialog = this;

        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("number of layers"),c);
        nField = new JTextField("6",7);
        nField.setMinimumSize(nField.getPreferredSize());
        gridPanel.add(nField,c);
        gridPanel.add(new JLabel("standard deviations"),c);
        stddevsField = new JTextField("2",7);
        stddevsField.setMinimumSize(stddevsField.getPreferredSize());
        gridPanel.add(stddevsField,c);
        roughBox = new JCheckBox("include approximate roughness");
        roughBox.setSelected(true);
        gridPanel.add(roughBox,c);
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
                    int n;
                    double stddevs;
                    stddevs = Double.parseDouble(stddevsField.getText());
                    n = Integer.parseInt(nField.getText());

                    if(n <= 0 || stddevs <= 0)
                        throw new IllegalArgumentException();

                    thisDialog.n = n;
                    thisDialog.stddevs = stddevs;
                    thisDialog.includeRoughness = roughBox.isSelected();

                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Both numbers must be positive", "Error", JOptionPane.ERROR_MESSAGE);
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
    public SplitRoughnessOpts call() {
        this.succesful = false;
        setVisible(true);
        if(!succesful)
            return null;
        return new SplitRoughnessOpts(n, stddevs, includeRoughness);
    }
}
