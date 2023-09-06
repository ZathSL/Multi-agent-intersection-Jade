import agent.Position;
import concept.Coordinate;
import concept.Intersection;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Main extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int MAP_SIZE = 10;
    private static final int AGENT_SIZE = 30;
    private static final int UPDATE_INTERVAL = 100; // Intervallo di aggiornamento (in millisecondi)

    private List<Position> agentPositions; // Posizioni degli agenti sulla mappa
    private static Intersection environment; // Riferimento all'oggetto Intersection

    public Main(Intersection environment) {
        agentPositions = new ArrayList<>();
        synchronized (environment) {
            Main.environment = environment;
        }

        setPreferredSize(new java.awt.Dimension(MAP_SIZE * AGENT_SIZE, MAP_SIZE * AGENT_SIZE));

        // Creazione del timer per l'aggiornamento periodico dell'interfaccia grafica
        Timer timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (environment) {
                    updateAgentPositions(); // Aggiornamento delle posizioni degli agenti
                    repaint(); // Ridisegna l'interfaccia grafica
                }
            }
        });
        timer.start();
    }

    // Aggiorna le posizioni degli agenti
    private void updateAgentPositions() {
        agentPositions.clear();
        agentPositions.addAll(environment.getAllAgents());// Aggiorna la lista delle posizioni degli agenti

    }

    // Disegna l'interfaccia grafica
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Disegna i quadrati della mappa
        int mapWidth = getWidth();
        int mapHeight = getHeight();
        int squareSize = Math.min(mapWidth, mapHeight) / MAP_SIZE;
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                int x = i * squareSize;
                int y = j * squareSize;
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x, y, squareSize, squareSize);
            }
        }

        // Disegna gli agenti sulla mappa
        for (Position agentPosition : agentPositions) {
            int x = agentPosition.getCurrentlyPositionX() * squareSize;
            int y = agentPosition.getCurrentlyPositionY() * squareSize;

            // Disegna il pallino
            g.setColor(Color.RED);
            g.fillOval(x, y, squareSize, squareSize);

            // Disegna il nome dell'agente al centro del pallino
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String agentName = agentPosition.getAid().getName().substring(0, 9);
            int nameX = x + squareSize / 2 - g.getFontMetrics().stringWidth(agentName) / 2;
            int nameY = y + squareSize / 2 + g.getFontMetrics().getHeight() / 3;
            g.drawString(agentName, nameX, nameY);
        }
    }

    public static void main(String[] args) {
        // Avvio del runtime Jade
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "false"); // Mostra la GUI
        ContainerController container = runtime.createMainContainer(profile);

        // Creazione dell'ambiente
        int dimension_height = MAP_SIZE;
        int dimension_width = MAP_SIZE;
        environment = new Intersection(dimension_height, dimension_width);

        // Avvio periodico degli agenti
        int numAgents = 4;
        long delay = 0;
        for (int i = 0; i < numAgents; i++) {
            // Crea un agente e specifica la classe dell'agente
            String agentClassName = "agent.VehicleAgent";
            String agentName = "Agent-" + i;

            try {
                Random random = new Random();
                Coordinate cd_start = new Coordinate(random.nextInt(dimension_height), random.nextInt(dimension_width));
                Coordinate cd_end = new Coordinate(random.nextInt(dimension_height), random.nextInt(dimension_width));

                if(i == 0){
                    cd_start = new Coordinate(1,1);
                    cd_end = new Coordinate(1,2);
                }
                if(i == 1){
                    cd_start = new Coordinate(1,3);
                    cd_end = new Coordinate(1,2);
                }

                Object[] agentArgs = new Object[]{environment, cd_start, cd_end, i};
                AgentController agentController = container.createNewAgent(agentName, agentClassName, agentArgs);

                agentController.start();

                Thread.sleep(delay);
            } catch (StaleProxyException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Creazione dell'interfaccia grafica
        JFrame frame = new JFrame("Agent Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Main(environment));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}