package intersectionBehaviour_v0.actions;

import agent.Position;
import agent.VehicleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

import java.util.List;

/*
    Mando messaggi INFORM riguardo la mia posizione agli altri agenti
     */
public class StateSendingBehaviour extends OneShotBehaviour {

    private VehicleAgent stateAgent;
    private List<AID> externalAgent;
    private final Object lock_agentState;
    private final Object lock_extAgents;

    public StateSendingBehaviour(VehicleAgent stateAgent, List<AID> externalAgent, Object lock_agentState, Object lock_extAgents) {
        this.lock_agentState = lock_agentState;
        this.lock_extAgents = lock_extAgents;
        synchronized (this.lock_agentState) {
            this.stateAgent = stateAgent;
        }
        synchronized (this.lock_extAgents) {
            this.externalAgent = externalAgent;
        }
    }

    /*
    Invio un messaggio riguardo al mio stato interno
     */
    @Override
    public void action() {
            ACLMessage messageInform = new ACLMessage(ACLMessage.INFORM);
            Position x = new Position(stateAgent.getInitPosition().getX(),stateAgent.getInitPosition().getY(),
                    stateAgent.getFinalPosition().getX(),stateAgent.getFinalPosition().getY(),stateAgent.getCurrentlyPosition().getX(),
                    stateAgent.getCurrentlyPosition().getY(), stateAgent.getAID());

            try {
                messageInform.setContentObject(x);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


                for (AID aid_agent : externalAgent) {
                    messageInform.addReceiver(aid_agent);
                }

            myAgent.send(messageInform);
    }
}