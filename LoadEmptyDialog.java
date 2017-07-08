import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class LoadEmptyDialog extends DataDialog {
    public LoadEmptyDialog(JFrame f) {
        super(f, "Empty measurement options", "angle", null, 700, 0, 3, 0, 90);
    }
}
