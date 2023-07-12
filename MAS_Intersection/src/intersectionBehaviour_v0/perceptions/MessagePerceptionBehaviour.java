package intersectionBehaviour_v0.perceptions;

import agent.Position;
import agent.VehicleAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concept.Coordinate;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
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
            ACLMessage msg = stateAgent.receive(); //Ricevi i messaggi dagli altri agenti
            if (msg != null) {
                // Elabora il messaggio
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    //Provo a de-serializzare l'oggetto VehicleAgent
                    Position senderAgent = null;
                    try {
                        String json_received =  msg.getContent();
                        //senderAgent = objectMapper.readValue(json_received,VehicleAgent.class);
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
                                if(!this.externalAgent.contains(senderAgent.getAid()))this.externalAgent.add(senderAgent.getAid());
                            }
                        }
                    }

                }
                /*//Il messaggio è di tipo REQUEST (mi stanno informando che stanno uscendo dall'intersezione)
                else if (msg.getPerformative() == ACLMessage.REQUEST) {
                    //System.out.println("Richiesta ricevuta: " + msg.getContent());
                    //Elabora la richiesta
                    try {
                        VehicleAgent temp = (VehicleAgent) msg.getContentObject();
                        if (temp != null) {
                            stateAgent.getMap().deleteAgent(temp, temp.getFinalPosition());
                            synchronized (lock_extAgents) {
                                this.externalAgent.remove(temp.getAID());
                            }

                            //Invia la risposta
                            ACLMessage response = new ACLMessage(ACLMessage.CONFIRM);
                            response.addReceiver(msg.getSender());
                            response.setContent("Received message about termination");

                            myAgent.send(response);
                        }
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }

                }

                 */
            } else {
                block(); //blocca il comportamento fino all'arrivo di un nuovo messaggio
            }
        }
    }
}

