package me.vlink102.games;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Wordle extends JFrame {
    public static int letters;
    private static final Random random = ThreadLocalRandom.current();
    public static Mode mode;
    public static String word;
    public static JPanel wordsPanel;
    public static List<String> currentWords;
    public static JScrollPane pane;
    public static ColorCyclePanel ccp;
    public static HashMap<Character, Integer> weightedMap;
    public static HashMap<Character, Integer> weightedMapIndex;
    public static Cell[] cells;
    public static Character[] alphabet = new Character[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static List<String> words;
    public static List<Character> usedCharacters;
    public static List<Character> mustContain;
    public static List<Character> remainingMustContain;
    public static List<Character> cannotContain;

    public Wordle() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension((letters * 60) + 10, 800));
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        pane = new JScrollPane();
        pane.setAlignmentY(TOP_ALIGNMENT);
        wordsPanel = new JPanel();
        wordsPanel.setLayout(new GridBagLayout());
        wordsPanel.setBackground(new Color(18, 18, 19));
        wordsPanel.setOpaque(true);
        pane.setViewportView(wordsPanel);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridy = 0;
        contentPane.add(pane, constraints);
        constraints.weighty = 0;
        constraints.gridy = 1;
        JTextField field = new JTextField();
        field.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageInput(field.getText());
                field.setText("");
            }
        });
        final JPanel bottom = new JPanel();
        bottom.setLayout(new GridBagLayout());
        GridBagConstraints bottomConstraints = new GridBagConstraints();
        bottomConstraints.insets = new Insets(5, 5, 5, 5);
        bottomConstraints.gridx = 0;
        HintButton hintButton = new HintButton();
        hintButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageInput("?");
            }
        });
        bottom.add(hintButton, bottomConstraints);
        bottomConstraints.weightx = 1;
        bottomConstraints.fill = GridBagConstraints.HORIZONTAL;
        bottomConstraints.gridx = 1;
        bottom.add(field, bottomConstraints);
        if (Objects.requireNonNull(mode) == Mode.NYT) {
            bottomConstraints.weightx = 0;
            bottomConstraints.gridx = 2;
            ccp = new ColorCyclePanel();
            bottom.add(ccp, bottomConstraints);
        }
        contentPane.add(bottom, constraints);
        this.pack();
        this.setVisible(true);
    }

    public static List<String> readLinesFromResource(String fileName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Wordle.class.getClassLoader().getResourceAsStream(fileName)),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static LetterInfo[] check(String word, Mode mode) {
        if (mode == Mode.NYT) {
            return new LetterInfo[letters];
        }
        if (Wordle.word == null && mode == Mode.PRACTISE) {
            return null;
        }
        LetterInfo[] results = IntStream.range(0, letters).mapToObj(i -> LetterInfo.NO_MATCH).toArray(LetterInfo[]::new);
        for (int i = 0; i < letters; i++) {
            char c = word.charAt(i);
            assert Wordle.word != null;
            if (Wordle.word.contains(String.valueOf(c))) {
                results[i] = LetterInfo.WRONG_POSITION;
            }
        }
        for (int i = 0; i < letters; i++) {
            if (word.charAt(i) == Wordle.word.charAt(i)) {
                results[i] = LetterInfo.CORRECT;
            }
        }
        return results;
    }

    public static JLabel characterPanel(final char character, final LetterInfo info) {
        return new WordleTile(String.valueOf(character).toUpperCase(), info.color);
    }

    @Deprecated
    public static JPanel getPanel(String word) {
        char[] characters = word.toCharArray();
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        LetterInfo[] checked = check(word, mode);
        for (int i = 0; i < letters; i++) {
            constraints.gridx = i;
            char c = characters[i];
            assert checked != null;
            JLabel charPanel = characterPanel(c, checked[i]);
            panel.add(charPanel, constraints);
        }
        return panel;
    }

    public static List<Character> canBe(List<Character> cannotBe) {
        List<Character> newAlphabet = new ArrayList<>(Arrays.stream(alphabet).toList());
        newAlphabet.removeAll(cannotBe);
        return newAlphabet;
    }

    @SuppressWarnings("unused")
    public static List<List<Character>> getPossibleCharacters() {
        List<List<Character>> possibleCharacters = new ArrayList<>();
        for (int i = 0; i < letters; i++) {
            possibleCharacters.add(new ArrayList<>());
            Cell cell = cells[i];
            if (cell.lockedIn != null) {
                possibleCharacters.get(i).add(cell.lockedIn);
            } else {
                List<Character> canBe = canBe(cell.cannotBe);
                possibleCharacters.get(i).addAll(canBe);
            }
        }
        return possibleCharacters;
    }

    @SuppressWarnings("unused")
    public static List<String> getWordsWithChar(int location, char c) {
        List<String> toReturn = new ArrayList<>();
        for (String s : words) {
            if (s.charAt(location) == c) {
                toReturn.add(s);
            }
        }
        return toReturn;
    }

    public static void removeIf(Cell cell, int i, String s, List<String> tempWords) {
        if (cell.lockedIn != null) {
            if (s.charAt(i) != cell.lockedIn) {
                tempWords.remove(s);
            }
        }
    }

    public static int sumCommon(String word) {
        int[] yah = word.chars().distinct().toArray();
        int sum = 0;
        for (int c : yah) {
            if (cannotContain.contains((char) c)) return 0;
            sum += weightedMapIndex.getOrDefault((char) c, 0);
        }
        return sum;
    }

    public static int sumDistinct(String word) {
        return word.chars().distinct().toArray().length;
    }

    public static List<String> mostCommonWordsOrder(List<String> words) {
        return words.stream().sorted(Comparator.comparingInt(Wordle::sumCommon)).toList().reversed();
    }

    public static List<String> genericWordOrder(List<String> words) {
        return words.stream().sorted(Comparator.comparingInt(Wordle::sumDistinct)).toList().reversed();
    }

    public static boolean isValid(String word) {
        for (Character character : mustContain) {
            if (!word.contains(character.toString())) {
                return false;
            }
        }
        for (int i = 0; i < letters; i++) {
            Cell cell = cells[i];
            if (cell.lockedIn != null) {
                if (word.charAt(i) != cell.lockedIn) {
                    return false;
                }
            } else {
                for (Character character : cell.cannotBe) {
                    if (word.charAt(i) == character) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<String> getLogicalGuesses() {
        System.out.println(Arrays.toString(cells));

        List<Character> e = new ArrayList<>();
        e.addAll(cannotContain);
        e.addAll(mustContain);
        List<Character> better = new ArrayList<>(canBe(e));

        List<StringGuess> guesses = new ArrayList<>();
        for (String s : words) {
            int counter = 0;
            int[] sChars = s.chars().distinct().toArray();

            for (Character character : better) {
                if (s.contains(character.toString())) counter++;
            }
            counter -= 5 - sChars.length;
            guesses.add(new StringGuess(s, counter));
        }
        return guesses.stream().sorted(StringGuess::compareTo).map(f -> f.string).toList();
    }

    public static List<String> getGuesses(Cell[] cells) {
        System.out.println(Arrays.toString(cells));
        List<String> tempWords = new ArrayList<>(words);
        for (String s : words) {
            if (!isValid(s)) {
                tempWords.remove(s);
                continue;
            }
            for (int i = 0; i < letters; i++) {
                Cell yeah = cells[i];
                removeIf(yeah, i, s, tempWords);
            }
        }
        return mostCommonWordsOrder(tempWords);
    }

    @SuppressWarnings("unused")
    public static List<String> compressAndRemoveDuplicate(List<String> list) {

        Map<String, Long> frequencyMap = list.stream()
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));

        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static List<String> combineLists(List<List<String>> listOfLists) {

        return listOfLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static List<String> compressAndRemoveDuplicates(List<List<String>> listOfLists) {

        Map<String, Long> frequencyMap = listOfLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));

        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void init(int letters, Mode mode) {
        Wordle.letters = letters;
        Wordle.cells = new Cell[letters];
        Wordle.mode = mode;
        SwingUtilities.invokeLater(() -> {
            FlatMaterialOceanicIJTheme.setup();
            new Wordle();
        });

        for (int i = 0; i < letters; i++) {
            cells[i] = new Cell();
        }
        words = readLinesFromResource("words" + File.separator + letters + ".txt");
        if (mode == Mode.DEBUG) {
            word = "slyly";
            mode = Mode.PRACTISE;
        } else {
            word = words.get(random.nextInt(words.size()));
        }
        currentWords = new ArrayList<>();
        weightedMap = new HashMap<>();
        weightedMapIndex = new HashMap<>();
        usedCharacters = new ArrayList<>();
        mustContain = new ArrayList<>();
        cannotContain = new ArrayList<>();
        remainingMustContain = new ArrayList<>();
        for (String s : words) {
            for (int i = 0; i < letters; i++) {
                char c = s.charAt(i);
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
    }

    public void takeAGuess() {
        List<String> guesses = getGuesses(cells);
        List<String> betterGuesses = getLogicalGuesses();
        StringJoiner guessList = new StringJoiner(", ");
        StringJoiner betterGuessList = new StringJoiner(", ");
        StringJoiner badGuessList = new StringJoiner(", ");
        StringJoiner badBetterGuessList = new StringJoiner(", ");
        for (int i = 0; i < Math.min(guesses.size(), 5); i++) {
            guessList.add(guesses.get(i));
        }
        for (int i = 0; i < Math.min(betterGuesses.size(), 5); i++) {
            betterGuessList.add(betterGuesses.get(i));
        }
        for (int i = 0; i < 5; i++) {
            if (guesses.size() - (i + 1) < 0) break;
            badGuessList.add(guesses.get(guesses.size() - (i + 1)));
        }
        for (int i = 0; i < 5; i++) {
            if (betterGuesses.size() - (i + 1) < 0) break;
            badBetterGuessList.add(betterGuesses.get(betterGuesses.size() - (i + 1)));
        }
        System.out.println("Computer guesses: " + ("[" + guessList + "]" + " (" + ((1f / guesses.size()) * 100) + "%)") + "\n" + ("[" + betterGuessList + "]" + " (" + ((1f / betterGuesses.size()) * 100)) + "%)");
        System.out.println("BAD guesses: " + ("[" + badGuessList + "]" + " (" + ((1f / guesses.size()) * 100) + "%)") + "\n" + ("[" + badBetterGuessList + "]" + " (" + ((1f / betterGuesses.size()) * 100)) + "%)");
    }

    public void manageInput(String input) {
        switch (mode) {
            case NYT -> {
                if (input.equals("?")) {
                    takeAGuess();
                    return;
                }
                currentWords.add(input);
                for (char c : input.toCharArray()) {
                    if (!usedCharacters.contains(c)) {
                        usedCharacters.add(c);
                    }
                }
                String yea = ccp.getColors();
                System.out.println(input);
                System.out.println(yea);
                LetterInfo[] info = new LetterInfo[letters];
                for (int i = 0; i < letters; i++) {
                    char c = input.charAt(i);
                    Cell cell = cells[i];
                    int yeah = Integer.parseInt(String.valueOf(yea.charAt(i)));
                    switch (yeah) {
                        case 0 -> {
                            info[i] = LetterInfo.NO_MATCH;
                            cell.wrongPosition(c);
                            if (!cannotContain.contains(c)) {
                                cannotContain.add(c);
                            }
                        }
                        case 1 -> {
                            info[i] = LetterInfo.WRONG_POSITION;
                            if (!mustContain.contains(c)) {
                                mustContain.add(c);
                            }
                            cell.wrongPosition(c);
                        }
                        case 2 -> {
                            info[i] = LetterInfo.CORRECT;
                            if (!mustContain.contains(c)) {
                                mustContain.add(c);
                            }
                            cell.lockIn(c);
                        }
                    }
                }
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = currentWords.size() - 1;
                wordsPanel.add(new WordleTilePanel(input, info), c);
                JScrollBar vertical = pane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
                wordsPanel.updateUI();
                System.out.println("Must Contain: " + mustContain);
                System.out.println("Used: " + usedCharacters);
                System.out.println("Cannot contain: " + cannotContain);
            }
            case PRACTISE, DEBUG -> {
                String next = input.toLowerCase();
                if (next.equals("?")) {
                    takeAGuess();
                    return;
                }
                if (words.contains(next)) {
                    LetterInfo[] info = check(next, mode);
                    System.out.println(Arrays.toString(info));
                    currentWords.add(next);
                    for (int i = 0; i < letters; i++) {
                        Cell cell = cells[i];
                        assert info != null;
                        LetterInfo letter = info[i];
                        char c = next.charAt(i);
                        switch (letter) {
                            case NO_MATCH -> {
                                cell.wrongPosition(c);
                                if (!cannotContain.contains(c)) {
                                    cannotContain.add(c);
                                }
                            }
                            case WRONG_POSITION -> {
                                cell.wrongPosition(c);
                                if (!mustContain.contains(c)) {
                                    mustContain.add(c);
                                }
                            }
                            case CORRECT -> {
                                if (!mustContain.contains(c)) {
                                    mustContain.add(c);
                                }
                                cell.lockIn(c);
                            }
                        }
                    }
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = currentWords.size() - 1;
                    wordsPanel.add(new WordleTilePanel(next), c);
                    JScrollBar vertical = pane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                    wordsPanel.updateUI();
                }
            }
        }
    }


    public static <E extends Enum<E>> JComboBox<E> createEnumComboBox(Class<E> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) {
            throw new IllegalArgumentException("Provided class must be an Enum type.");
        }
        return new JComboBox<>(enumClass.getEnumConstants());
    }

    public enum Mode {
        PRACTISE,
        NYT,
        DEBUG
    }

    @Getter
    public enum LetterInfo {
        NO_MATCH(new Color(58, 58, 60)),
        WRONG_POSITION(new Color(181, 159, 59)),
        CORRECT(new Color(83, 141, 78)),
        UNKNOWN(Color.CYAN);
        private final Color color;

        LetterInfo(Color color) {
            this.color = color;
        }

    }

    public static class ColorCyclePanel extends JPanel {
        private final JButton[] buttons;
        private final Color[] colors = {LetterInfo.NO_MATCH.color, LetterInfo.WRONG_POSITION.color, LetterInfo.CORRECT.color};

        public ColorCyclePanel() {

            setLayout(new GridLayout(1, 5));

            buttons = new JButton[5];

            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = new JButton();
                buttons[i].setPreferredSize(new Dimension(20, 20));
                buttons[i].setBackground(colors[0]);
                buttons[i].setOpaque(true);
                buttons[i].setBorderPainted(false);
                int index = i;
                buttons[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            cycleColor(index, true);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            cycleColor(index, false);
                        }
                    }
                });
                add(buttons[i]);
            }
        }

        private void cycleColor(int index, boolean forward) {
            JButton button = buttons[index];
            Color currentColor = button.getBackground();
            int currentIndex = getColorIndex(currentColor);
            int nextColorIndex;
            if (forward) {
                nextColorIndex = (currentIndex + 1) % colors.length;
            } else {
                nextColorIndex = (currentIndex - 1 + colors.length) % colors.length;
            }
            button.setBackground(colors[nextColorIndex]);
        }

        private int getColorIndex(Color color) {
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equals(color)) {
                    return i;
                }
            }
            return 0;
        }

        public String getColors() {
            StringBuilder sb = new StringBuilder();
            for (JButton button : buttons) {
                sb.append(getColorIndex(button.getBackground()));
            }
            return sb.toString();
        }
    }

    public static class HintButton extends JButton {
        public HintButton() {

            setText("?");
            setFont(new Font("Arial", Font.BOLD, 15));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(40, 40));
            setForeground(Color.WHITE);
            setBackground(new Color(70, 130, 180));

            setToolTipText("Click for a hint!");
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(20, 20);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isPressed()) {
                g.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g.setColor(getBackground().brighter());
            } else {
                g.setColor(getBackground());
            }

            g.fillOval(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(getBackground().darker());
            g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
        }

        @Override
        public boolean contains(int x, int y) {

            double radius = getWidth() / 2.0;
            double centerX = getWidth() / 2.0;
            double centerY = getHeight() / 2.0;
            return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2);
        }
    }

    public static class WordleTile extends JLabel {
        public WordleTile(String letter, Color backgroundColor) {
            super(letter, SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 30));
            setOpaque(false);
            setBackground(backgroundColor);
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(50, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        }
    }

    public static class WordleTilePanel extends JPanel {
        public WordleTilePanel(String word, LetterInfo[] info) {
            setLayout(new GridLayout(1, 5, 5, 5));
            setBackground(new Color(18, 18, 19));
            setBorder(new EmptyBorder(0, 0, 5, 0));
            for (int i = 0; i < letters; i++) {
                char c = word.charAt(i);
                add(characterPanel(c, info[i]));
            }
        }

        public WordleTilePanel(String word) {
            setLayout(new GridLayout(1, 5, 5, 5));
            setBackground(new Color(18, 18, 19));
            setBorder(new EmptyBorder(0, 0, 5, 0));
            LetterInfo[] info = check(word, mode);
            for (int i = 0; i < letters; i++) {
                char c = word.charAt(i);
                add(characterPanel(c, info[i]));
            }
        }
    }

    public static class Cell {
        @Getter
        private final List<Character> cannotBe;
        private Character lockedIn = null;

        public Cell() {
            cannotBe = new ArrayList<>();
        }

        public void lockIn(char c) {
            this.lockedIn = c;
        }

        public void wrongPosition(char c) {
            if (!cannotBe.contains(c)) {
                this.cannotBe.add(c);
            }
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "cannotBe=" + cannotBe +
                    ", lockedIn=" + lockedIn +
                    '}';
        }
    }

    public record StringGuess(String string, int count) implements Comparable<StringGuess> {
        @Override
        public int compareTo(StringGuess o) {
            return Integer.compare(o.count, count);
        }
    }
}
