import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for layer settings */
public class LayerDialog extends JDialog {
    private boolean succesful;
    private JTextField rhomin, rhoval, rhomax, dmin, dval, dmax,
            rmin, rval, rmax, betaField, deltaField, nameField,
            c1, c2, fract;
    private JCheckBox rhofit, dfit, rfit; /* fit enable checkboxes */
    private Layer layer;
    public LayerDialog(JFrame f)
    {
        super(f,"Layer properties",true);
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("name"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        nameField = new JTextField("name",25);
        nameField.setMinimumSize(nameField.getPreferredSize());
        gridPanel.add(nameField,c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("density (g/cm^3)"),c);
        gridPanel.add(new JLabel("min"),c);
        rhomin = new JTextField("1",7);
        rhomin.setMinimumSize(rhomin.getPreferredSize());
        gridPanel.add(rhomin,c);
        gridPanel.add(new JLabel("value"),c);
        rhoval = new JTextField("2",7);
        rhoval.setMinimumSize(rhoval.getPreferredSize());
        gridPanel.add(rhoval,c);
        gridPanel.add(new JLabel("max"),c);
        rhomax = new JTextField("2",7);
        rhomax.setMinimumSize(rhomax.getPreferredSize());
        gridPanel.add(rhomax,c);
	rhofit = new JCheckBox("fit");
	rhofit.setSelected(true);
        gridPanel.add(rhofit,c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("thickness (nm)"),c);
        gridPanel.add(new JLabel("min"),c);
        dmin = new JTextField("1",7);
        dmin.setMinimumSize(dmin.getPreferredSize());
        gridPanel.add(dmin,c);
        gridPanel.add(new JLabel("value"),c);
        dval = new JTextField("2",7);
        dval.setMinimumSize(dval.getPreferredSize());
        gridPanel.add(dval,c);
        gridPanel.add(new JLabel("max"),c);
        dmax = new JTextField("2",7);
        dmax.setMinimumSize(dmax.getPreferredSize());
        gridPanel.add(dmax,c);
	dfit = new JCheckBox("fit");
	dfit.setSelected(true);
        gridPanel.add(dfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("roughness (nm)"),c);
        gridPanel.add(new JLabel("min"),c);
        rmin = new JTextField("1",7);
        rmin.setMinimumSize(dmin.getPreferredSize());
        gridPanel.add(rmin,c);
        gridPanel.add(new JLabel("value"),c);
        rval = new JTextField("2",7);
        rval.setMinimumSize(dval.getPreferredSize());
        gridPanel.add(rval,c);
        gridPanel.add(new JLabel("max"),c);
        rmax = new JTextField("2",7);
        rmax.setMinimumSize(dmax.getPreferredSize());
        gridPanel.add(rmax,c);
	rfit = new JCheckBox("fit");
	rfit.setSelected(true);
        gridPanel.add(rfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        c.gridwidth = 1;
        gridPanel.add(new JLabel("composition"),c);
        gridPanel.add(new JLabel("compound 1"),c);
        c1 = new JTextField("Al",7);
        c1.setMinimumSize(c1.getPreferredSize());
        gridPanel.add(c1,c);
        gridPanel.add(new JLabel("fraction"),c);
        fract = new JTextField("0.5",7);
        fract.setMinimumSize(fract.getPreferredSize());
        gridPanel.add(fract,c);
        gridPanel.add(new JLabel("compound 2"),c);
        c2 = new JTextField("O",7);
        c2.setMinimumSize(c2.getPreferredSize());
        gridPanel.add(c2,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);




        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);

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
                FitValue rho, d, r;
                double f;
                try {
                    f = Double.parseDouble(fract.getText());
                    rho = new FitValue(Double.parseDouble(rhomin.getText())*1e3,
                                       Double.parseDouble(rhoval.getText())*1e3,
                                       Double.parseDouble(rhomax.getText())*1e3,
				       rhofit.isSelected());
                    d = new FitValue(Double.parseDouble(dmin.getText())/1e9,
                                     Double.parseDouble(dval.getText())/1e9,
                                     Double.parseDouble(dmax.getText())/1e9,
			             dfit.isSelected());
                    r = new FitValue(Double.parseDouble(rmin.getText())/1e9,
                                     Double.parseDouble(rval.getText())/1e9,
                                     Double.parseDouble(rmax.getText())/1e9,
				     rfit.isSelected());
                    /* This must be first, otherwise cancel can return invalid data */
                    layer.setCompounds(new ChemicalFormula(c1.getText()),new ChemicalFormula(c2.getText()));
                    layer.setName(nameField.getText());
                    layer.setThickness(d);
                    layer.setDensity(rho);
                    layer.setRoughness(r);
                    layer.setF(f);
                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(ElementNotFound e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid boundary values", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(ChemicalFormulaException e) {
                    JOptionPane.showMessageDialog(null, "Invalid chemical formula", "Error", JOptionPane.ERROR_MESSAGE);
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
    public boolean call(Layer l) {
        this.succesful = false;
        this.layer = l;
        rhomin.setText(String.format(Locale.US,"%.6g",l.getDensity().getMin()/1e3));
        rhoval.setText(String.format(Locale.US,"%.6g",l.getDensity().getExpected()/1e3));
        rhomax.setText(String.format(Locale.US,"%.6g",l.getDensity().getMax()/1e3));
	rhofit.setSelected(l.getDensity().getEnabled());
        dmin.setText(String.format(Locale.US,"%.6g",l.getThickness().getMin()*1e9));
        dval.setText(String.format(Locale.US,"%.6g",l.getThickness().getExpected()*1e9));
        dmax.setText(String.format(Locale.US,"%.6g",l.getThickness().getMax()*1e9));
	dfit.setSelected(l.getThickness().getEnabled());
        rmin.setText(String.format(Locale.US,"%.6g",l.getRoughness().getMin()*1e9));
        rval.setText(String.format(Locale.US,"%.6g",l.getRoughness().getExpected()*1e9));
        rmax.setText(String.format(Locale.US,"%.6g",l.getRoughness().getMax()*1e9));
	rfit.setSelected(l.getRoughness().getEnabled());
        c1.setText(""+l.getCompound1());
        c2.setText(""+l.getCompound2());
        fract.setText(String.format(Locale.US,"%.6g",l.getF()));
        nameField.setText(l.getName());
        setVisible(true);
        return succesful;
    }
}
