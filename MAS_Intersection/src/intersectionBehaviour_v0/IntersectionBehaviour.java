package intersectionBehaviour_v0;

import agent.VehicleAgent;
import intersectionBehaviour_v0.actions.GreedyMovementBehaviour;
import intersectionBehaviour_v0.actions.StateSendingBehaviour;
import intersectionBehaviour_v0.perceptions.GetAvailableAgentsBehaviour;
import intersectionBehaviour_v0.perceptions.MessagePerceptionBehaviour;
import intersectionBehaviour_v0.perceptions.MonitoringTerminationBehaviour;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;

import java.util.*;


public class IntersectionBehaviour extends ParallelBehaviour {
    // agente che adotta il comportamento
    private VehicleAgent state_agent;
    private List<AID> external_agent;

    private final Object lock_agentState = new Object();
    private final Object lock_extAgents = new Object();

    public IntersectionBehaviour(VehicleAgent agent) {
        this.state_agent = agent;
        this.external_agent = new ArrayList<>();
    }

    public void onStart() {
        addSubBehaviour(new GetAvailableAgentsBehaviour(this.external_agent, lock_agentState, lock_extAgents));
        //Comportamento di percezione dei messaggi
        addSubBehaviour(new MessagePerceptionBehaviour(state_agent, external_agent, lock_agentState, lock_extAgents));
        //Comportamento di percezione di terminazione di agenti
        addSubBehaviour(new MonitoringTerminationBehaviour(external_agent,state_agent, lock_agentState, lock_extAgents));
        //Comportamento di invio dei messaggi
        addSubBehaviour(new StateSendingBehaviour(state_agent, external_agent, lock_agentState, lock_extAgents));
        //Comportamento di esecuzione dell'algoritmo greedy per trovare il path migliore
        addSubBehaviour(new GreedyMovementBehaviour(state_agent, external_agent, lock_agentState, lock_extAgents));


    }

}