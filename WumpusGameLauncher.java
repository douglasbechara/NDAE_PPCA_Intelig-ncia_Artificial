package wumpus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// INTERFACE INICIAR C/ OS BOTÕES.

public class WumpusGameLauncher extends JFrame {
    public WumpusGameLauncher() {
        setTitle("Mundo do Wumpus - Escolha seu Agente");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        JButton btnAg1 = new JButton("Agente 1 - Aleatório");
        JButton btnAg2 = new JButton("Agente 2 - Reativo");
        JButton btnAg3 = new JButton("Agente 3 - Aprendizagem");
        JButton btnExit = new JButton("Sair");

        btnAg1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(Ag1::new);
            }
        });

        btnAg2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(Ag2::new);
            }
        });

        btnAg3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(MundoWumpusWorGenetico::run);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        add(btnAg1);
        add(btnAg2);
        add(btnAg3);
        add(btnExit);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WumpusGameLauncher launcher = new WumpusGameLauncher();
            launcher.setVisible(true);
        });
    }
}
