package me.vlink102.games;

import me.vlink102.NYT;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static me.vlink102.games.Wordle.genericWordOrder;
import static me.vlink102.games.Wordle.readLinesFromResource;

public class SpellingBee extends JFrame {
    public static List<String> words;
    public static HashMap<Character, Integer> weightedMap;
    public static HashMap<Character, Integer> weightedMapIndex;

    public SpellingBee() {
        words = readLinesFromResource("words" + File.separator + "words_alpha.txt");
        weightedMap = new HashMap<>();
        weightedMapIndex = new HashMap<>();
        for (String s : words) {
            for (char c : s.toCharArray()) {
                weightedMap.put(c, weightedMap.getOrDefault(c, 0) + 1);
            }
        }
        weightedMap = weightedMap.entrySet()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, _) -> e1,
                        LinkedHashMap::new
                ));
        int d = 25;
        for (Map.Entry<Character, Integer> characterIntegerEntry : weightedMap.entrySet()) {
            weightedMapIndex.put(characterIntegerEntry.getKey(), d);
            d--;
        }
        System.out.println(weightedMap);
        System.out.println(weightedMapIndex);

        setTitle("Spelling Bee");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        BufferedImage image = null;
        try {
            image = ImageIO.read(NYT.class.getResource("/icons/beegrid.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final int inputBoxSize = 50;

        JPanel panel = new JPanel();
        panel.setLayout(null);
        final JTextField t = new JTextField();
        t.setBorder(null);
        t.setOpaque(false);
        t.setFont(new Font("Arial", Font.BOLD, 50));
        t.setHorizontalAlignment(SwingConstants.CENTER);
        t.setBounds((image.getWidth() / 2) - (inputBoxSize / 2), ((image.getHeight() / 3) / 2) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t2 = new JTextField();
        t2.setBorder(null);
        t2.setOpaque(false);
        t2.setFont(new Font("Arial", Font.BOLD, 50));
        t2.setHorizontalAlignment(SwingConstants.CENTER);
        t2.setBounds((image.getWidth() / 2) - (inputBoxSize / 2), (image.getHeight() / 2) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t3 = new JTextField();
        t3.setBorder(null);
        t3.setOpaque(false);
        t3.setHorizontalAlignment(SwingConstants.CENTER);
        t3.setFont(new Font("Arial", Font.BOLD, 50));
        t3.setBounds((image.getWidth() / 2) - (inputBoxSize / 2), (int) (image.getHeight() * 2.5f / 3) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t4 = new JTextField();
        t4.setHorizontalAlignment(SwingConstants.CENTER);
        t4.setBorder(null);
        t4.setOpaque(false);
        t4.setFont(new Font("Arial", Font.BOLD, 50));
        t4.setBounds((image.getWidth() / 4) - (inputBoxSize), (image.getHeight() /3) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t5 = new JTextField();

        t5.setHorizontalAlignment(SwingConstants.CENTER);
        t5.setFont(new Font("Arial", Font.BOLD, 50));
        t5.setBorder(null);
        t5.setOpaque(false);
        t5.setBounds(((image.getWidth() * 3/ 4)), ( image.getHeight() /3) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t6 = new JTextField();

        t6.setHorizontalAlignment(SwingConstants.CENTER);
        t6.setFont(new Font("Arial", Font.BOLD, 50));
        t6.setBorder(null);
        t6.setOpaque(false);
        t6.setBounds(((image.getWidth() * 3/ 4)), ( image.getHeight() * 2 /3) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        final JTextField t7 = new JTextField();

        t7.setHorizontalAlignment(SwingConstants.CENTER);
        t7.setFont(new Font("Arial", Font.BOLD, 50));
        t7.setBorder(null);
        t7.setOpaque(false);
        t7.setBounds(((image.getWidth() / 4) - (inputBoxSize)), ( image.getHeight() * 2 /3) - (inputBoxSize / 2), inputBoxSize, inputBoxSize);
        panel.add(t);
        panel.add(t2);
        panel.add(t3);
        panel.add(t4);
        panel.add(t5);
        panel.add(t6);
        panel.add(t7);
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBounds(0, 0, image.getWidth(), image.getHeight());
        panel.add(label);
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

        content.add(panel, c);

        final JButton solve = new JButton("Solve");
        solve.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Character required = t2.getText().charAt(0);
                List<Character> all = new ArrayList<>();
                all.add(t.getText().toLowerCase().charAt(0));
                all.add(t3.getText().toLowerCase().charAt(0));
                all.add(t4.getText().toLowerCase().charAt(0));
                all.add(t5.getText().toLowerCase().charAt(0));
                all.add(t6.getText().toLowerCase().charAt(0));
                all.add(t7.getText().toLowerCase().charAt(0));
                all.add(required);
                System.out.println(all);

                List<String> filtered = new ArrayList<>(words);

                for (String word : words) {
                    if (!word.contains(required.toString())) {
                        filtered.remove(word);
                        continue;
                    }
                    for (char c1 : word.toCharArray()) {
                        if (!all.contains(c1)) {
                            filtered.remove(word);
                            break;
                        }
                    }
                }

                List<String> best = filtered.stream().sorted(Comparator.comparingInt(String::length)).toList().reversed();

                System.out.println(best);
            }
        });

        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        content.add(solve, c);
        pack();
        setVisible(true);
    }
}
