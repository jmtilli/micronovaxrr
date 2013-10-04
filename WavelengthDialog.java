import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for changing wavelength */
public class WavelengthDialog extends JDialog {
    private boolean succesful;
    private double lambda;
    private JTextField field;
    public WavelengthDialog(JFrame f)
    {
        super(f,"Wavelength",true);

        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("wavelength"),c);
        field = new JTextField("2",15);
        field.setMinimumSize(field.getPreferredSize());
        gridPanel.add(field,c);
        gridPanel.add(new JLabel("nm"),c);
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
                    lambda = Double.parseDouble(field.getText())/1e9;
                    //if(lambda < 0.01e-9 || lambda > 100e-9)
                    if(lambda <= 0)
                        throw new IllegalArgumentException();
                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid wavelength", "Error", JOptionPane.ERROR_MESSAGE);
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
    public Double call(double lambda) {
        this.succesful = false;
        field.setText(String.format(Locale.US,"%.6f",lambda*1e9));
        setVisible(true);
        if(succesful)
            return this.lambda;
        else
            return null;
    }
}
