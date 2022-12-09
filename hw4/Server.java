package HW1;
import java.util.LinkedList;

public class Server {
    LinkedList<Request> FIFOqueue;
    LinkedList<Request> SJNqueue;

    String policy;
    Request running = null;
    double totalQueueLength = 0;
    double totalResponseTime = 0;
    double totalWaitTime = 0;
    int id;

    public Server(String policy) {
        this.policy = policy;
        if(this.policy.equals("FIFO")){
            FIFOqueue = new LinkedList<Request>();
        }
        else if(this.policy.equals("SJN")){ 
            SJNqueue = new LinkedList<Request>();
        }
    }
    public void addRequest(Request req) {
        // If SJN sorted where first is smallest last is largest
        // If FIFO last one
    
        if(this.policy.equals("FIFO")){
            FIFOqueue.addLast(req);
         }
         else if(this.policy.equals("SJN")){
            if (SJNqueue.size() == 0){
                SJNqueue.add(req);
                return;
            }
            if (req.length > SJNqueue.getLast().length){
                SJNqueue.addLast(req);
                return;
            }
    
            int i = 0;
            while(i < SJNqueue.size() & (req.length > SJNqueue.get(i).length)){
                i++;
            }
            if(i == SJNqueue.size()){
               SJNqueue.addLast(req);
                return;
            }
            else{
                SJNqueue.add(i,req);
                return;
            }
         }
    }
    public Request top(){
        if(this.policy.equals("FIFO")){
           return FIFOqueue.peekFirst();
        }
        else{
            // Set the request that is at the top to running
            return SJNqueue.peekFirst();
        }
    }
    public boolean hasRunning(){
        if(this.running != null){
            return true;
        }
        else{
            return false;
        }
    }
    public boolean onlyItem(){
        if(this.policy.equals("FIFO")){
            if(FIFOqueue.size() == 1){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            if(SJNqueue.size() == 1){
                return true;
            }
            else{
                return false;
            }
        }
    }
    public boolean IsEmpty(){
        if(this.policy.equals("FIFO")){
            if(FIFOqueue.size() == 0){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            if(SJNqueue.size() == 0){
                return true;
            }
            else{
                return false;
            }
        }
    }
    public Request removeRequest(){

        if(this.policy.equals("FIFO")){
            Request reqToRemove =  FIFOqueue.pollFirst();
            this.running = null;;
            return reqToRemove;
        }
        else{
            Request reqToRemove = this.running;
            this.running = null;;
            SJNqueue.remove(reqToRemove);
            return reqToRemove;
        }

    }
    
    public void startReq(Request req){
        this.running = req;
    }
    public double waitSize(){
        if(this.policy.equals("FIFO")){
            return FIFOqueue.size() - 1;
        }
        else{
            return SJNqueue.size() - 1;
        }
    }
    public double size(){
        if(this.policy.equals("FIFO")){
            return FIFOqueue.size();
        }
        else{
            return SJNqueue.size();
        }
    }
    public void updateQueueLength(){
        if (this.size() > 1){
            this.totalQueueLength += this.waitSize();
        }
    }

    public void stats(double time,double numMonitors,double numDropped){

    }
}
