import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for layer settings */
public class InfoDialog extends JDialog {
    private JTextField wlField, deltaPerRhoField, betaPerRhoField,
                       betaPerDeltaField, rhoEPerRhoField, rhoField,
                       rhoEField, deltaField, betaField, critField,
                       absorptionField, sldField;

    public InfoDialog(JFrame f)
    {
        super(f,"Optical properties",true);
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;



        c.gridwidth = 1;
        gridPanel.add(new JLabel("wavelength (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        wlField = new JTextField("",12);
        wlField.setEditable(false);
        wlField.setMinimumSize(wlField.getPreferredSize());
        gridPanel.add(wlField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("rho_e/rho (1/kg)"),c);
        rhoEPerRhoField = new JTextField("",12);
        rhoEPerRhoField.setEditable(false);
        rhoEPerRhoField.setMinimumSize(rhoEPerRhoField.getPreferredSize());
        gridPanel.add(rhoEPerRhoField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("delta/rho (m^3/kg)"),c);
        deltaPerRhoField = new JTextField("",12);
        deltaPerRhoField.setEditable(false);
        deltaPerRhoField.setMinimumSize(deltaPerRhoField.getPreferredSize());
        gridPanel.add(deltaPerRhoField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("beta/rho (m^3/kg)"),c);
        betaPerRhoField = new JTextField("",12);
        betaPerRhoField.setEditable(false);
        betaPerRhoField.setMinimumSize(betaPerRhoField.getPreferredSize());
        gridPanel.add(betaPerRhoField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("beta/delta"),c);
        betaPerDeltaField = new JTextField("",12);
        betaPerDeltaField.setEditable(false);
        betaPerDeltaField.setMinimumSize(betaPerDeltaField.getPreferredSize());
        gridPanel.add(betaPerDeltaField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("rho (kg/m^3)"),c);
        rhoField = new JTextField("",12);
        rhoField.setEditable(false);
        rhoField.setMinimumSize(rhoField.getPreferredSize());
        gridPanel.add(rhoField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("rho_e (1/m^3)"),c);
        rhoEField = new JTextField("",12);
        rhoEField.setEditable(false);
        rhoEField.setMinimumSize(rhoEField.getPreferredSize());
        gridPanel.add(rhoEField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("delta"),c);
        deltaField = new JTextField("",12);
        deltaField.setEditable(false);
        deltaField.setMinimumSize(deltaField.getPreferredSize());
        gridPanel.add(deltaField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("beta"),c);
        betaField = new JTextField("",12);
        betaField.setEditable(false);
        betaField.setMinimumSize(betaField.getPreferredSize());
        gridPanel.add(betaField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("theta_crit (degrees)"),c);
        critField = new JTextField("",12);
        critField.setEditable(false);
        critField.setMinimumSize(critField.getPreferredSize());
        gridPanel.add(critField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("SLD (1/m^2)"),c);
        sldField = new JTextField("",12);
        sldField.setEditable(false);
        sldField.setMinimumSize(sldField.getPreferredSize());
        gridPanel.add(sldField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("absorption coeff (1/m)"),c);
        absorptionField = new JTextField("",12);
        absorptionField.setEditable(false);
        absorptionField.setMinimumSize(absorptionField.getPreferredSize());
        gridPanel.add(absorptionField,c);
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
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public void call(Layer l) {
        /*
        rhomin.setText(String.format(Locale.US,"%.6g",l.getDensity().getMin()/1e3));
        rhoval.setText(String.format(Locale.US,"%.6g",l.getDensity().getExpected()/1e3));
        rhomax.setText(String.format(Locale.US,"%.6g",l.getDensity().getMax()/1e3));
        dmin.setText(String.format(Locale.US,"%.6g",l.getThickness().getMin()*1e9));
        dval.setText(String.format(Locale.US,"%.6g",l.getThickness().getExpected()*1e9));
        dmax.setText(String.format(Locale.US,"%.6g",l.getThickness().getMax()*1e9));
        rmin.setText(String.format(Locale.US,"%.6g",l.getRoughness().getMin()*1e9));
        rval.setText(String.format(Locale.US,"%.6g",l.getRoughness().getExpected()*1e9));
        rmax.setText(String.format(Locale.US,"%.6g",l.getRoughness().getMax()*1e9));
        c1.setText(""+l.getCompound1());
        c2.setText(""+l.getCompound2());
        fract.setText(String.format(Locale.US,"%.6g",l.getF()));
        nameField.setText(l.getName());
        */
        double lambda = l.getXRRCompound().getLambda();
        wlField.setText(String.format(Locale.US,"%.6g",lambda));
        deltaPerRhoField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getDeltaPerRho()));
        betaPerRhoField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getBetaPerRho()));
        betaPerDeltaField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getBetaPerDelta()));
        rhoEPerRhoField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getRhoEPerRho()));
        rhoField.setText(String.format(Locale.US,"%.6g",l.getDensity().getExpected()));
        rhoEField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getRhoEPerRho()*l.getDensity().getExpected()));
        deltaField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getDeltaPerRho()*l.getDensity().getExpected()));
        betaField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getBetaPerRho()*l.getDensity().getExpected()));
        critField.setText(String.format(Locale.US,"%.6g",Math.acos(1-l.getXRRCompound().getDeltaPerRho()*l.getDensity().getExpected())*180/Math.PI));
        sldField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getDeltaPerRho()*l.getDensity().getExpected()*2*Math.PI/lambda/lambda));
        absorptionField.setText(String.format(Locale.US,"%.6g",l.getXRRCompound().getBetaPerRho()*l.getDensity().getExpected()*4*Math.PI/lambda));
        setVisible(true);
    }
}
