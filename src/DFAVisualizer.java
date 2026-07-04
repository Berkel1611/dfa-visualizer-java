import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class DFAVisualizer extends JFrame {
    private DFAPanel dfaPanel;
    private JTextField inputField;
    private JButton loadButton, prevButton, nextButton, resetButton;
    private JLabel statusLabel, wordLabel, stepLabel;
    private final String[] states_names = {"q0", "q1", "q2", "q3", "q4", "q5", "q6"};

    // Definicja automatu
    private Map<String, Point> states;
    private Map<Transition, String> transitions;
    private Set<String> acceptingStates;

    // Stan wizualizacji
    private String currentWord = "";
    private int currentStep = -1;
    private List<StateTransition> history;

    public DFAVisualizer() {
        super("Automat DFA - dokładnie dwa wystąpienia podciągu \"11\"");
        initializeAutomaton();
        initializeGUI();
        reset();
    }

    private void initializeAutomaton() {
        // Definicja stanów (pozycje)
        states = new HashMap<>();

        for(int i = 0; i < states_names.length; i++)
            states.put(states_names[i], new Point(150+i*150, 200));

        // Definicja przejść
        transitions = new HashMap<>();

        transitions.put(new Transition("q0", '0'), "q0");
        transitions.put(new Transition("q0", '1'), "q1");
        transitions.put(new Transition("q1", '0'), "q0");
        transitions.put(new Transition("q1", '1'), "q2");
        transitions.put(new Transition("q2", '0'), "q2");
        transitions.put(new Transition("q2", '1'), "q3");
        transitions.put(new Transition("q3", '0'), "q2");
        transitions.put(new Transition("q3", '1'), "q4");
        transitions.put(new Transition("q4", '0'), "q4");
        transitions.put(new Transition("q4", '1'), "q5");
        transitions.put(new Transition("q5", '0'), "q4");
        transitions.put(new Transition("q5", '1'), "q6");
        transitions.put(new Transition("q6", '0'), "q6");
        transitions.put(new Transition("q6", '1'), "q6");

        // Stany akceptujące i odrzucające
        acceptingStates = new HashSet<>(Arrays.asList("q4", "q5"));

        history = new ArrayList<>();
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Panel główny
        setLayout(new BorderLayout(10, 10));

        // Panel górny
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        wordLabel = new JLabel("Słowo: (wprowadź poniżej)");
        wordLabel.setFont(new Font("Arial", Font.BOLD, 16));

        stepLabel = new JLabel("Krok: -");
        stepLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        statusLabel = new JLabel("Status: Gotowy");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        topPanel.add(wordLabel);
        topPanel.add(stepLabel);
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel środkowy
        dfaPanel = new DFAPanel();
        add(dfaPanel, BorderLayout.CENTER);

        // Panel dolny
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel z przyciskami nawigacji
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        prevButton = new JButton("◄ Poprzedni");
        prevButton.setFont(new Font("Arial", Font.BOLD, 14));
        prevButton.addActionListener(e -> previousStep());

        nextButton = new JButton("Następny ►");
        nextButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextButton.addActionListener(e -> nextStep());

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setForeground(Color.RED);
        resetButton.addActionListener(e -> reset());

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(resetButton);

        // Panel z polem tekstowym
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JLabel inputLabel = new JLabel("Wprowadź słowo (0, 1):");
        inputLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        inputField = new JTextField(20);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.addActionListener(e -> loadWord());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if(Character.isISOControl(c))
                    return;
                if(c != '0' && c != '1')
                    e.consume();
            }
        });

        loadButton = new JButton("Załaduj");
        loadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadButton.setBackground(new Color(34, 139, 34));
        loadButton.setForeground(Color.WHITE);
        loadButton.addActionListener(e -> loadWord());

        inputPanel.add(inputLabel);
        inputPanel.add(inputField);
        inputPanel.add(loadButton);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        updateButtons();
    }

    private void loadWord() {
        String word = inputField.getText().trim();

        currentWord = word;
        reset();
        currentStep = 0;

        // Buduj historię
        String state = "q0";
        history.add(new StateTransition(state, null));
        for (char symbol : word.toCharArray()) {
            state = transitions.get(new Transition(state, symbol));
            history.add(new StateTransition(state, symbol));
        }

        wordLabel.setText("Słowo: " + (word.isEmpty() ? "(puste)" : word));
        updateDisplay();
    }

    private void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            updateDisplay();
        }
    }

    private void nextStep() {
        if (currentStep < history.size() - 1) {
            currentStep++;
            updateDisplay();
        }
    }

    private void reset() {
        currentStep = -1;
        history.clear();
        updateDisplay();
    }

    private void updateDisplay() {
        // Aktualizuj etykiety
        String currentState = getCurrentState();

        if (currentStep == -1)
            stepLabel.setText("");
        else
            stepLabel.setText(String.format("Krok: %d/%d - Stan: %s",
                    currentStep,
                    currentWord.length(),
                    currentState));

        // Aktualizuj status
        if (currentStep == -1) {
            statusLabel.setText("Status: ");
            statusLabel.setForeground(Color.BLACK);
            wordLabel.setText("Wprowadź słowo ...");
        }
        else if (currentStep == history.size() - 1 && !currentWord.isEmpty()) {
            if (acceptingStates.contains(currentState)) {
                statusLabel.setText("Status: ZAAKCEPTOWANE");
                statusLabel.setForeground(new Color(34, 139, 34));
            } else {
                statusLabel.setText("Status: ODRZUCONE");
                statusLabel.setForeground(Color.RED);
            }
        } else {
            statusLabel.setText("Status: W trakcie przetwarzania...");
            statusLabel.setForeground(Color.BLACK);
        }

        updateButtons();
        dfaPanel.repaint();
    }

    private void updateButtons() {
        prevButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < history.size() - 1);
    }

    private String getCurrentState() {
        if(currentStep == -1) return "";
        return history.get(currentStep).state;
    }

    private String getProcessedWord() {
        if (currentStep <= 0) return "";
        return currentWord.substring(0, currentStep);
    }

    // Panel do rysowania automatu
    class DFAPanel extends JPanel {
        private final int STATE_RADIUS = 35;
        private final int ARROW_SIZE = 10;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String currentState = getCurrentState();

            // Rysuj przejścia
            drawTransitions(g2d);

            // Rysuj stany
            for (Map.Entry<String, Point> entry : states.entrySet()) {
                String state = entry.getKey();
                Point pos = entry.getValue();

                drawState(g2d, state, pos, state.equals(currentState), (currentStep-1)==currentWord.length()-1);
            }

            // Przetworzony tekst
            if (!currentWord.isEmpty()) {
                drawProcessedText(g2d);
            }
        }

        private void drawState(Graphics2D g2d, String state, Point pos, boolean isCurrent, boolean isLast) {
            // Określ kolor
            Color color;
            if (isCurrent) {
                if (isLast) {
                    if (acceptingStates.contains(state)) {
                        color = new Color(144, 238, 144); // light green
                    } else {
                        color = new Color(255, 182, 193); // light red
                    }
                } else {
                    color = Color.YELLOW;
                }
            } else {
                color = Color.WHITE;
            }
            // Rysuj stan
            g2d.setColor(color);
            g2d.fillOval(pos.x - STATE_RADIUS, pos.y - STATE_RADIUS,
                    STATE_RADIUS * 2, STATE_RADIUS * 2);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(pos.x - STATE_RADIUS, pos.y - STATE_RADIUS,
                    STATE_RADIUS * 2, STATE_RADIUS * 2);

            // Podwójne kółko dla stanów akceptujących
            if (acceptingStates.contains(state)) {
                g2d.drawOval(pos.x - STATE_RADIUS + 5, pos.y - STATE_RADIUS + 5,
                        (STATE_RADIUS - 5) * 2, (STATE_RADIUS - 5) * 2);
            }

            // Nazwa stanu
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(state);
            int textHeight = fm.getAscent();
            g2d.drawString(state, pos.x - textWidth / 2, pos.y + textHeight / 2 - 2);
        }

        private void drawTransitions(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));

            // Pętle własne
            for (String state : Arrays.asList("q0", "q2", "q4"))
                drawSelfLoop(g2d, state, "0", 90);
            drawSelfLoop(g2d, "q6", "0,1", 270);

            // Przejścia między stanami

            drawArrowBetween(g2d, "q0", "q1", "1");
            drawArrowBetween(g2d, "q1", "q0", "0");
            drawArrowBetween(g2d, "q1", "q2", "1");
            drawArrowBetween(g2d, "q2", "q3", "1");
            drawArrowBetween(g2d, "q3", "q2", "0");
            drawArrowBetween(g2d, "q3", "q4", "1");
            drawArrowBetween(g2d, "q4", "q5", "1");
            drawArrowBetween(g2d, "q5", "q4", "0");
            drawArrowBetween(g2d, "q5", "q6", "1");
        }

        private void drawSelfLoop(Graphics2D g2d, String state, String label, int angle) {
            Point pos = states.get(state);
            double angleRad = Math.toRadians(angle);

            // punkt środka łuku
            int loopX = (int)(pos.x + Math.cos(angleRad) * 50);
            int loopY = (int)(pos.y + Math.sin(angleRad) * 50);

            // rysowanie pętli
            g2d.drawOval(loopX - 15, loopY - 25, 30, 50);

            // ---- GROTu ----
            // kierunek styczny do okręgu
            double arrowAngle = angleRad + 2.7;

            // punkt gdzie ma być grot
            int arrowX = (int)(loopX - 28 * Math.cos(arrowAngle));
            int arrowY = (int)(loopY + 18 * Math.sin(arrowAngle));

            drawArrowHead(g2d, arrowX, arrowY, arrowAngle, Color.BLACK);

            // Etykieta
            int labelX = (int)(loopX + Math.cos(angleRad) * 40);
            int labelY = (int)(loopY + Math.sin(angleRad) * 40);

            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(labelX - labelWidth/2 - 2, labelY - fm.getAscent() - 2,
                    labelWidth + 4, fm.getHeight() + 4);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, labelX - labelWidth/2, labelY);
        }

        private void drawArrowBetween(Graphics2D g2d, String from, String to,
                                      String label) {
            int curve = -30;
            Point p1 = states.get(from);
            Point p2 = states.get(to);

            double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
            double px = -Math.sin(angle);
            double py =  Math.cos(angle);

            int offset = -20;
            int x1 = (int)(p1.x + Math.cos(angle) * STATE_RADIUS + px * offset);
            int y1 = (int)(p1.y + Math.sin(angle) * STATE_RADIUS + py * offset);
            int x2 = (int)(p2.x - Math.cos(angle) * STATE_RADIUS + px * offset);
            int y2 = (int)(p2.y - Math.sin(angle) * STATE_RADIUS + py * offset);

            // Rysuj zakrzywioną linię
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;

            double perpAngle = angle + Math.PI / 2;
            int ctrlX = (int)(midX + curve * Math.cos(perpAngle));
            int ctrlY = (int)(midY + curve * Math.sin(perpAngle));

            QuadCurve2D curve2d = new QuadCurve2D.Float(x1, y1, ctrlX, ctrlY, x2, y2);
            g2d.draw(curve2d);

            // Grot strzałki
            double endAngle = Math.atan2(y2 - ctrlY, x2 - ctrlX);
            drawArrowHead(g2d, x2, y2, endAngle, Color.BLACK);

            // Etykieta
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(ctrlX - labelWidth/2 - 2, ctrlY - fm.getAscent() - 2,
                    labelWidth + 4, fm.getHeight() + 4);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, ctrlX - labelWidth/2, ctrlY);
        }

        private void drawArrowHead(Graphics2D g2d, int x, int y, double angle, Color color) {
            int[] xPoints = {
                    x,
                    (int)(x - ARROW_SIZE * Math.cos(angle - 0.3)),
                    (int)(x - ARROW_SIZE * Math.cos(angle + 0.3))
            };
            int[] yPoints = {
                    y,
                    (int)(y - ARROW_SIZE * Math.sin(angle - 0.3)),
                    (int)(y - ARROW_SIZE * Math.sin(angle + 0.3))
            };

            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        private void drawProcessedText(Graphics2D g2d) {
            int y = 30;
            int x = 20;

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Przetworzono: ", x, y);

            x += g2d.getFontMetrics().stringWidth("Przetworzono: ");

            if(currentStep == -1) return;
            String processed = getProcessedWord();
            String remaining = currentWord.substring(processed.length());

            // Przetworzony fragment (zielony)
            g2d.setColor(new Color(34, 139, 34));
            g2d.drawString(processed, x, y);

            x += g2d.getFontMetrics().stringWidth(processed);

            // Pozostały fragment (szary)
            g2d.setColor(Color.GRAY);
            g2d.drawString(remaining, x, y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DFAVisualizer visualizer = new DFAVisualizer();
            visualizer.setVisible(true);
            visualizer.setResizable(false);
        });
    }
}