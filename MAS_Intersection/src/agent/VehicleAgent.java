package agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intersectionBehaviour_v0.IntersectionBehaviour;
import concept.Coordinate;
import concept.Intersection;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

import java.io.Serializable;
import java.util.Objects;
public class VehicleAgent extends Agent implements Serializable{

    private Coordinate currentlyPosition;
    private Coordinate initPosition;
    private Coordinate finalPosition;
    private Intersection map;
    private Integer priority;
    private Coordinate intention;


    protected void setup(){
        Object[] args = getArguments();
        /*
        L'agente inizializza il suo stato interno quando viene creato
         */
        if(args != null && args.length > 0 ) {
            map = (Intersection) args[0];
            this.initPosition = (Coordinate) args[1];
            this.currentlyPosition = (Coordinate) args[1];
            this.finalPosition = (Coordinate) args[2];
            this.priority = (Integer) args[3];
        }
        this.intention = new Coordinate(-1,-1);

        System.out.println("L'agente "+getName()+" è inizializzato, partendo da:"+this.initPosition+" e deve arrivare a "+finalPosition);

        /*
        L'agente si registra al servizio offerto dal DF per trovare gli altri agenti nell'intersezione
         */
        registerAgent();
        /*
        L'agente assumendo questo comportamento, in parallelo eseguirà la percezione sullo stato degli altri agenti
        presenti nell'intersezione tramite la ricezione di messaggi INFORM dagli altri agenti, inoltre si sposterà
        al passo più conveniente per raggiungere la sua destinazione. Dopo lo spostamento, l'agente invia un
        messaggio che informa del suo spostamento
         */
        addBehaviour(new IntersectionBehaviour(this));
    }

    private void registerAgent() {
        System.out.println("Agent "+getLocalName()+" registering service discovery of type \"vehicle-discovery\"");

        try{
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType("vehicle-discovery");

            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            dfd.addServices(sd);

            DFService.register(this, dfd);

        }catch(FIPAException fe){
            fe.printStackTrace();
        }
    }

    public Intersection getMap(){
        return this.map;
    }

    public void setMap(Intersection map){ this.map = map;}
    public boolean setCurrentlyPosition(Coordinate currentlyPosition) {
        if(currentlyPosition==null)return false;
        this.currentlyPosition = currentlyPosition;
        return true;
    }

    public Coordinate getCurrentlyPosition() {
        return currentlyPosition;
    }

    public Coordinate getInitPosition() {
        return initPosition;
    }

    public Coordinate getFinalPosition() {
        return finalPosition;
    }

    public Integer getPriority(){ return priority; }

    public Coordinate getIntention(){ return intention; }

    public void setIntention(Coordinate nextStep) { intention = nextStep;}

    public void setInitPosition(Coordinate initPosition){ this.initPosition = initPosition;}

    public void setFinalPosition(Coordinate finalPosition){this.finalPosition = finalPosition;}

    public void setPriority(Integer priority) { this.priority = priority; }

    @Override
    public int hashCode() {
        return Objects.hash(getAID());
    }

    // Override equals() method
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        VehicleAgent other = (VehicleAgent) obj;
        return Objects.equals(getAID(), other.getAID());
    }

}