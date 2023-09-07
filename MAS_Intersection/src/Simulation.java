import agent.Position;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import concept.Intersection;
import concept.Coordinate;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private static final int MAP_SIZE = 10;

    private static Intersection environment; // Riferimento all'oggetto Intersection

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
                Coordinate cd_start = null;
                Coordinate cd_end = null;

                if (i == 0) {
                    cd_start = new Coordinate(1, 1);
                    cd_end = new Coordinate(1, 2);
                }
                if (i == 1) {
                    cd_start = new Coordinate(1, 3);
                    cd_end = new Coordinate(1, 2);
                }

                if (i == 2) {
                    cd_start = new Coordinate(dimension_height-1, dimension_width-1);
                    cd_end = new Coordinate(random.nextInt(dimension_height), random.nextInt(dimension_width));
                }

                if (i == 3) {
                    cd_start = new Coordinate(dimension_height/2, dimension_width/2);
                    cd_end = new Coordinate(random.nextInt(dimension_height), random.nextInt(dimension_width));
                }

                /* if (i == 2){
                    cd_start = new Coordinate(0,2);
                    cd_end = new Coordinate(1,2);
                }
                if( i == 3){
                    cd_start = new Coordinate(0,3);
                    cd_end = new Coordinate(1,2);
                }*/

                Object[] agentArgs = new Object[]{environment, cd_start, cd_end, i};
                AgentController agentController = container.createNewAgent(agentName, agentClassName, agentArgs);

                agentController.start();

                Thread.sleep(delay);
            } catch (StaleProxyException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Creazione dell'interfaccia grafica
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Agent Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            List<Position> agentPositions = new ArrayList<>();
            MapView mapView = new MapView(agentPositions);
            frame.getContentPane().add(mapView);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Creazione del timer per l'aggiornamento periodico dell'interfaccia grafica
            Timer timer = new Timer(100, e -> {
                synchronized (environment) {
                    List<Position> updatedPositions = environment.getAllAgents();
                    mapView.updateAgentPositions(updatedPositions);
                }
            });
            timer.start();
        });
    }
}
