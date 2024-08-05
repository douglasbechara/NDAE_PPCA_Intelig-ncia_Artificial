package wumpus;

import java.util.*;

class MundoWumpusWorGenetico {

    // Configurações do algoritmo genético
    static final int POPULATION_SIZE = 100; // Tamanho da população
    static final int GENE_LENGTH = 50; // Comprimento do gene
    static final double MUTATION_RATE = 0.01; // Taxa de mutação
    static final int GENERATIONS = 1000; // Número de gerações
    static final boolean VERBOSE = false; // Flag para exibir a matriz a cada passo

    static final Random random = new Random();

    static class Agent {
        List<Character> genes; // Genes do agente representando os movimentos
        int fitness; // Aptidão do agente

        // Estatísticas para o relatório
        int movimentosTotais;
        int mortesPorPoco;
        int mortesPeloWumpus;
        long tempoDecorrido;

        // Construtor do agente inicializa genes com movimentos aleatórios
        Agent() {
            genes = new ArrayList<>();
            for (int i = 0; i < GENE_LENGTH; i++) {
                genes.add(randomMove());
            }
            fitness = 0; // Inicializa a aptidão
            movimentosTotais = 0;
            mortesPorPoco = 0;
            mortesPeloWumpus = 0;
            tempoDecorrido = 0;
        }

        // Gera um movimento aleatório (U, D, L, R)
        static char randomMove() {
            char[] moves = {'U', 'D', 'L', 'R'};
            return moves[random.nextInt(moves.length)];
        }

        // Calcula a aptidão do agente com base em sua trajetória no mundo
        void calculateFitness(World world) {
            fitness = 0; // Redefine a aptidão
            int x = 0, y = 0; // Posição inicial do agente
            Set<String> visitedPositions = new HashSet<>();
            long startTime = System.currentTimeMillis();

            // Percorre cada movimento do gene do agente
            for (char move : genes) {
                movimentosTotais++;
                fitness -= 1; // Deduz 1 ponto para cada movimento

                switch (move) {
                    case 'U':
                        y++; // Move para cima
                        break;
                    case 'D':
                        y--; // Move para baixo
                        break;
                    case 'L':
                        x--; // Move para a esquerda
                        break;
                    case 'R':
                        x++; // Move para a direita
                        break;
                }

                // Verifica se o agente saiu dos limites do mundo
                if (x < 0 || x >= world.size || y < 0 || y >= world.size) {
                    break; // Fora dos limites, interrompe a execução
                }

                String position = x + "," + y;

                // Verifica se o agente caiu em um buraco ou encontrou o Wumpus
                if (world.pits.contains(position)) {
                    fitness -= 1000; // Penalidade ao cair em um buraco
                    mortesPorPoco++;
                    break;
                }
                if (position.equals(world.wumpus)) {
                    fitness -= 1000; // Penalidade ao encontrar o Wumpus
                    mortesPeloWumpus++;
                    break;
                }

                // Verifica se o agente encontrou o ouro
                if (position.equals(world.gold)) {
                    fitness += 1000; // Agente encontrou o ouro, ganha 1000 pontos
                    break; // Interrompe a execução
                }

                // Verifica se a posição não foi visitada antes
                if (visitedPositions.add(position)) {
                    fitness++; // Incrementa a pontuação em 1 para uma nova posição visitada
                }

                // Imprime a matriz se o modo verboso estiver ativado
                if (VERBOSE) {
                    world.printMatrix(x, y);
                }
            }

            long endTime = System.currentTimeMillis();
            tempoDecorrido = (endTime - startTime) / 1000; // Tempo decorrido em segundos

            this.fitness = fitness; // Define a pontuação do agente
        }
    }

    static class World {
        int size; // Tamanho do mundo
        List<String> pits; // Lista de buracos
        String wumpus; // Posição do Wumpus
        String gold; // Posição do ouro

        // Construtor do mundo inicializa o tamanho e reseta o mundo
        World(int size) {
            this.size = size;
            reset();
        }

        // Reseta o mundo com posições novas para os elementos
        void reset() {
            pits = new ArrayList<>();
            List<String> positions = new ArrayList<>();
            for (int i = 1; i < size; i++) {
                for (int j = 1; j < size; j++) {
                    positions.add(i + "," + j);
                }
            }
            Collections.shuffle(positions);
            
            for (int i = 0; i < 3; i++) {
                pits.add(positions.get(i));
            }
            wumpus = positions.get(3);
            gold = positions.get(4);
        }

