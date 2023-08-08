package agent;

import jade.core.AID;

import java.io.Serializable;

public class MovementIntention implements Serializable {

    private int nextStepX;
    private int nextStepY;
    private AID aid;
    private Integer priority;

    public MovementIntention(int nextStepX, int nextStepY, AID aid, Integer priority){
        this.nextStepX = nextStepX;
        this.nextStepY = nextStepY;
        this.aid = aid;
        this.priority = priority;
    }

    public int getNextStepX(){ return nextStepX; }

    public int getNextStepY(){ return  nextStepY; }

    public Integer getPriority() { return  priority; }

    public AID getAid() { return aid; }
}
