package wumpus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;



public class Ag2 extends JFrame {
    private static final long serialVersionUID = 1L;
    private int tamanho;
    private char[][] world;
    private char[][] initialWorld; // Para armazenar o estado inicial do mundo
    private int[] posicaoAgente = {0, 0};
    private int[] posicaoOuro;
    private boolean ouroColetado = false;
    private boolean flechaDisparada = false;
    private boolean wumpusMorto = false;
    private JTextArea outputArea;
    private JPanel worldPanel;
    private int movimentos = 0;
    private int mortesPorPoco = 0;
    private int mortesPeloWumpus = 0;
    private Timer timer;
    private boolean jogoPausado = false;
    private long tempoInicial;
    private int pontuacao = 0; // Variável para armazenar a pontuação

    // Memória do agente para armazenar percepções
    private Map<String, String> percepcoesMemoria = new HashMap<>();

    public Ag2() {
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
                "Digite o tamanho da matriz", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int linhas = Integer.parseInt(linhasField.getText());
                int colunas = Integer.parseInt(colunasField.getText());
                this.tamanho = Math.max(Math.min(linhas, 10), 2); // Limita o tamanho entre 2 e 10
                this.world = new char[this.tamanho][this.tamanho];
                this.initialWorld = new char[this.tamanho][this.tamanho]; // Inicializa o array para o estado inicial
                this.posicaoOuro = new int[]{this.tamanho - 1, this.tamanho - 1}; // Inicializa o ouro na posição (tamanho-1, tamanho-1)
                initializeWorld();
                saveInitialWorldState();
                createAndShowGUI();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Por favor, insira números válidos para as linhas e colunas.");
                showInputDialog();
            }
        } else {
            // Se o usuário cancelar, fecha o programa
            System.exit(0);
        }
    }

    private void initializeWorld() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                world[i][j] = ' ';
            }
        }

        placeOuro();
        placeWumpus();
        placePits();
    }

    private void saveInitialWorldState() {
        for (int i = 0; i < tamanho; i++) {
            System.arraycopy(world[i], 0, initialWorld[i], 0, tamanho);
        }
    }

    private void restoreInitialWorldState() {
        for (int i = 0; i < tamanho; i++) {
            System.arraycopy(initialWorld[i], 0, world[i], 0, tamanho);
        }
    }

    private void placeOuro() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(tamanho);
            y = rand.nextInt(tamanho);
        } while ((x == 0 && y == 0) || world[x][y] != ' ');

        posicaoOuro = new int[]{x, y};
        world[x][y] = 'O';
    }

    private void placeWumpus() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(tamanho);
            y = rand.nextInt(tamanho);
        } while ((x == 0 && y == 0) || world[x][y] != ' ');

        world[x][y] = 'W';
    }

    private void placePits() {
        Random rand = new Random();
        int numPits = tamanho - 1;
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
        restartButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this, "Deseja gerar um novo mundo?", "Reiniciar", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                reiniciarJogo(true);
            } else {
                reiniciarJogo(false);
            }
        });
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

    private void showPerceptions() {
        String[] perceptions = getPerceptions();
        for (String perception : perceptions) {
            outputArea.append(perception + "\n");
        }
    }

    public String[] getPerceptions() {
        int x = posicaoAgente[0];
        int y = posicaoAgente[1];
        ArrayList<String> perceptions = new ArrayList<>();

        // Verifica se o agente foi morto pelo Wumpus
        if (world[x][y] == 'W' && !wumpusMorto) {
            perceptions.add("Você foi comido pelo Wumpus! Levou o farelo!");
            mortesPeloWumpus++;
            pontuacao -= 300; // Penalidade por morrer pelo Wumpus
            resetAgente(true);
        } 
        // Verifica se o agente caiu em um poço
        else if (world[x][y] == 'P') {
            perceptions.add("Você caiu em um poço!!! Morreu jogador!!!");
            mortesPorPoco++;
            pontuacao -= 200; // Penalidade por cair no poço
            resetAgente(true);
        } 
        // Verifica se o agente encontrou o ouro
        else if (world[x][y] == 'O') {
            perceptions.add("Você encontrou o ouro!!! Parabéns! Agora volte ao início.");
            ouroColetado = true;
            world[x][y] = ' '; // Remove o ouro do mundo
            pontuacao += 500; // Pontuação por coletar o ouro
        } 
        // Adiciona as percepções de brisa e fedor
        else {
            if ((x > 0 && world[x - 1][y] == 'P') || (x < tamanho - 1 && world[x + 1][y] == 'P') ||
                (y > 0 && world[x][y - 1] == 'P') || (y < tamanho - 1 && world[x][y + 1] == 'P')) {
                perceptions.add("Brisa");
            }
            if ((x > 0 && world[x - 1][y] == 'W') || (x < tamanho - 1 && world[x + 1][y] == 'W') ||
                (y > 0 && world[x][y - 1] == 'W') || (y < tamanho - 1 && world[x][y + 1] == 'W')) {
                perceptions.add("Fedor");
            }
        }

        // Memorizando as percepções
        for (String perception : perceptions) {
            String key = x + "," + y;
            percepcoesMemoria.put(key, perception);
            
        }

        return perceptions.toArray(new String[0]);
    }

    
    private void resetAgente(boolean mantemMemoria) {
        if (ouroColetado) {
            ouroColetado = false;
            world[posicaoOuro[0]][posicaoOuro[1]] = 'O'; // Reposiciona o ouro
        }
        posicaoAgente = new int[]{0, 0};
        movimentos = 0;
        updateWorldPanel();
        outputArea.append("\nReiniciando agente na posição inicial...\n\n");

        if (!mantemMemoria) {
            percepcoesMemoria.clear(); // Limpa a memória de percepções se necessário
        }
    }

    private void iniciarJogoAutonomo() {
        tempoInicial = System.currentTimeMillis();
        jogoPausado = false;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!jogoPausado) {
                    moverAgente();
                    if (posicaoAgente[0] == 0 && posicaoAgente[1] == 0 && ouroColetado) {
                        long tempoDecorrido = (System.currentTimeMillis() - tempoInicial) / 1000;
                        pontuacao += (int) (1000 - tempoDecorrido); // Pontuação bônus por tempo rápido
                        outputArea.append("Parabéns! Você retornou com o ouro.\nTempo: " + tempoDecorrido + " segundos\nPontuação: " + pontuacao + "\n");
                        gerarRelatorio(tempoDecorrido);
                        timer.stop();
                    }
                }
            }
        });
        timer.start();
    }

    private void pausarJogo() {
        jogoPausado = true;
    }

    private void continuarJogo() {
        jogoPausado = false;
    }

    private void reiniciarJogo(boolean novoMundo) {
        ouroColetado = false;
        flechaDisparada = false;
        wumpusMorto = false;
        movimentos = 0;
        mortesPorPoco = 0;
        mortesPeloWumpus = 0;
        pontuacao = 0;
        percepcoesMemoria.clear();
        if (novoMundo) {
            initializeWorld();
            saveInitialWorldState();
        } else {
            restoreInitialWorldState();
        }
        posicaoAgente = new int[]{0, 0};
        updateWorldPanel();
        outputArea.setText("");
        outputArea.append("Jogo reiniciado.\n\n");
    }

    private void moverAgente() {
        if (ouroColetado) {
            // Movimentação para voltar ao início
            if (posicaoAgente[0] > 0) {
                posicaoAgente[0]--;
            } else if (posicaoAgente[1] > 0) {
                posicaoAgente[1]--;
            }
        } else {
            // Movimentação em busca do ouro
            Random rand = new Random();
            int direcao = rand.nextInt(4);
            switch (direcao) {
                case 0: // Mover para cima
                    if (posicaoAgente[0] > 0) {
                        posicaoAgente[0]--;
                    }
                    break;
                case 1: // Mover para baixo
                    if (posicaoAgente[0] < tamanho - 1) {
                        posicaoAgente[0]++;
                    }
                    break;
                case 2: // Mover para a esquerda
                    if (posicaoAgente[1] > 0) {
                        posicaoAgente[1]--;
                    }
                    break;
                case 3: // Mover para a direita
                    if (posicaoAgente[1] < tamanho - 1) {
                        posicaoAgente[1]++;
                    }
                    break;
            }
        }
        movimentos++;
        pontuacao -= 1; // Penalidade por cada movimento
        updateWorldPanel();
        showPerceptions();
        getPerceptions();

        // Atirar flecha se sentir fedor e o Wumpus ainda estiver vivo
        if (!flechaDisparada && !wumpusMorto && percepcoesMemoria.containsValue("Fedor")) {
            atirarFlecha();
        }
    }

    private void atirarFlecha() {
        outputArea.append("Você atirou uma flecha!\n");
        flechaDisparada = true;

        // Verificar se o Wumpus está em uma posição adjacente
        int x = posicaoAgente[0];
        int y = posicaoAgente[1];
        if ((x > 0 && world[x - 1][y] == 'W') || (x < tamanho - 1 && world[x + 1][y] == 'W') ||
                (y > 0 && world[x][y - 1] == 'W') || (y < tamanho - 1 && world[x][y + 1] == 'W')) {
            outputArea.append("Você matou o Wumpus!\n");
            wumpusMorto = true;
            pontuacao += 300; // Pontuação por matar o Wumpus
            // Remover o Wumpus do mundo
            for (int i = 0; i < tamanho; i++) {
                for (int j = 0; j < tamanho; j++) {
                    if (world[i][j] == 'W') {
                        world[i][j] = ' ';
                        break;
                    }
                }
            }
            updateWorldPanel();
        } else {
            outputArea.append("Você errou!\n");
        }
    }

    private void gerarRelatorio(long tempoDecorrido) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("\n\n==== Relatório de Jogo ====\n");
        relatorio.append("Tempo decorrido: ").append(tempoDecorrido).append(" segundos\n");
        relatorio.append("Movimentos: ").append(movimentos).append("\n");
        relatorio.append("Mortes por poço: ").append(mortesPorPoco).append("\n");
        relatorio.append("Mortes pelo Wumpus: ").append(mortesPeloWumpus).append("\n");
        relatorio.append("Ouro coletado: ").append(ouroColetado ? "Sim" : "Não").append("\n");
        relatorio.append("Flecha disparada: ").append(flechaDisparada ? "Sim" : "Não").append("\n");
        relatorio.append("Wumpus morto: ").append(wumpusMorto ? "Sim" : "Não").append("\n");
        relatorio.append("Pontuação final: ").append(pontuacao).append("\n");
        relatorio.append("===========================\n");

        outputArea.append(relatorio.toString());
        outputArea.append("Clique em Reiniciar para jogar novamente.\n\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ag2());
    }
}


