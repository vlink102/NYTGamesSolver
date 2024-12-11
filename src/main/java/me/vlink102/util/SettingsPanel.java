package me.vlink102;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final Setting[] settings;

    public Setting[] getSettings() {
        return settings;
    }

    public SettingsPanel(Setting... settings) {
        super(new GridBagLayout());
        this.settings = settings;
        GridBagConstraints constraints = new GridBagConstraints();
        for (int i = 0; i < settings.length; i++) {
            Setting setting = settings[i];
            constraints.gridx = 0;
            constraints.weightx = 1;
            constraints.weighty = 0;
            constraints.gridy = i;
            constraints.anchor = GridBagConstraints.WEST;
            this.add(new JLabel(setting.label), constraints);
            constraints.gridx = 1;
            this.add(setting.component, constraints);
        }
    }

    public record Setting(String label, JComponent component) {
        public Object getValue() {
            if (component instanceof JSpinner spinner) {
                return spinner.getValue();
            }
            if (component instanceof JSlider slider) {
                return slider.getValue();
            }
            if (component instanceof JComboBox<?> comboBox) {
                return comboBox.getSelectedItem();
            }
            return null;
        }

        public static Setting spinner(String label, int value, int min, int max, int step) {
            return new Setting(label, new JSpinner(new SpinnerNumberModel(value, min, max, step)));
        }

        public static <E> Setting dropDown(String label, Class<E> enumClass) {
            return new Setting(label, new JComboBox<>(enumClass.getEnumConstants()));
        }

        public static Setting slider(String label, int value, int min, int max) {
            return new Setting(label, new JSlider(min, max, value));
        }

        public Setting(String label, JComponent component) {
            this.label = label;
            this.component = component;

        }
    }
}
