package concept;

import jade.content.Concept;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class Coordinate implements Concept, Serializable {
    private int x;
    private int y;

    public Coordinate() {
        // Costruttore di default necessario per la deserializzazione
    }
    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){ return x; }

    public void setX(int x) { this.x = x;}

    public void setY(int y) { this.y = y;}

    public int getY(){ return y; }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(X: "+x+", Y: "+y+")";
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        x = in.readInt();
        y = in.readInt();
    }
}
