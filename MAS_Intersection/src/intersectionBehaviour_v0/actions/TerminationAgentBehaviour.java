package intersectionBehaviour_v0.actions;

import agent.VehicleAgent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;


public class TerminationAgentBehaviour extends Behaviour {

    private final Object lock_agentState;
    private VehicleAgent agentState;

    public TerminationAgentBehaviour(Object lock_agentState, VehicleAgent agent) {
        this.lock_agentState = lock_agentState;
        synchronized (this.lock_agentState){
            this.agentState = agent;
        }
    }


    public void action() {
        synchronized (lock_agentState) {
            String serviceName = "Termination-"+ myAgent.getAID();

            //Creazione della descrizione dell'agente
            DFAgentDescription agentDescription = new DFAgentDescription();
            agentDescription.setName(myAgent.getAID());

            //Creazione della descrizione del servizio
            ServiceDescription serviceDescription = new ServiceDescription();
            serviceDescription.setName(serviceName);
            serviceDescription.setType("termination-service"); //Tipo del servizio

            //Aggiunta alla descrizione del servizio all'agente
            agentDescription.addServices(serviceDescription);

            //Deregistrazione del servizio precedente (se presente)
            try {
                DFService.deregister(myAgent);
            } catch (FIPAException e) {
                e.printStackTrace();
            }


            //Registrazione del servizio presso il Directory Facilitator (DF)
            try {
                DFService.register(myAgent, agentDescription);
                myAgent.doDelete();
                agentState.getMap().deleteAgentByAID(myAgent.getAID());
                System.out.println("Ho terminato correttamente "+myAgent.getName());

            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean done() {
        myAgent.doDelete();
        return true;
    }
}