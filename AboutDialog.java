import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





public class AboutDialog extends JDialog {

    private JTextArea t;

    private final String str =

"Micronova XRR fitting software, version 1.9\n"+
"\n"+
"Copyright 2006-2014 Aalto University\n"+
"Copyright 2006-2017 Juha-Matti Tilli\n"+
"\n"+
"Authors:\n"+
"  Juha-Matti Tilli <juha-matti.tilli@iki.fi>\n"+
"\n"+
"Permission is hereby granted, free of charge, to any person obtaining a copy of\n"+
"this software and associated documentation files (the \"Software\"), to deal in \n"+
"the Software without restriction, including without limitation the rights to\n"+
"use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies\n"+
"of the Software, and to permit persons to whom the Software is furnished to do\n"+
"so, subject to the following conditions:\n"+
"\n"+
"The above copyright notice and this permission notice shall be included in all \n"+
"copies or substantial portions of the Software.\n"+
"\n"+
"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR \n"+
"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"+
"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n"+
"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"+
"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n"+
"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n"+
"SOFTWARE.\n"+
"\n"+
"It is requested (but not required) that the following article is cited by\n"+
"all scientific papers that make use of this software:\n"+
"\n"+
"     Tiilikainen J, Tilli J-M, Bosund V, Mattila M, Hakkarainen T,\n"+
"     Airaksinen V-M and Lipsanen H 2007 J. Phys D: Appl. Phys. 40 215-8\n"+
"\n"+
"This program uses JFreeChart, Copyright 2000-2006, by Object Refinery\n"+
"Limited and Contributors. It is distributed under the GNU Lesser General\n"+
"Public License: http://www.gnu.org/licenses/lgpl.html\n";

    public AboutDialog(JFrame f)
    {
        super(f,"About XRR",true);
        Container dialog;

        dialog = getContentPane();

        GridBagConstraints c = new GridBagConstraints();
        dialog.setLayout(new GridBagLayout());
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1;
        c.ipadx = c.ipady = 2;
        c.insets = new Insets(2, 2, 2, 2);

        t = new JTextArea();
        t.setEditable(false);
        t.setOpaque(false);
        t.setBorder(null);
        t.setText(str);
        t.setFont(new Font("monospaced", Font.PLAIN, 12));
        /*
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        */
        t.setRows(14);
        t.setMinimumSize(t.getPreferredSize());
        dialog.add(new JScrollPane(t),c);
        t.setCaretPosition(0);


        /*
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE,p.getPreferredSize().height));
        dialog.add(p,c);
        */

        JPanel btnPanel = new JPanel();
        JButton btn;
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        dialog.setPreferredSize(dialog.getLayout().preferredLayoutSize(dialog));
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        btnPanel.setMinimumSize(btnPanel.getPreferredSize());

        c.weighty = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        dialog.add(btn,c);
        pack();
    }

    public void call() {
        setVisible(true);
    }
}
