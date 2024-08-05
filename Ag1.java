package wumpus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Ag1 extends JFrame {
    private int tamanho;
    private char[][] world;
    private int[] posicaoAgente = {0, 0};
    private int[] posicaoOuro;
    private boolean ouroColetado = false;
    private boolean temFlecha = true;
    private JTextArea outputArea;
    private JPanel worldPanel;
    private int movimentos = 0;
    private int mortesPorPoco = 0;
    private int mortesPeloWumpus = 0;
    private Timer timer;
    private boolean jogoPausado = false;
    private int scoreMatouWumpus = 0;
    private int scorePegouOuroERetornou = 0;
    private int scoreNovaPosicao = 0;
    private int scoreAtirarFlecha = 0;
    private Set<String> posicoesVisitadas = new HashSet<>();

    public Ag1() {
        showInputDialog();
    }

    private void showInputDialog() {
        JTextField linhasField = new JTextField(5);
        JTextField colunasField = new JTextField(5);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Número de linhas:"));
        myPanel.add(linhasField);
        myPanel.add(Box.createHorizontalStrut(15));
        myPanel.add(new JLabel("Número de colunas:"));
        myPanel.add(colunasField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Digite o tamanho da matriz do Ag. Aleatório", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int linhas = Integer.parseInt(linhasField.getText());
                int colunas = Integer.parseInt(colunasField.getText());
                this.tamanho = Math.max(linhas, colunas);
                this.world = new char[this.tamanho][this.tamanho];
                this.posicaoOuro = new int[]{this.tamanho - 1, this.tamanho - 1};
                inicializacaoMundo();
                createAndShowGUI();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Por favor, insira números válidos para as linhas e colunas.");
                showInputDialog();
            }
        } else {
            System.exit(0);
        }
    }

    private void inicializacaoMundo() {
        Random rand = new Random();
        int xOuro, yOuro;

        do {
            xOuro = rand.nextInt(tamanho);
            yOuro = rand.nextInt(tamanho);
        } while (xOuro == 0 && yOuro == 0);

        posicaoOuro = new int[]{xOuro, yOuro};

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                world[i][j] = ' ';
            }
        }

        world[posicaoOuro[0]][posicaoOuro[1]] = 'O';
        placeWumpus();
        placePits();
    }

    private void placeWumpus() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(tamanho);
            y = rand.nextInt(tamanho);
        } while ((x == 0 && y == 0) || (x == posicaoOuro[0] && y == posicaoOuro[1]));
        world[x][y] = 'W';
    }

    private void placePits() {
        Random rand = new Random();
        int numPits = rand.nextInt(tamanho) + 1;
        int pitsPlaced = 0;
        while (pitsPlaced < numPits) {
            int x = rand.nextInt(tamanho);
            int y = rand.nextInt(tamanho);
            if (world[x][y] == ' ' && (x != 0 || y != 0)) {
                world[x][y] = 'P';
                pitsPlaced++;
            }
        }
    }

    private void createAndShowGUI() {
        setTitle("Mundo do Wumpus");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new BorderLayout());

        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.SOUTH);

        worldPanel = new JPanel(new GridLayout(tamanho, tamanho));
        add(worldPanel, BorderLayout.CENTER);
        updateWorldPanel();

        JPanel controlPanel = new JPanel();
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> iniciarJogoAutonomo());
        JButton pauseButton = new JButton("Pausar");
        pauseButton.addActionListener(e -> pausarJogo());
        JButton continueButton = new JButton("Continuar");
        continueButton.addActionListener(e -> continuarJogo());
        JButton restartButton = new JButton("Reiniciar");
        restartButton.addActionListener(e -> reiniciarJogo());
        JButton exitButton = new JButton("Sair");
        exitButton.addActionListener(e -> System.exit(0));

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(continueButton);
        controlPanel.add(restartButton);
        controlPanel.add(exitButton);
        add(controlPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private void updateWorldPanel() {
        worldPanel.removeAll();
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                if (i == posicaoAgente[0] && j == posicaoAgente[1]) {
                    label.setText("A");
                    label.setOpaque(true);
                    label.setBackground(Color.GREEN);
                } else {
                    switch (world[i][j]) {
                        case 'O':
                            label.setText("O");
                            label.setOpaque(true);
                            label.setBackground(Color.YELLOW);
                            break;
                        case 'W':
                            label.setText("W");
                            label.setOpaque(true);
                            label.setBackground(Color.RED);
                            break;
                        case 'P':
                            label.setText("P");
                            label.setOpaque(true);
                            label.setBackground(Color.BLUE);
                            break;
                        default:
                            label.setText(String.valueOf(world[i][j]));
                            break;
                    }
                }
                worldPanel.add(label);
            }
        }
        worldPanel.revalidate();
        worldPanel.repaint();
    }

    private void iniciarJogoAutonomo() {
        outputArea.setText("");
        timer = new Timer(1000, taskPerformer);
        timer.start();
    }

    private void pausarJogo() {
        jogoPausado = true;
    }

    private void continuarJogo() {
        jogoPausado = false;
    }

    private void reiniciarJogo() {
        if (timer != null) {
            timer.stop();
        }
        ouroColetado = false;
        temFlecha = true;
        movimentos = 0;
        mortesPorPoco = 0;
        mortesPeloWumpus = 0;
        scoreMatouWumpus = 0;
        scorePegouOuroERetornou = 0;
        scoreNovaPosicao = 0;
        scoreAtirarFlecha = 0;
        inicializacaoMundo();
        posicaoAgente = new int[]{0, 0};
        posicoesVisitadas.clear();
        updateWorldPanel();
        outputArea.setText("");
    }

    private void generateReport() {
        int penalidadeMortesPorPoco = mortesPorPoco * 1000;
        int penalidadeMortesPeloWumpus = mortesPeloWumpus * 1000;
        int score = ((scoreMatouWumpus + scorePegouOuroERetornou + scoreNovaPosicao)
                - (movimentos + penalidadeMortesPorPoco + penalidadeMortesPeloWumpus + scoreAtirarFlecha));
        
        String relatorio = "---- RELATÓRIO FINAL ----\n";
        relatorio += "(+) Matar o Wumpus: " + scoreMatouWumpus + "\n";
        relatorio += "(+) Pegar e Retornar com o Ouro: " + scorePegouOuroERetornou + "\n";
        relatorio += "(+) Movimento de posição nova: " + scoreNovaPosicao + "\n";
        relatorio += "(-) Movimentos totais: " + movimentos + "\n";
        relatorio += "(-) Mortes por Poço: " + penalidadeMortesPorPoco + "\n";
        relatorio += "(-) Mortes pelo Wumpus: " + penalidadeMortesPeloWumpus + "\n";
        relatorio += "(-) Atirar flecha: " + scoreAtirarFlecha + "\n";
        relatorio += "---- PONTUAÇÃO FINAL ----: " + score + "\n";
        relatorio += "-------------------------\n";

        outputArea.append(relatorio);
    }

    private final ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            if (!jogoPausado) {
                buscarOuro();
                updateWorldPanel();
                movimentos++;
            }
        }
    };

    private void atirarFlecha(int x, int y) {
        if (temFlecha) {
            temFlecha = false;
            scoreAtirarFlecha -= 10; // Penalidade por atirar a flecha
            if ((x >= 0 && x < tamanho && y >= 0 && y < tamanho) && world[x][y] == 'W') {
                outputArea.append("Você atirou a flecha e acertou: Grito!!! O Wumpus morreu!\n");
                scoreMatouWumpus += 1000;
                world[x][y] = ' ';
            } else {
                outputArea.append("Você atirou a flecha mas não acertou o Wumpus.\n");
            }
        }
    }

    private void decidirEAtirarFlecha() {
        if (!temFlecha) return;

        int x = posicaoAgente[0];
        int y = posicaoAgente[1];

        if (sentiuFedor(x - 1, y)) {
            atirarFlecha(x - 1, y);
        } else if (sentiuFedor(x + 1, y)) {
            atirarFlecha(x + 1, y);
        } else if (sentiuFedor(x, y - 1)) {
            atirarFlecha(x, y - 1);
        } else if (sentiuFedor(x, y + 1)) {
            atirarFlecha(x, y + 1);
        }
    }

    private boolean sentiuFedor(int x, int y) {
        return x >= 0 && x < tamanho && y >= 0 && y < tamanho && world[x][y] == 'W';
    }

    private int[] moverAleatoriamente(int x, int y) {
        ArrayList<int[]> possiveisMovimentos = new ArrayList<>();

        if (x > 0) possiveisMovimentos.add(new int[]{x - 1, y});
        if (x < tamanho - 1) possiveisMovimentos.add(new int[]{x + 1, y});
        if (y > 0) possiveisMovimentos.add(new int[]{x, y - 1});
        if (y < tamanho - 1) possiveisMovimentos.add(new int[]{x, y + 1});

        if (!possiveisMovimentos.isEmpty()) {
            return possiveisMovimentos.get(new Random().nextInt(possiveisMovimentos.size()));
        } else {
            return new int[]{x, y};
        }
    }

    private void buscarOuro() {
        int x = posicaoAgente[0];
        int y = posicaoAgente[1];

        int[] novaPosicao = moverAleatoriamente(x, y);
        moverAgente(novaPosicao[0], novaPosicao[1]);
    }

    private void processarPosicaoAtual() {
        int x = posicaoAgente[0];
        int y = posicaoAgente[1];

        if (posicoesVisitadas.add(x + "," + y)) {
            scoreNovaPosicao += 1;
        }

        if (world[x][y] == 'W') {
            outputArea.append("Você foi comido pelo Wumpus, ");
            mortesPeloWumpus++;
            resetAgente();
        } else if (world[x][y] == 'P') {
            outputArea.append("Você caiu em um poço!!!");
            mortesPorPoco++;
            resetAgente();
        } else if (world[x][y] == 'O' && !ouroColetado) {
            outputArea.append("Brilho!!! Você encontrou o ouro!!\n");
            ouroColetado = true;
            world[x][y] = ' ';
        } else if (ouroColetado && x == 0 && y == 0) {
            outputArea.append("Parabéns! Você retornou com o ouro e venceu o jogo!\n");
            scorePegouOuroERetornou += 1000;
            generateReport();
            timer.stop();
        } else {
            decidirEAtirarFlecha();
        }

        // Mostra as percepções
        String[] perceptions = getPerceptions(x, y);
        for (String perception : perceptions) {
            outputArea.append(perception + "\n");
        }
    }

    private void moverAgente(int x, int y) {
        posicaoAgente[0] = x;
        posicaoAgente[1] = y;
        updateWorldPanel();
        processarPosicaoAtual();
    }

    public String[] getPerceptions(int x, int y) {
        ArrayList<String> perceptions = new ArrayList<>();

        if (world[x][y] == 'W') {
            perceptions.add("Morreu!!");
        } else if (world[x][y] == 'P') {
            perceptions.add(" Morreu!!");
        } else {
            if ((x > 0 && world[x - 1][y] == 'W') || (x < tamanho - 1 && world[x + 1][y] == 'W') ||
                (y > 0 && world[x][y - 1] == 'W') || (y < tamanho - 1 && world[x][y + 1] == 'W')) {
                perceptions.add("Você sentiu um fedor estranho...");
            }
            if ((x > 0 && world[x - 1][y] == 'P') || (x < tamanho - 1 && world[x + 1][y] == 'P') ||
                (y > 0 && world[x][y - 1] == 'P') || (y < tamanho - 1 && world[x][y + 1] == 'P')) {
                perceptions.add("Você sente uma brisa...");
            }
        }

        return perceptions.toArray(new String[0]);
    }

    private void resetAgente() {
        posicaoAgente = new int[]{0, 0};
        if (ouroColetado) {
            world[posicaoOuro[0]][posicaoOuro[1]] = 'O';
            ouroColetado = false;
        }
        posicoesVisitadas.clear();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Ag1::new);
    }
}