        // Método para imprimir a matriz com a posição atual do agente
        void printMatrix(int agentX, int agentY) {
            for (int y = size - 1; y >= 0; y--) {
                for (int x = 0; x < size; x++) {
                    String position = x + "," + y;
                    if (x == agentX && y == agentY) {
                        System.out.print("A "); // Agente
                    } else if (position.equals(gold)) {
                        System.out.print("G "); // Ouro
                    } else if (position.equals(wumpus)) {
                        System.out.print("W "); // Wumpus
                    } else if (pits.contains(position)) {
                        System.out.print("P "); // Buraco
                    } else {
                        System.out.print(". "); // Espaço vazio
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o tamanho da matriz quadrada: ");
        int size = scanner.nextInt(); // Lê o tamanho da matriz do usuário
        World world = new World(size); // Cria o mundo com o tamanho especificado
        List<Agent> population = initializePopulation();

        // Solicita o tamanho do torneio ao usuário
        System.out.print("Digite o tamanho do torneio: ");
        int tournamentSize = scanner.nextInt();

        // Simula o clique no botão para resetar o mundo
        world.reset();

        // Executa o algoritmo genético por um número definido de gerações
        for (int generation = 0; generation < GENERATIONS; generation++) {
            // Calcula a aptidão de cada agente na população
            for (Agent agent : population) {
                agent.calculateFitness(world);
            }

            List<Agent> newPopulation = new ArrayList<>();

            // Cria uma nova população com seleção, crossover e mutação
            while (newPopulation.size() < POPULATION_SIZE) {
                Agent parent1 = selectTournament(population, tournamentSize);
                Agent parent2 = selectTournament(population, tournamentSize);
                Agent[] children = crossover(parent1, parent2);
                newPopulation.add(mutate(children[0]));
                newPopulation.add(mutate(children[1]));
            }

            // Adiciona elitismo
            population.sort(Comparator.comparingInt(a -> -a.fitness));
            newPopulation.add(population.get(0)); // Adiciona o melhor agente da geração anterior

            population = newPopulation;

            // Encontra e imprime a melhor aptidão da geração atual
            int bestFitness = population.stream().mapToInt(agent -> agent.fitness).max().orElse(0);
            System.out.println("Generation " + generation + ", Best Fitness: " + bestFitness);
        }

        // Encontra e imprime o melhor agente ao final de todas as gerações
        Agent bestAgent = Collections.max(population, Comparator.comparingInt(a -> a.fitness));
        System.out.println("Best Agent: " + bestAgent.genes);
        System.out.println("Best Fitness: " + bestAgent.fitness);

        // Gera o relatório final
        generateReport(bestAgent);
    }

    // Inicializa a população com agentes aleatórios
    static List<Agent> initializePopulation() {
        List<Agent> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Agent());
        }
        return population;
    }

    // Seleciona um agente da população usando seleção por torneio
    static Agent selectTournament(List<Agent> population, int tournamentSize) {
        List<Agent> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        return Collections.max(tournament, Comparator.comparingInt(a -> a.fitness));
    }

    // Realiza o crossover entre dois agentes para gerar dois filhos
    static Agent[] crossover(Agent parent1, Agent parent2) {
        Agent child1 = new Agent();
        Agent child2 = new Agent();
        int crossoverPoint = random.nextInt(GENE_LENGTH);

        for (int i = 0; i < GENE_LENGTH; i++) {
            if (i < crossoverPoint) {
                child1.genes.set(i, parent1.genes.get(i));
                child2.genes.set(i, parent2.genes.get(i));
            } else {
                child1.genes.set(i, parent2.genes.get(i));
                child2.genes.set(i, parent1.genes.get(i));
            }
        }

        return new Agent[]{child1, child2};
    }

    // Realiza a mutação em um agente
    static Agent mutate(Agent agent) {
        for (int i = 0; i < GENE_LENGTH; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                agent.genes.set(i, Agent.randomMove());
            }
        }
        return agent;
    }

    // Gera o relatório final com as estatísticas do melhor agente
    static void generateReport(Agent agent) {
        String relatorio = "---- RELATÓRIO FINAL ----\n";
        relatorio += "Movimentos totais: " + agent.movimentosTotais + "\n";
        relatorio += "Mortes por poço: " + agent.mortesPorPoco + "\n";
        relatorio += "Mortes pelo Wumpus: " + agent.mortesPeloWumpus + "\n";
        relatorio += "Tempo decorrido: " + agent.tempoDecorrido + " segundos\n";
        relatorio += "Pontuação final: " + agent.fitness + "\n";
        relatorio += "-------------------------\n";
        System.out.println(relatorio);
    }
}
