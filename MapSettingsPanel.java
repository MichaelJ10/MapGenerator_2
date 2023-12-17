import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class MapSettingsPanel extends JPanel {
    final static DecimalFormat DF = new DecimalFormat("0.###");
    JButton newMap;
    JButton loadMap;
    NumberSetting seed;
    NumberSetting resolution;
    JCheckBox shadow;
    JPanel renderSteps;
    JTextField mapName;
    JButton save;

    public MapSettingsPanel(Map map) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        add(newMap = new JButton("New Map"));
        add(loadMap = new JButton("Load Map"));
        add(seed = new NumberSetting(20, "Seed"));
        add(resolution = new NumberSetting(20, "Resolution"));
        add(shadow = new JCheckBox("Shadow"));
        add(mapName = new JTextField(20, "Name"));
        add(save = new JButton("Save"));
        add(Box.createVerticalGlue());
        Component[] components = getComponents();
        JComponent[] jComponents = new JComponent[components.length];

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JComponent) {
                jComponents[i] = (JComponent) components[i];
            }
        }

        for (JComponent jComponent : jComponents) {
            // jComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (jComponent instanceof javax.swing.JTextField)
                jComponent.setMaximumSize(new Dimension(100000, 25));
        }

        resolution.setDouble(map.getResolution());
        resolution.addOnchangeListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map.setResolution((float) resolution.getDouble());
            }
        });

        seed.setLong(MapChunk.getSeed());
        seed.addOnchangeListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MapChunk.setSeed(seed.getLong());
                map.reRenderAll();
            }
        });

        shadow.setSelected(true);
        shadow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map.toggleFullRender();
            }
        });
    }

    public class NumberSetting extends JNumberField {
        String label;
        String str = "";

        public NumberSetting(int columns, String label) {
            super(columns);
            this.label = label;
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    str = getText().replace(label + ": ", "");
                }

                public void keyReleased(KeyEvent e) {
                    int pos = getCaretPosition();
                    int startLength = getText().length();
                    setText(label + ": " + getText());
                    pos += getText().length() - startLength;
                    setCaretPosition(Math.max(Math.min(pos, getText().length()), 0));
                    if (pos < label.length() + 2)
                        setText(label + ": " + str);
                }
            });
            getKeyListeners()[1].keyReleased(new KeyEvent(NumberSetting.this, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, (char) KeyEvent.VK_RIGHT));
        }

        public void addOnchangeListener(ActionListener event) {
            addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    event.actionPerformed(new ActionEvent(NumberSetting.this, ActionEvent.ACTION_PERFORMED, "onchange"));
                }
            });
        }

        public double getDouble() {
            String str = "";
            for (char chr : getText().toCharArray())
                if (Character.isDigit(chr) || chr == '.')
                    str += chr;
            return (str.equals("") ? 0 : Double.parseDouble(str));
        }

        public long getLong() {
            String str = "";
            for (char chr : getText().toCharArray())
                if (Character.isDigit(chr) || chr == '.')
                    str += chr;
            return Long.parseLong(str);
        }

        public void setDouble(double number) {
            setText(label + ": " + DF.format(number));
        }

        public void setLong(long number) {
            setText(label + ": " + number);
        }
    }

    public class JTextField extends javax.swing.JTextField {
        String str = "";
        String label;

        public JTextField(int columns, String label) {
            super(columns);
            this.label = label;
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    str = JTextField.super.getText().replace(label + ": ", "");
                }

                public void keyReleased(KeyEvent e) {
                    int pos = getCaretPosition();
                    if (pos < label.length() + 2)
                        setText(label + ": " + str);
                }
            });
            getKeyListeners()[0].keyReleased(new KeyEvent(JTextField.this, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, (char) KeyEvent.VK_RIGHT));
        }

        @Override
        public String getText() {
            return super.getText().replace(label + ": ", "");
        }
    }

    public class JNumberField extends javax.swing.JTextField {
        public JNumberField(int columns) {
            super(columns);
            addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    int pos = getCaretPosition();
                    int startLength = getText().length();
                    String str = "";
                    for (char chr : getText().toCharArray())
                        if (Character.isDigit(chr) || chr == '.')
                            str += chr;
                    setText(str);
                    pos -= startLength - str.length();
                    setCaretPosition(Math.max(Math.min(pos, str.length()), 0));
                }
            });
        }
    }
}
