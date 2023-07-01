package concept;

import agent.Position;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jade.content.Concept;
import agent.VehicleAgent;
import jade.core.AID;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Intersection implements Concept, Serializable {

    // Dimensione verticale della mappa
    private int dimension_height;
    // Dimensione orizzontale della mappa
    private int dimension_width;

    /* Rappresentazione della mappa attraverso una mappa
       nella quale la chiave sono le coordinate bi-dimensionali
       che identificano un punto specifico e il valore identifica
       la presenza del veicolo o null in quel punto
     */
    @JsonIgnore
    private HashMap<Coordinate, Position> intersection_map;

    public Intersection() {
        this.dimension_width = 100;
        this.dimension_height = 100;
        this.intersection_map = new HashMap<>();
    }

    /*
    Inizializzo la mappa con la sue dimensioni, siamo in due dimensioni
     */
    public Intersection(int dimension_height, int dimension_width) {
        this.dimension_height = dimension_height;
        this.dimension_width = dimension_width;
        this.intersection_map = new HashMap<>();
    }

    /*
    Mi restituisce le coordinate di un agente, se è presente
     */
    public Coordinate findAgent(Position agent) {
        synchronized (intersection_map) {
            for (Map.Entry<Coordinate, Position> entry : intersection_map.entrySet()) {
                if (entry.getValue().equals(agent)) {
                    return entry.getKey();
                }
            }
            return null; // Agente non trovato
        }
    }

    public Position findAgentByAID(AID agentAID) {
        synchronized (intersection_map) {
            Coordinate cord_selected = null;
            for (Coordinate cord : this.intersection_map.keySet()) {
                Position state_extAgent = this.intersection_map.get(cord);
                if (state_extAgent != null) {
                    if (state_extAgent.getAid().equals(agentAID)) {
                        cord_selected = cord;
                    }
                }
            }
            return this.intersection_map.get(cord_selected);
        }
    }

    /*
    Aggiungi un agente alla mappa
     */
    public boolean addAgent(Position v, Coordinate c) {
        synchronized (intersection_map) {
            if (this.intersection_map.containsKey(v) || v == null || c == null) return false;
            return this.intersection_map.put(c, v) != null;
        }
    }

    /*
    Restituisce la lista di tutti gli agenti presenti nella mappa, altrimenti lista vuota
     */
    public List<Position> getAllAgents() {
        synchronized (intersection_map) {
            List<Position> temp = new ArrayList<>();
            Iterator<Map.Entry<Coordinate, Position>> iterator = intersection_map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Coordinate, Position> entry = iterator.next();
                if (entry.getValue() != null) {
                    temp.add(entry.getValue());
                }
            }
            return temp;
        }
    }


    /*public static Hashtable<Coordinate, List<VehicleAgent>> getIntersection_map() {
        return (Hashtable<Coordinate, List<VehicleAgent>>) intersection_map;
    }*/

    public int getDimension_height() {
        return dimension_height;
    }

    public int getDimension_width() {
        return dimension_width;
    }

    public void setDimension_height(int dimension_height) {
        this.dimension_height = dimension_height;
    }

    public void setDimension_width(int dimension_width) {
        this.dimension_width = dimension_width;
    }

    public void setIntersection_map(Intersection map) {
        this.intersection_map = intersection_map;
    }

    // Modifica la posizione di un veicolo all'interno della mappa
    public boolean modifyPosition(Position v, Coordinate c_new) {
        synchronized (intersection_map) {
            if (v == null || c_new == null) return false;
            //System.out.println(intersection_map.get(v.getCurrentlyCoordinate()) + "  " + findAgent(v));

            Position x = intersection_map.remove(v.getCurrentlyCoordinate());
            //System.out.println(intersection_map.containsValue(v) + "  " + intersection_map.size());

            intersection_map.remove(findAgent(v));
            intersection_map.put(c_new, v);
            return true;
        }
    }

    public boolean deleteAgent(VehicleAgent v, Coordinate c) {
        if (v == null || c == null) return false;
        synchronized (intersection_map) {
            return intersection_map.remove(c) != null;
        }
    }

    // Ottieni informazioni riguardo a una specifica coordinata
    public Position getInfoCoordinate(Coordinate c) {
        synchronized (intersection_map) {
            if (c != null) return intersection_map.get(c);
            return null;
        }
    }

    public boolean EnableMovement(Coordinate new_c) {
        // la nuova posizione dell'agente sarebbe fuori dalla mappa
        if (new_c.getX() < 0 || new_c.getX() > dimension_height ||
                new_c.getY() < 0 || new_c.getY() > dimension_width) {
            return false;
        }
        synchronized (intersection_map) {
            Position temp = intersection_map.get(new_c);
            //in questa posizione c'è un agente
            return temp == null;
        }
    }

    public List<Coordinate> getAllCordinates() {
        List<Coordinate> temp = new ArrayList<>();
        for (int i = 0; i < dimension_height; i++) {
            for (int j = 0; j < dimension_width; j++) {
                temp.add(new Coordinate(i, j));
            }
        }
        return temp;
    }

    public boolean deleteAgentByAID(AID agentAID) {
        synchronized (intersection_map) {
            Coordinate cord_selected = null;
            for (Coordinate cord : this.intersection_map.keySet()) {
                Position state_extAgent = this.intersection_map.get(cord);
                if (state_extAgent != null) {
                    if (state_extAgent.getAid().equals(agentAID)) {
                        cord_selected = cord;
                    }
                }
            }
            return this.intersection_map.remove(cord_selected) != null;
        }
    }

}