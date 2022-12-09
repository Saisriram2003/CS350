package HW1;
import java.util.LinkedList;

public class LoadGenerator {
    double lambda;
    double T;
    Timeline events;
    public LoadGenerator(double lambda, double time, Timeline t){
        this.lambda = lambda;
        this.T = time;
        this.events = t;
    }
    public static void main(String args[]){
        // Getting inputs from command line
        double lambdaA = Double.parseDouble(args[0]);
        double lambdaB = Double.parseDouble(args[1]);
        double T = Integer.parseInt(args[2]);

        //Initializing Timeline
        Timeline events = new Timeline();
        Event eventA = new Event('A', 0);
        Event eventB = new Event('B', 0);
        events.addToTimeline(eventA);
        events.addToTimeline(eventB);

        //Initializing generator for A and generator for B
        LoadGenerator generatorA = new LoadGenerator(lambdaA,T,events);
        LoadGenerator generatorB = new LoadGenerator(lambdaB,T,events);

        //Looping through events and adding them to timeline until time reaches T
        while(events.timeline.size() > 0){
            Event curr = events.popNext();

            if (curr.type == 'A'){
                generatorA.releaseRequest(curr);
            }
            else if(curr.type == 'B'){
                generatorB.releaseRequest(curr);
            }
        }
    }
    //Counters for A and B
    int countA = 0;
    int countB = 0;

    void releaseRequest(Event evt){
        if (evt.type == 'A'){
            System.out.println("A" + countA + ": " + evt.timestamp);
            countA++;
        }
        else if(evt.type == 'B'){
            System.out.println("B" + countB + ": " + evt.timestamp);
            countB++;
        }
        Exp exp = new Exp();
        double inter = exp.getExp(this.lambda);
        double newTime = evt.timestamp + inter;
        if(newTime > this.T){
            return;
        }
        Event event = new Event(evt.type,newTime);
        this.events.addToTimeline(event);
    }
    
}
