package intersectionBehaviour_v0.perceptions;

import agent.VehicleAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.List;

public class MonitoringTerminationBehaviour extends CyclicBehaviour {

    private List<AID> external_agents;
    private VehicleAgent agent_state;
    private final Object lock_agentState;
    private final Object lock_extAgents;
    private final long pauseTime = 100; // Tempo di pausa tra un controllo e l'altro (in millisecondi)


    public MonitoringTerminationBehaviour(List<AID> external_agents, VehicleAgent agent_state, Object lock_agentState, Object lock_extAgents){
        this.lock_agentState = lock_agentState;
        this.lock_extAgents = lock_extAgents;
        synchronized (this.lock_agentState){
            this.agent_state = agent_state;
        }
        synchronized (this.lock_extAgents){
            this.external_agents = external_agents;
        }
    }
    @Override
    public void action() {
        //Creazione della descrizione del servizio da cercare
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("termination-service");
        template.addServices(serviceDescription);
        synchronized (lock_agentState) {
            //Ricerca degli agenti che offrono il servizio
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                for (DFAgentDescription dfd : result) {
                    AID agentAID = dfd.getName();
                    synchronized (lock_extAgents) {
                        if (external_agents.remove(agentAID)) {
                          //  System.out.println("Agente " + myAgent.getName() + " ha rimosso l'agente" + agentAID.getName());
                        }
                    }
                    if (agent_state.getMap().deleteAgentByAID(agentAID)) {
                        //System.out.println("Agente " + myAgent.getName() + " ha rimosso correttamente dalla mappa l'agente " + agentAID.getName());
                    }
                }
                //Pausa tra una ricerca e l'altra
                // Metti in pausa l'agente per un certo periodo di tempo
                Thread.sleep(pauseTime);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
