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

import static me.vlink102.games.Wordle.readLinesFromResource;

public class LetterBoxed extends JFrame {
    private final List<String> usedWords;
    public static List<String> words;
    public static HashMap<Character, Integer> weightedMap;
    public static HashMap<Character, Integer> weightedMapIndex;
    public static JTextField[][] fields;

    public class Field extends JTextField {
        public Field() {
            super(1);
            setFont(new Font("Arial", Font.BOLD, 30));
        }
    }

    public static String fromGUI() {
        return fields[0][0].getText() + fields[0][1].getText() + fields[0][2].getText() + " "
                + fields[1][0].getText() + fields[1][1].getText() + fields[1][2].getText() + " "
                + fields[2][0].getText() + fields[2][1].getText() + fields[2][2].getText() + " "
                + fields[3][0].getText() + fields[3][1].getText() + fields[3][2].getText();
    }

    public LetterBoxed() {
        words = readLinesFromResource("words" + File.separator + "words_alpha.txt");
        fields = new JTextField[4][3];
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
        usedWords = new ArrayList<>();
        setTitle("Letter Boxed");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;

        BufferedImage image = null;
        try {
            image = ImageIO.read(NYT.class.getResource("/icons/letterbox.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final JPanel gridPanel = new JPanel(); /*{
            @Override
            protected void paintChildren(Graphics g) {
                g.drawImage(finalImage, this.getWidth() / 5, this.getHeight() / 5, content.getWidth() - (int) (this.getWidth() * (2f/5)), content.getHeight() - (int) (this.getHeight() * (2f/5)), null);
                for (Component component : this.getComponents()) {
                    component.repaint();
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 375);
            }
        }*/
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                 fields[i][j] = new Field();
            }
        }
        gridPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for custom grid placement
        createGrid(gridPanel, image);


        /*gridPanel.setLayout(new GridLayout(5,5,1,1));

        JPanel[][] griddy = new JPanel[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                JPanel panel = new JPanel();
                griddy[i][j] = panel;
                gridPanel.add(panel);
                panel.setAlignmentY(CENTER_ALIGNMENT);
                panel.setAlignmentX(CENTER_ALIGNMENT);
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                fields[i][j] = new Field();
            }
        }
        griddy[0][1].add(fields[0][0]);
        griddy[0][2].add(fields[0][1]);
        griddy[0][3].add(fields[0][2]);

        griddy[1][0].add(fields[1][0]);
        griddy[2][0].add(fields[1][1]);
        griddy[3][0].add(fields[1][2]);

        griddy[1][4].add(fields[2][0]);
        griddy[2][4].add(fields[2][1]);
        griddy[3][4].add(fields[2][2]);

        griddy[4][1].add(fields[3][0]);
        griddy[4][2].add(fields[3][1]);
        griddy[4][3].add(fields[3][2]);
*/
        //letterBox.setBounds(0, 0, 200, 200);
        //letterBox.setPreferredSize(new Dimension(finalImage.getWidth(), finalImage.getHeight()));
        //griddy[1][1] = letterBox;
        content.add(gridPanel, constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        final JButton button = new JButton("Solve Letterbox");
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String letters = fromGUI();//"eci axy hrn otu";//scanner.nextLine();
                String[] split = letters.split(" ");
                char[][] boxed = boxFromSplit(split);
                //System.out.println(filterWords(boxed).size());
                System.out.println(combinatoryPhrases(boxed));
                List<String> yea = Wordle.genericWordOrder(filterWords(boxed));
                List<Character> fromBoxes = getFromBoxes(boxed);
                List<String> singles = getCoveredSingles(yea, fromBoxes);
                List<List<String>> covered = getCoveredDoubles(yea, fromBoxes);
                //List<List<String>> triples = getCoveredTriples(yea, fromBoxes);
                NYT.cc("1-Word Solutions (" + singles.size() + "): " + singles);
                NYT.cc("2-Word Solutions (" + covered.size() + "): " + covered);
                //NYT.cc("3-Word Solutions (" + triples.size() + "): " + triples);
            }
        });
        constraints.gridy = 1;
        content.add(button, constraints);
        pack();
        setVisible(true);
    }

    private static void createGrid(JPanel grid, Image image) {
        GridBagConstraints gbc = new GridBagConstraints();

        // Add row labels at the top (_ 1 2 3 _)
        gbc.gridy = 0;
        for (int x = 0; x < 5; x++) {
            gbc.gridx = x;
            if (x == 0 || x == 4) {
                grid.add(new JLabel(" "), gbc);
            } else {
                grid.add(fields[0][x - 1], gbc);
            }
        }

        // Add the rows with letters (a, b, c) and the centered image
        for (int y = 1; y <= 3; y++) {
            gbc.gridy = y;
            for (int x = 0; x < 5; x++) {
                gbc.gridx = x;
                if (x == 0 || x == 4) {
                    // Add row labels (a, b, c) on the left and right
                    grid.add(fields[x == 0 ? 3 : 1][y - 1], gbc);
                } else if (x == 2 && y == 2) {
                    // Add the image in the center of the grid
                    grid.add(new JLabel(new ImageIcon(image)), gbc);
                } else {
                    grid.add(new JLabel(" "), gbc); // Empty cells
                }
            }
        }

        // Add row labels at the bottom (_ 1 2 3 _)
        gbc.gridy = 4;
        for (int x = 0; x < 5; x++) {
            gbc.gridx = x;
            if (x == 0 || x == 4) {
                grid.add(new JLabel(" "), gbc);
            } else {
                grid.add(fields[2][x - 1], gbc);
            }
        }
    }

    public static List<String> getCoveredSingles(List<String> total, List<Character> mustCover) {
        List<String> coveredSingles = new ArrayList<>();
        for (String s : total) {
            List<Character> ya = new ArrayList<>();
            for (char c : s.toCharArray()) {
                if (!ya.contains(c)) {
                    ya.add(c);
                }
            }
            if (ya.size() == 12) {
                coveredSingles.add(s);
            }
        }

        return coveredSingles;
    }

    public static List<List<String>> getCoveredDoubles(List<String> total, List<Character> mustCover) {
        List<List<String>> coveredDoubles = new ArrayList<>();
        for (String s1 : total) {
            for (String s2 : total) {
                if (s2.startsWith(s1.charAt(s1.length() - 1) + "")) {
                    String joined = s1 + s2;
                    List<Character> done = new ArrayList<>();
                    for (char c : joined.toCharArray()) {
                        if (mustCover.contains(c)) {
                            if (!done.contains(c)) {
                                done.add(c);
                            }
                        }
                        if (done.size() == 12) {
                            coveredDoubles.add(List.of(s1, s2));
                            break;
                        }
                    }

                }
            }
        }
        return coveredDoubles;
    }
    public static List<List<String>> getCoveredTriples(List<String> total, List<Character> mustCover) {
        List<List<String>> coveredTriples = new ArrayList<>();
        for (String s1 : total) {
            for (String s2 : total) {
                if (s2.startsWith(s1.charAt(s1.length() - 1) + "")) {
                    for (String s3 : total) {
                        if (s3.startsWith(s2.charAt(s2.length() - 1) + "")) {
                            String joined = s1 + s2 + s3;
                            List<Character> done = new ArrayList<>();
                            for (char c : joined.toCharArray()) {
                                if (mustCover.contains(c)) {
                                    if (!done.contains(c)) {
                                        done.add(c);
                                    }
                                }
                                if (done.size() == 12) {
                                    coveredTriples.add(List.of(s1, s2, s3));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return coveredTriples;
    }

    public static char[][] boxFromSplit(String[] split) {
        char[][] chars = new char[4][3];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                chars[i][j] = split[i].charAt(j);
            }
        }
        return chars;
    }

    public static List<String> combinatoryPhrases(char[][] boxes) {
        List<String> combined = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            combined.add("" + boxes[i][0] + boxes[i][1]);
            combined.add("" + boxes[i][0] + boxes[i][2]);
            combined.add("" + boxes[i][1] + boxes[i][0]);
            combined.add("" + boxes[i][1] + boxes[i][2]);
            combined.add("" + boxes[i][2] + boxes[i][0]);
            combined.add("" + boxes[i][2] + boxes[i][1]);

            combined.add("" + boxes[i][0] + boxes[i][0]);
            combined.add("" + boxes[i][1] + boxes[i][1]);
            combined.add("" + boxes[i][2] + boxes[i][2]);
        }
        return combined;
    }

    public static List<Character> getFromBoxes(char[][] boxes) {
        List<Character> yeah = new ArrayList<>();
        for (char[] box : boxes) {
            for (char c : box) {
                yeah.add(c);
            }
        }
        return yeah;
    }

    public static List<String> filterWords(char[][] boxes) {
        List<String> filtered = new ArrayList<>(words);
        List<Character> used = getFromBoxes(boxes);
        System.out.println(used);
        for (String word : words) {

            if (word.length() < 3) {
                filtered.remove(word);
            }
            boolean cont = true;
            for (char c : word.toCharArray()) {
                if (!used.contains(c)) {
                    filtered.remove(word);
                    cont = false;
                    break;
                }
            }
            if (cont) {
                for (String combinatoryPhrase : combinatoryPhrases(boxes)) {
                    if (word.contains(combinatoryPhrase)) {
                        filtered.remove(word);
                        break;
                    }
                }
            }
        }
        return filtered;
    }
}