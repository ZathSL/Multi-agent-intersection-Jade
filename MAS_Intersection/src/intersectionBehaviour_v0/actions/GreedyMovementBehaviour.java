package intersectionBehaviour_v0.actions;

import agent.MovementIntention;
import agent.Position;
import agent.VehicleAgent;
import concept.Coordinate;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.*;


/*
    Eseguo i passi verso la meta con un algoritmo A*
     */
public class GreedyMovementBehaviour extends CyclicBehaviour {

    private List<Coordinate> path = null;
    private VehicleAgent stateAgent;
    private List<AID> externalAgent;
    private final Object lock_agentState;
    private final Object lock_extAgents;

    public GreedyMovementBehaviour(VehicleAgent stateAgent, List<AID> externalAgent, Object lock_agentState, Object lock_extAgents) {
        this.lock_agentState = lock_agentState;
        this.lock_extAgents = lock_extAgents;
        synchronized (this.lock_agentState) {
            this.stateAgent = stateAgent;
        }
        synchronized (this.lock_extAgents) {
            this.externalAgent = externalAgent;
        }
    }

    @Override
    public void action() {
        synchronized (lock_agentState) {
            if (path == null || path.isEmpty()) {
                //Calcola il percorso utilizzando l'algoritmo di Dijkstra
                path = dijkstra(stateAgent.getInitPosition(), stateAgent.getFinalPosition());
            }
            if (path.size() > 1) {
                //Ottieni il prossimo passo del percorso calcolato
                Coordinate nextStep = path.get(1);
                //System.out.println(stateAgent.getName()+ " vuole compiere il passo verso "+nextStep+ " il problema è che c'è "+this.stateAgent.getMap().getInfoCoordinate(nextStep));

                // il movimento è permesso in quanto in quella posizione non vi è alcun agente
                if (stateAgent.getMap().EnableMovement(nextStep)) {
                    stateAgent.setIntention(nextStep);
                    //Invio l'intenzione di movimento a tutti gli altri agenti
                    ACLMessage messageRequest = new ACLMessage(ACLMessage.REQUEST);

                    MovementIntention movementIntention = new MovementIntention(nextStep.getX(), nextStep.getY(), stateAgent.getAID(), stateAgent.getPriority());

                    try {
                        messageRequest.setContentObject(movementIntention);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    for (AID aid_agent : externalAgent) {
                        messageRequest.addReceiver(aid_agent);
                    }

                    myAgent.send(messageRequest);
                    //Attendo tre secondi per dare il tempo agli altri agenti di inviarmi le risposte
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (lock_extAgents) {
                        boolean flag = true;
                        MessageTemplate template = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
                        List<ACLMessage> msg_list = stateAgent.receive(template, externalAgent.size());
                        if (msg_list != null) {
                            if (msg_list.size() < externalAgent.size()) {
                                System.out.println(stateAgent.getName() + "dice: Non ho ricevuto qualche conferma");
                                flag = false;
                            } else {
                                for (ACLMessage msg : msg_list) {
                                    if (msg != null && msg.getPerformative() == ACLMessage.REFUSE) {
                                        flag = false;
                                        System.out.println("L'agente "+msg.getSender().getName()+" ha mandato una refuse a "+ stateAgent.getName());
                                    }
                                }
                            }
                            //Nessun agente ha le mie stesse intenzioni oppure ho maggiore priorità
                            if (flag) {
                                //Creazione delle nuove informazioni da mandare come mio stato interno agli altri agenti
                                Position positionAgent = new Position(stateAgent.getInitPosition().getX(), stateAgent.getInitPosition().getY(),
                                        stateAgent.getFinalPosition().getX(), stateAgent.getFinalPosition().getY(), stateAgent.getCurrentlyPosition().getX(),
                                        stateAgent.getCurrentlyPosition().getY(), stateAgent.getAID());

                                stateAgent.getMap().modifyPosition(positionAgent, nextStep);
                                stateAgent.setCurrentlyPosition(nextStep);

                                synchronized (lock_extAgents) {
                                    myAgent.addBehaviour(new StateSendingBehaviour(stateAgent, externalAgent, lock_agentState, lock_extAgents));
                                }
                                path.remove(0);
                            }else{
                                path = dijkstra(stateAgent.getCurrentlyPosition(), stateAgent.getFinalPosition());
                            }
                        } else if(externalAgent.size() == 0){
                            Position positionAgent = new Position(stateAgent.getInitPosition().getX(), stateAgent.getInitPosition().getY(),
                                    stateAgent.getFinalPosition().getX(), stateAgent.getFinalPosition().getY(), stateAgent.getCurrentlyPosition().getX(),
                                    stateAgent.getCurrentlyPosition().getY(), stateAgent.getAID());
                            stateAgent.getMap().modifyPosition(positionAgent, nextStep);
                            stateAgent.setCurrentlyPosition(nextStep);
                            path.remove(0);
                        }
                    }
                } else {
                    path = dijkstra(stateAgent.getCurrentlyPosition(), stateAgent.getFinalPosition());
                    if(stateAgent.getMap().getInfoCoordinate(nextStep).getAid().getName()!=null)System.out.println("L'agente " + myAgent.getName() + " non può compiere il passo da " + stateAgent.getCurrentlyPosition() + " a " + nextStep + " per la presenza dell'agente " + stateAgent.getMap().getInfoCoordinate(nextStep).getAid().getName());

                }

                //path.remove(0);

            } else {
                if (this.stateAgent.getMap().getInfoCoordinate(stateAgent.getFinalPosition()) != null && stateAgent.getCurrentlyPosition().equals(stateAgent.getFinalPosition())) {
                    myAgent.addBehaviour(new TerminationAgentBehaviour(lock_agentState, stateAgent));
                } else {
                    path = dijkstra(stateAgent.getCurrentlyPosition(), stateAgent.getFinalPosition());
                }
            }
        }

    }

    public List<Coordinate> dijkstra(Coordinate source, Coordinate destination) {
        // Distanza minima per raggiungere ogni coordinata
        Map<Coordinate, Integer> distance = new HashMap<>();

        // Coordinata precedente per ricostruire il percorso
        Map<Coordinate, Coordinate> previous = new HashMap<>();

        // Coda con priorità per le coordinate
        PriorityQueue<Coordinate> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        // Inizializzazione
        for (Coordinate coord : stateAgent.getMap().getAllCordinates()) {
            distance.put(coord, Integer.MAX_VALUE);
            previous.put(coord, null);
        }

        distance.put(source, 0);
        queue.add(source);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();

            if (current.equals(destination)) {
                if (stateAgent.getFinalPosition().equals(stateAgent.getCurrentlyPosition())) {
                    if (myAgent.getName().contains("Agent-0"))
                        System.out.println("L'agente " + myAgent.getName() + " è arrivato a destinazione " + stateAgent.getFinalPosition());
                    myAgent.addBehaviour(new TerminationAgentBehaviour(lock_agentState, stateAgent));

                }
                break; // Arrivati alla destinazione, interrompiamo l'algoritmo
            }


            // Scansione dei vicini
            for (Coordinate neighbor : getNeighbors(current)) {
                int alt = distance.get(current);
                if (!stateAgent.getMap().EnableMovement(neighbor)) {
                    //System.out.println("Agente: "+myAgent.getName()+"NON è CONSENTITO" + stateAgent.getMap().getInfoCoordinate(neighbor).getAid().getName());
                    //System.out.println(stateAgent.getMap().getAllAgents().size());
                    alt = Integer.MAX_VALUE;
                } else alt += manhattanDistance(current, neighbor);
                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor, alt);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        // Ricostruzione del percorso
        this.path = new ArrayList<>();
        Coordinate current = destination;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }

    private List<Coordinate> getNeighbors(Coordinate coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        List<Coordinate> neighbors = new ArrayList<>();

        // Coordinata nord
        if (x > 0) {
            neighbors.add(new Coordinate(x - 1, y));
        }
        // Coordinata sud
        if (x < stateAgent.getMap().getDimension_height() - 1) {
            neighbors.add(new Coordinate(x + 1, y));
        }
        // Coordinata ovest
        if (y > 0) {
            neighbors.add(new Coordinate(x, y - 1));
        }
        // Coordinata est
        if (y < stateAgent.getMap().getDimension_width() - 1) {
            neighbors.add(new Coordinate(x, y + 1));
        }

        return neighbors;
    }


    private int manhattanDistance(Coordinate source, Coordinate destination) {
        return Math.abs(source.getX() - destination.getX()) + Math.abs(source.getY() - destination.getY());
    }
}
