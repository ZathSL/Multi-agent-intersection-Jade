package intersectionBehaviour_v0.perceptions;

import agent.MovementIntention;
import agent.Position;
import agent.VehicleAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.List;

/*
    Ricevo gli stimoli dall'esterno attraverso messaggi di INFORM da parte di tutti gli altri agenti
     */
public class MessagePerceptionBehaviour extends CyclicBehaviour {

    VehicleAgent stateAgent;
    List<AID> externalAgent;
    private final Object lock_agentState;
    private final Object lock_extAgents;


    public MessagePerceptionBehaviour(VehicleAgent stateAgent, List<AID> externalAgent, Object lock_agentState, Object lock_extAgents) {
        this.lock_agentState = lock_agentState;
        this.lock_extAgents = lock_extAgents;
        synchronized (this.lock_agentState){
            this.stateAgent = stateAgent;
        }
        synchronized (this.lock_extAgents){
            this.externalAgent = externalAgent;
        }
    }

    @Override
    public void action() {
        synchronized (lock_agentState) {
            MessageTemplate template = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            List<ACLMessage> msg_list;
            synchronized (lock_extAgents) {
                msg_list = stateAgent.receive(template, externalAgent.size() * 2);
            }
            if (msg_list != null) {
                for (ACLMessage msg : msg_list) {
                    if (msg != null) {
                        // Elabora il messaggio
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            //Provo a de-serializzare l'oggetto Position
                            Position senderAgent = null;
                            try {
                                senderAgent = (Position) msg.getContentObject();
                            } catch (UnreadableException e) {
                                throw new RuntimeException(e);
                            }
                            //se l'agente esiste allora modifico lo stato interno del mio agente sulla posizione aggiornata
                            if (senderAgent != null) {
                                if (this.stateAgent.getMap().findAgentByAID(msg.getSender()) != null) {
                                    this.stateAgent.getMap().modifyPosition(senderAgent, senderAgent.getCurrentlyCoordinate());
                                } else {
                                    this.stateAgent.getMap().addAgent(senderAgent, senderAgent.getCurrentlyCoordinate());
                                    synchronized (lock_extAgents) {
                                        if (!this.externalAgent.contains(senderAgent.getAid()))
                                            this.externalAgent.add(senderAgent.getAid());
                                    }
                                }
                            }

                        } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                            //E' arrivato un messaggio di intenzione
                            try {
                                MovementIntention movementIntention = (MovementIntention) msg.getContentObject();
                                ACLMessage message = null;
                                //Controllo se l'intenzione è identica alla mia
                                if (movementIntention.getNextStepX() == stateAgent.getIntention().getX() &&
                                        movementIntention.getNextStepY() == stateAgent.getIntention().getY() &&
                                        movementIntention.getPriority() > stateAgent.getPriority()) {
                                    //In questo caso mando una refuse perché ho più priorità
                                    //Il mio valore di priorità è minore (valori più piccoli corrispondono a priorità maggiore)
                                    message = new ACLMessage(ACLMessage.REFUSE);

                                } else {
                                    //In questo caso mando una confirm
                                    //Il mio valore di priorità è maggiore
                                    message = new ACLMessage(ACLMessage.CONFIRM);
                                }
                                message.addReceiver(movementIntention.getAid());
                                myAgent.send(message);
                            } catch (UnreadableException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    } else {
                        block(); //blocca il comportamento fino all'arrivo di un nuovo messaggio
                    }
                }
            }
        }
    }
}

