package me.vlink102;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sudoku extends JFrame {
    private static final HashMap<Point, JButton> buttonMap = new HashMap<>();
    public static int[][] matrix;

    public static int[][] mostRecentGenerated;

    public Sudoku() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(500, 650));

        matrix = generateSudoku(Difficulty.MEDIUM);
        mostRecentGenerated = Sudoku.clone(matrix);

        Container container = this.getContentPane();
        container.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 0, 5, 0);
        constraints.fill = GridBagConstraints.BOTH;
        container.add(new SudokuGrid(), constraints);

        final JPanel bottom = new JPanel();
        bottom.setLayout(new GridBagLayout());
        GridBagConstraints bottomConstraints = new GridBagConstraints();
        bottomConstraints.fill = GridBagConstraints.HORIZONTAL;

        bottomConstraints.weightx = 1;
        bottomConstraints.weighty = 0;

        final SettingsPanel generateSettings = new SettingsPanel(
                SettingsPanel.Setting.spinner("Solve Delay (ms)", 10, 0, 1000, 1),
                SettingsPanel.Setting.dropDown("Generated Difficulty", Difficulty.class)
        );

        bottom.add(generateSettings, bottomConstraints);

        final JPanel solvePanel = new JPanel();
        solvePanel.setLayout(new GridBagLayout());
        GridBagConstraints solvePanelConstraints = new GridBagConstraints();

        solvePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
        solvePanelConstraints.weightx = 1;
        solvePanelConstraints.weighty = 0;

        final JButton solveButton = new NiceButton("Solve (Realtime)", new Color(76, 175, 80), new Color(56, 142, 60), new Color(46, 125, 50), new Runnable() {
            @Override
            public void run() {
                mostRecentGenerated = Sudoku.clone(matrix);
                try {
                    if (solveSudoku(matrix, true, (int) generateSettings.getSettings()[0].getValue()).get()) {
                        updateMap(matrix, mostRecentGenerated);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        final JButton quickSolveButton = new NiceButton("Quick-Solve", new Color(33, 150, 243), new Color(30, 136, 229), new Color(25, 118, 210), () -> {
            mostRecentGenerated = clone(matrix);
            try {

                if (solveSudoku(matrix, false, 0).get()) {
                    updateMap(matrix, mostRecentGenerated);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        final JButton generateRandom = new NiceButton("Generate Random", new Color(255, 152, 0), new Color(251, 140, 0), new Color(245, 124, 0), new Runnable() {
            @Override
            public void run() {
                Difficulty difficulty = (Difficulty) generateSettings.getSettings()[1].getValue();
                int[][] pluh = generateSudoku(difficulty);
                mostRecentGenerated = Sudoku.clone(pluh);
                matrix = Sudoku.clone(pluh);

                updateMap(matrix, mostRecentGenerated);
            }
        });
        solvePanel.add(generateRandom, solvePanelConstraints);
        solvePanelConstraints.gridy = 1;
        solvePanel.add(solveButton, solvePanelConstraints);
        solvePanelConstraints.gridy = 2;
        solvePanel.add(quickSolveButton, solvePanelConstraints);
        bottomConstraints.gridx = 1;
        bottom.add(solvePanel, bottomConstraints);

        constraints.gridy = 1;
        container.add(bottom, constraints);

        updateMap(matrix, mostRecentGenerated);

        this.pack();
        this.setVisible(true);
    }

    private static boolean isSafe(int[][] grid, int row, int col, int num) {

        for (int i = 0; i < 9; i++) {
            if (grid[row][i] == num || grid[i][col] == num) {
                return false;
            }
        }

        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[startRow + i][startCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean fillGrid(int[][] grid) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (grid[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
                    Collections.shuffle(numbers);
                    for (int num : numbers) {
                        if (isSafe(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (fillGrid(grid)) {
                                return true;
                            }

                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static void removeCells(int[][] grid, int cellsToRemove) {
        Random random = new Random();
        while (cellsToRemove > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            if (grid[row][col] != 0) {
                grid[row][col] = 0;
                cellsToRemove--;
            }
        }
    }

    public static int[][] generateSudoku(Difficulty difficulty) {
        int[][] grid = new int[9][9];

        if (!fillGrid(grid)) {
            throw new IllegalStateException("Failed to generate a valid Sudoku solution.");
        }

        int cellsToRemove = switch (difficulty) {
            case EASY -> 30;
            case MEDIUM -> 45;
            case HARD -> 55;
            case EXTREME -> 60;
        };
        removeCells(grid, cellsToRemove);
        return grid;
    }

    public static boolean isValid(int[][] grid) {

        for (int i = 0; i < 9; i++) {
            boolean[] rowCheck = new boolean[9];
            boolean[] colCheck = new boolean[9];
            for (int j = 0; j < 9; j++) {
                if (grid[i][j] != 0 && rowCheck[grid[i][j] - 1]) return false;
                if (grid[j][i] != 0 && colCheck[grid[j][i] - 1]) return false;
                if (grid[i][j] != 0) rowCheck[grid[i][j] - 1] = true;
                if (grid[j][i] != 0) colCheck[grid[j][i] - 1] = true;
            }
        }

        for (int block = 0; block < 9; block++) {
            boolean[] blockCheck = new boolean[9];
            int startRow = (block / 3) * 3;
            int startCol = (block % 3) * 3;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int val = grid[startRow + i][startCol + j];
                    if (val != 0 && blockCheck[val - 1]) return false;
                    if (val != 0) blockCheck[val - 1] = true;
                }
            }
        }
        return true;
    }

    public static int[][] rotateMatrixClockwise(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = matrix[i][j];
            }
        }
        return rotated;
    }

    public static int findMostFrequent(int[][] arr) {
        if (arr == null || arr.length == 0 || Arrays.stream(arr).allMatch(row -> row == null || row.length == 0)) {
            throw new IllegalArgumentException("Array cannot be null, empty, or contain empty rows");
        }

        return Arrays.stream(arr)
                .flatMapToInt(Arrays::stream)
                .filter(num -> num != 0)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Array cannot be empty"));
    }

    public static int findMostFrequent(int[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }

        return Arrays.stream(arr)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Array cannot be empty"));
    }

    public static ValidationResult validateGrid(int[][] matrix) {

        ValidationResult result = new ValidationResult();
        for (int[] row : matrix) {
            if (Arrays.stream(row).filter(num -> num != 0).distinct().count() != Arrays.stream(row).filter(num -> num != 0).count()) {
                int frequent = findMostFrequent(row);
                result.addRowError(frequent);
            }
        }

        for (int col = 0; col < 9; col++) {
            Set<Integer> uniqueValues = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                int num = matrix[row][col];
                if (num != 0 && !uniqueValues.add(num)) {
                    result.addColumnError(num);
                }
            }
        }

        int[][][] subMatrices = splitTo3x3Grids(matrix);
        for (int[][] subMatrix : subMatrices) {
            if (!areAllValuesDistinct(subMatrix)) {
                result.addBoxError(findMostFrequent(subMatrix));
            }
        }
        return result;
    }

    public static boolean areAllValuesDistinct(int[][] grid) {
        Set<Integer> uniqueValues = new HashSet<>();
        return Arrays.stream(grid)
                .flatMapToInt(Arrays::stream)
                .filter(num -> num != 0)
                .allMatch(uniqueValues::add);
    }

    public static int[][][] splitTo3x3Grids(int[][] grid) {
        if (grid.length != 9 || grid[0].length != 9) {
            throw new IllegalArgumentException("Input grid must be 9x9.");
        }
        int[][][] result = new int[9][3][3];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int gridIndex = (row / 3) * 3 + (col / 3);
                int subGridRow = row % 3;
                int subGridCol = col % 3;
                result[gridIndex][subGridRow][subGridCol] = grid[row][col];
            }
        }
        return result;
    }

    public static CompletableFuture<Boolean> solveSudoku(int[][] grid, boolean liveUpdate, int delay) {
        return CompletableFuture.supplyAsync(() -> {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    if (grid[row][col] == 0) {

                        for (int num = 1; num <= 9; num++) {
                            grid[row][col] = num;

                            if (liveUpdate) {
                                buttonMap.get(new Point(row, col)).setText("" + (num == 0 ? "" : num));
                            }


                            if (/*validateGrid(grid)*/isValid(grid)) {

                                try {
                                    if (solveSudoku(grid, liveUpdate, delay).get()) {

                                        return true;
                                    }
                                    try {
                                        Thread.sleep(delay);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            grid[row][col] = 0;
                            if (liveUpdate) buttonMap.get(new Point(row, col)).setText("");
                        }
                        return false;
                    }
                }
            }

            return true;
        });
    }

    public static void updateMap(int[][] grid, int[][] mostRecentGenerated) {
        buttonMap.forEach((point, jButton) -> {
            int a = grid[point.row][point.col];
            jButton.setText("" + (a == 0 ? "" : a));

            jButton.setForeground(new Color(95, 95, 95));

            if (mostRecentGenerated[point.row][point.col] != 0) {
                jButton.setBackground(new Color(233, 233, 233));
            } else {
                jButton.setBackground(new Color(255, 255, 255));
            }
        });
    }

    static int[][] clone(int[][] a) {
        int[][] b = new int[a.length][];
        for (int i = 0; i < a.length; i++) {
            b[i] = new int[a[i].length];
            System.arraycopy(a[i], 0, b[i], 0, a[i].length);
        }
        return b;
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD, EXTREME
    }

    public record Point(int row, int col) implements Comparable<Point> {
        @Override
        public int compareTo(Point o) {
            int result = Integer.compare(this.row, o.row);
            if (result != 0) return result;
            return Integer.compare(this.col, o.col);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row && col == point.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    public static class ValidationResult {
        private final List<String> errors;

        public ValidationResult() {
            errors = new ArrayList<>();
        }

        public void addRowError(int num) {
            errors.add("Cannot place " + num + " in this row!");
        }

        public void addColumnError(int num) {
            errors.add("Cannot place " + num + " in this column!");
        }

        public void addBoxError(int num) {
            errors.add("Cannot place " + num + " in this box!");
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    public static class NumberPad extends JDialog {
        private int selectedNumber = -1;

        public NumberPad(JFrame parent) {
            super(parent, "Number Pad", true);
            initializeUI();
        }

        public static int showNumberPad(JFrame parent) {
            NumberPad numberPad = new NumberPad(parent);
            numberPad.setVisible(true);
            return numberPad.getSelectedNumber();
        }

        private void initializeUI() {
            setLayout(new GridLayout(3, 3, 5, 5));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            for (int i = 1; i <= 9; i++) {
                JButton button = new JButton(String.valueOf(i));
                button.setFont(new Font("Arial", Font.BOLD, 20));
                button.addActionListener(new ButtonClickListener(i));
                add(button);
            }
            pack();
            setLocationRelativeTo(getParent());
        }

        public int getSelectedNumber() {
            return selectedNumber;
        }

        private class ButtonClickListener implements ActionListener {
            private final int number;

            public ButtonClickListener(int number) {
                this.number = number;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedNumber = number;
                dispose();
            }
        }
    }

    public class SudokuGrid extends JPanel {
        private static final int GRID_SIZE = 9; // 9x9 grid
        private static final int SUBGRID_SIZE = 3; // 3x3 sub-grids
        private static final int CELL_SIZE = 50; // Default cell size in pixels
        private static final int BORDER_THICKNESS = 4; // Outer border thickness

        public SudokuGrid() {
            setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));
            initializeCells();

            SwingUtilities.invokeLater(this::repaint);
            // Add a thick outer border around the grid
            //setBorder(BorderFactory.createLineBorder(new Color(95, 95, 95), BORDER_THICKNESS));
        }

        private void initializeCells() {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    int preset = matrix[i][j];
                    JButton cell = new JButton();
                    cell.setFont(new Font("Arial", Font.BOLD, 28)); // Elegant font
                    //cell.setPreferredSize(new Dimension(50,50));
                    cell.setBackground(Color.WHITE);
                    cell.setFocusable(false);
                    cell.setBorder(BorderFactory.createLineBorder(new Color(197, 197, 197), 1)); // Thin borders for individual cells
                    int finalI = i;
                    int finalJ = j;
                    cell.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int result = NumberPad.showNumberPad(Sudoku.this);
                            matrix[finalI][finalJ] = result;
                            ValidationResult result1 = validateGrid(matrix);
                            if (!result1.isValid()) {
                                JOptionPane.showMessageDialog(null, String.join("\n", result1.errors));
                            }
                            cell.setText("" + result);
                        }
                    });
                    if (preset != 0) cell.setText("" + preset);
                    add(cell);
                    buttonMap.put(new Point(finalI, finalJ), cell);
                }
            }
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);

            Graphics2D g2 = (Graphics2D) g;

            // Set anti-aliasing for smoother lines
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw thicker lines for 3x3 grid on top of everything else
            g2.setColor(new Color(185, 185, 185));
            g2.setStroke(new BasicStroke(3)); // Thicker stroke for subgrid lines

            int panelWidth = getWidth() - BORDER_THICKNESS * 2; // Account for outer border
            int panelHeight = getHeight() - BORDER_THICKNESS * 2; // Account for outer border
            int cellWidth = panelWidth / GRID_SIZE;
            int cellHeight = panelHeight / GRID_SIZE;

            // Draw vertical lines for 3x3 grid
            for (int i = 0; i <= GRID_SIZE; i++) {
                if (i % SUBGRID_SIZE == 0) {
                    int x = BORDER_THICKNESS + i * cellWidth;
                    g2.drawLine(x, BORDER_THICKNESS, x, panelHeight + BORDER_THICKNESS);
                }
            }

            // Draw horizontal lines for 3x3 grid
            for (int i = 0; i <= GRID_SIZE; i++) {
                if (i % SUBGRID_SIZE == 0) {
                    int y = BORDER_THICKNESS + i * cellHeight;
                    g2.drawLine(BORDER_THICKNESS, y, panelWidth + BORDER_THICKNESS, y);
                }
            }

            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(BORDER_THICKNESS));
            g2.drawRect(
                    BORDER_THICKNESS / 2,
                    BORDER_THICKNESS / 2,
                    getWidth() - BORDER_THICKNESS,
                    getHeight() - BORDER_THICKNESS
            );
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(CELL_SIZE * GRID_SIZE + BORDER_THICKNESS * 2, CELL_SIZE * GRID_SIZE + BORDER_THICKNESS * 2);
        }
    }
}