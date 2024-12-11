package me.vlink102;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import me.vlink102.games.LetterBoxed;
import me.vlink102.games.SpellingBee;
import me.vlink102.games.Sudoku;
import me.vlink102.games.Wordle;
import me.vlink102.util.ConsoleColors;
import me.vlink102.util.SettingsPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class NYT extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatMaterialLighterIJTheme.setup();
            new NYT().setVisible(true);
        });
    }

    public NYT() {
        super("New York Times Games (vlink102)");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(600, 400));
        this.setLocationRelativeTo(null);

        Container container = this.getContentPane();
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,4,3,3));

        cc("&b&lLoading NYT Games...&r");
        NYTGame sudoku = new NYTGame(fromResources("sudoku"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Sudoku();
            }
        });
        NYTGame wordle = new NYTGame(fromResources("wordle"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsPanel wordlePanel = new SettingsPanel(
                        SettingsPanel.Setting.spinner("Letters", 5, 3, 31, 1, 26, 30),
                        SettingsPanel.Setting.dropDown("Mode", Wordle.Mode.class)
                );
                int result = JOptionPane.showConfirmDialog(NYT.this, wordlePanel, "Wordle Settings", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    int letters = (int) wordlePanel.getSettings()[0].getValue();
                    if (Arrays.stream(((SettingsPanel.Setting.JSpinnerExcluder) wordlePanel.getSettings()[0].component()).getExcluded()).anyMatch(value -> value == letters)) {
                        JOptionPane.showMessageDialog(NYT.this, "There are no words with length " + letters);
                        return;
                    }
                    Wordle.Mode mode = (Wordle.Mode) wordlePanel.getSettings()[1].getValue();
                    Wordle.init(letters, mode);
                }
            }
        });
        NYTGame letterBox = new NYTGame(fromResources("boxed"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LetterBoxed();
            }
        });
        NYTGame spellingBee = new NYTGame(fromResources("bee"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SpellingBee();
            }
        });

        panel.add(sudoku);
        panel.add(wordle);
        panel.add(letterBox);
        panel.add(spellingBee);
        container.add(panel);

        this.pack();
    }

    public static void cc(String initial) {
        initial = initial.replaceAll("&r", ConsoleColors.RESET);
        initial = initial.replaceAll("&0&l", ConsoleColors.BLACK_BOLD);
        initial = initial.replaceAll("&c&l", ConsoleColors.RED_BOLD);
        initial = initial.replaceAll("&d&l", ConsoleColors.PURPLE_BOLD);
        initial = initial.replaceAll("&e&l", ConsoleColors.YELLOW_BOLD);
        initial = initial.replaceAll("&9&l", ConsoleColors.BLUE_BOLD);
        initial = initial.replaceAll("&a&l", ConsoleColors.GREEN_BOLD);
        initial = initial.replaceAll("&b&l", ConsoleColors.CYAN_BOLD);
        initial = initial.replaceAll("&f&l", ConsoleColors.WHITE_BOLD);
        initial = initial.replaceAll("&0&n", ConsoleColors.BLACK_UNDERLINED);
        initial = initial.replaceAll("&c&n", ConsoleColors.RED_UNDERLINED);
        initial = initial.replaceAll("&d&n", ConsoleColors.PURPLE_UNDERLINED);
        initial = initial.replaceAll("&e&n", ConsoleColors.YELLOW_UNDERLINED);
        initial = initial.replaceAll("&9&n", ConsoleColors.BLUE_UNDERLINED);
        initial = initial.replaceAll("&a&n", ConsoleColors.GREEN_UNDERLINED);
        initial = initial.replaceAll("&b&n", ConsoleColors.CYAN_UNDERLINED);
        initial = initial.replaceAll("&f&n", ConsoleColors.WHITE_UNDERLINED);
        initial = initial.replaceAll("&0", ConsoleColors.BLACK);
        initial = initial.replaceAll("&c", ConsoleColors.RED);
        initial = initial.replaceAll("&d", ConsoleColors.PURPLE);
        initial = initial.replaceAll("&e", ConsoleColors.YELLOW);
        initial = initial.replaceAll("&9", ConsoleColors.BLUE);
        initial = initial.replaceAll("&a", ConsoleColors.GREEN);
        initial = initial.replaceAll("&b", ConsoleColors.CYAN);
        initial = initial.replaceAll("&f", ConsoleColors.WHITE);
        System.out.println(initial);
    }



    public static ImageIcon fromResources(String path) {
        try {
            BufferedImage image = ImageIO.read(NYT.class.getResource("/icons/" + path + ".png"));
            //BufferedImage image = ImageIO.read(new File("src/main/resources/icons/" + path + ".png"));
            return new ImageIcon(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class NYTGame extends JButton {
        public NYTGame(ImageIcon icon, AbstractAction action) {
            super();
            this.setPreferredSize(new Dimension(100, 100));
            this.setIcon(icon);
            this.addActionListener(action);
        }
    }
}
