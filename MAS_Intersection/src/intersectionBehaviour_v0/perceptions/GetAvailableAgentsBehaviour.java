package intersectionBehaviour_v0.perceptions;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.List;

public class GetAvailableAgentsBehaviour extends CyclicBehaviour{

    private List<AID> external_agents;
    private final Object lock_agentState;
    private final Object lock_extAgents;
    private final long pauseTime = 100; // Tempo di pausa tra un controllo e l'altro (in millisecondi)


    public GetAvailableAgentsBehaviour(List<AID> external, Object lock_agentState, Object lock_extAgents){
        this.lock_agentState = lock_agentState;
        this.lock_extAgents = lock_extAgents;
        synchronized (this.lock_extAgents){
            this.external_agents = external;
        }
    }
    @Override
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("vehicle-discovery");
        template.addServices(sd);

        try{
            synchronized (lock_agentState) {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                for (DFAgentDescription dfd : result) {
                    AID agentAID = dfd.getName();
                    if (external_agents != null && !agentAID.equals(myAgent.getAID()) && !external_agents.contains(agentAID)) {
                        synchronized (lock_extAgents) {
                            external_agents.add(agentAID);
                        }
                    }
                }
            }
            //Pausa tra una ricerca e l'altra
            // Metti in pausa l'agente per un certo periodo di tempo
            Thread.sleep(pauseTime);

        }catch (FIPAException fe){
            fe.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

}
