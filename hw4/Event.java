package HW1;

public class Event {
    public char type;
    public double timestamp;
    public char reqType;
    public Request request;
    public Event(char t,double time){
        this.type = t;
        this.timestamp = time;
    }
}
