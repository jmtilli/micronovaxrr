import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class DepthProfileDialog extends DataDialog {
    public DepthProfileDialog(JFrame f, double defaultMin, double defaultMax) {
        super(f, "Depth profile options", "depth", "nm", 1000, defaultMin, defaultMax, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
