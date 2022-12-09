package HW1;

import java.util.LinkedList;

public class Timeline {
    LinkedList<Event> timeline = new LinkedList<Event>();

    public Timeline(){
        this.timeline = timeline;
    }

    public void addToTimeline(Event evtToAdd){
        if (this.timeline.size() == 0){
            this.timeline.add(evtToAdd);
            return;
        }
        if (evtToAdd.timestamp > this.timeline.getLast().timestamp){
            this.timeline.addLast(evtToAdd);
            return;
        }

        int i = 0;
        while(i < this.timeline.size() & (evtToAdd.timestamp > this.timeline.get(i).timestamp)){
            i++;
        }
        if( i == this.timeline.size()){
            this.timeline.addLast(evtToAdd);
            return;
        }
        else{
            this.timeline.add(i,evtToAdd);
            return;
        }

    }
    public Event popNext(){
        return this.timeline.pollFirst();
    }
}
