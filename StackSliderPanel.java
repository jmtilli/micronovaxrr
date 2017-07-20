import java.awt.*;
import javax.swing.*;

public class StackSliderPanel extends JPanel {
    public StackSliderPanel(XRRApp xrr, LayerStack ls) {
        super();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());
        new SingleScrollbarUpdater(xrr, ls, ls.getSum(), "sum (dB)", 1, false).addToGridBag(this);
        new SingleScrollbarUpdater(xrr, ls, ls.getProd(), "norm (dB)", 1, false).addToGridBag(this);
        new SingleScrollbarUpdater(xrr, ls, ls.getBeam(), "beam", 1, true).addToGridBag(this);
        new SingleScrollbarUpdater(xrr, ls, ls.getStdDev(), "FWHM (\u00B0)", 180/Math.PI * (2*Math.sqrt(2*Math.log(2))), true).addToGridBag(this);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        add(new JPanel(), c);
    }
}
