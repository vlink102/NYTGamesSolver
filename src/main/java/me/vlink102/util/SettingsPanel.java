package me.vlink102.util;

import lombok.Getter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

        public static JSpinner getCustomSpinner(int min, int max, int step, int defaultValue, int... excludedInts) {
            // Create the SpinnerModel with default behavior
            SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, min, max, step);

            // Create the JSpinner with the custom model
            JSpinner spinner = new JSpinner(model);

            // Customize the editor to allow restricted input
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
            spinner.setEditor(editor);

            // Add a ChangeListener to handle value changes
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int value = (int) spinner.getValue();
                    if (isExcluded(value, excludedInts)) {
                        // Adjust value if it's excluded
                        value = getNextValidValue(value, (Integer) model.getValue(),(int) model.getStepSize(), excludedInts);
                        spinner.setValue(value); // Update spinner value
                    }
                }
            });

            // Modify the model to skip excluded numbers during increment or decrement
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int currentValue = (int) spinner.getValue();
                    int nextValue = (int) model.getValue();

                    if (isExcluded(nextValue, excludedInts)) {
                        // Skip to next valid number if the value is excluded
                        nextValue = getNextValidValue(nextValue, currentValue,(int) model.getStepSize(), excludedInts);
                        spinner.setValue(nextValue);
                    }
                }
            });

            return spinner;
        }

        // Helper method to check if a value is excluded
        private static boolean isExcluded(int value, int... excludedInts) {
            for (int excluded : excludedInts) {
                if (value == excluded) {
                    return true;
                }
            }
            return false;
        }

        // Helper method to get the next valid value (skip excluded values)
        private static int getNextValidValue(int currentValue, int startValue, int step, int... excludedInts) {
            int direction = currentValue > startValue ? 1 : -1; // Determine direction (up or down)
            int nextValue = currentValue + direction;

            // Loop until we find a non-excluded value
            while (isExcluded(nextValue, excludedInts)) {
                nextValue += direction;
            }

            return nextValue;
        }

        @Getter
        public static class JSpinnerExcluder extends JSpinner {
            private final int[] excluded;

            public JSpinnerExcluder(SpinnerNumberModel model, int... excluded) {
                super(model);
                this.excluded = excluded;
            }
        }

        public static Setting spinner(String label, int value, int min, int max, int step, int... excluded) {
            return new Setting(label, new JSpinnerExcluder(new SpinnerNumberModel(value, min, max, step), excluded));
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
