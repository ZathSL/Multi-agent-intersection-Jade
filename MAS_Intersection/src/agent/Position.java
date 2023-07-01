package agent;


import concept.Coordinate;
import jade.core.AID;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {
    private int initPositionX;
    private int initPositionY;
    private int finalPositionX;
    private int finalPositionY;
    private int currentlyPositionX;
    private int currentlyPositionY;
    private AID aid;

    public Position(int initPositionX, int initPositionY, int finalPositionX, int finalPositionY, int currentlyPositionX, int currentlyPositionY, AID aid) {
        this.initPositionX = initPositionX;
        this.initPositionY = initPositionY;
        this.finalPositionX = finalPositionX;
        this.finalPositionY = finalPositionY;
        this.currentlyPositionX = currentlyPositionX;
        this.currentlyPositionY = currentlyPositionY;
        this.aid = aid;
    }

    public AID getAid(){
        return aid;
    }
    public void setAid(AID aid){
        this.aid = aid;
    }

    public int getInitPositionX() {
        return initPositionX;
    }

    public int getInitPositionY() {
        return initPositionY;
    }

    public int getFinalPositionX() {
        return finalPositionX;
    }

    public int getFinalPositionY() {
        return finalPositionY;
    }

    public int getCurrentlyPositionX() {
        return currentlyPositionX;
    }

    public int getCurrentlyPositionY() {
        return currentlyPositionY;
    }

    public Coordinate getCurrentlyCoordinate(){
        return new Coordinate(this.currentlyPositionX,this.currentlyPositionY);
    }

    public Coordinate getInitCoordinate(){
        return new Coordinate(this.initPositionX,this.initPositionY);
    }

    public Coordinate getFinalCoordinate(){
        return new Coordinate(this.finalPositionX, this.finalPositionY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        return Objects.equals(aid, other.aid);
    }

}