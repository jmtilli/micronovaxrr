import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





public class LinkDialog extends JDialog {
    /* throws IllegalArgumentException */
    private boolean ok;
    private final JCheckBox dBox = new JCheckBox("Thickness");
    private final JCheckBox rhoBox = new JCheckBox("Density");
    private final JCheckBox rBox = new JCheckBox("Roughness");

    public boolean ok()
    {
        return ok;
    }
    public boolean d()
    {
        return dBox.isSelected();
    }
    public boolean rho()
    {
        return rhoBox.isSelected();
    }
    public boolean r()
    {
        return rBox.isSelected();
    }

    public LinkDialog(Dialog d, String title)
    {
        super(d,title,true);
        init();
    }
    public LinkDialog(Frame f, String title)
    {
        super(f,title,true);
        init();
    }
    private void init() {
        Container dialog;
        JPanel checkboxPanel = new JPanel();

        dialog = getContentPane();

        dialog.setLayout(new BorderLayout());

        checkboxPanel.setLayout(new GridLayout(3, 1));
        checkboxPanel.add(dBox);
        checkboxPanel.add(rhoBox);
        checkboxPanel.add(rBox);
        dialog.add(checkboxPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btn;
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                ok = true;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                ok = false;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        /*
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        */
        dialog.add(btnPanel, BorderLayout.SOUTH);
        pack();
    }
    public void call() {
        setVisible(true);
    }
}
