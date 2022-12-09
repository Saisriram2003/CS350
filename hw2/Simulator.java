package HW1;

public class Simulator {
    public static double time;
    public static double arrival;
    public static double service;
    public static String policy;

    public static void main(String args[]){
        // Total Simulation time
        time = Double.parseDouble(args[0]);
        // Arrival lambda 
        arrival = Double.parseDouble(args[1]);
        // args[2] for HW3
        //Service time
        service = Double.parseDouble(args[3]);
        //Service Lambda 
         // args[4] for HW3
        policy = args[5];
         

         simulate(time);

    }
    public static void simulate(double time){
        int numArrivals = 0;
        double completedRequests = 0;
        double numMonitors = 0;

        double totalResponseTime = 0;
        double totalWaitTime = 0;
        double totalSystemTime = 0;
        double queueLength = 0;

        // Initializing generator,timeline, and a request queue and counters
        Exp generator = new Exp();
        Timeline events = new Timeline();
        Server server = new Server(policy);
        Event e;
        Request req;

        // Adding first birth and monitor event to timeline
        Event e1 = new Event('B',generator.getExp(arrival));

        events.addToTimeline(e1);
        Event e2 = new Event('M',generator.getExp(arrival));
        events.addToTimeline(e2);

        double currentTime = 0;

        while(currentTime < time){
            // Update time and event we're at
            Event currEvent = events.popNext();
            currentTime = currEvent.timestamp;

            if (currEvent.type == 'B'){
                // Create new Req
                req = new Request(numArrivals);

                // Length of Req is generated
                req.length = generator.getExp(1/service);
                req.arrival_time = currentTime;
                server.addRequest(req);


                System.out.println("X" + req.id + " ARR: " + currentTime + " LEN: " + req.length);
                numArrivals++;

                double birthTime = currentTime + (generator.getExp(arrival));
                e = new Event('B',birthTime);
                events.addToTimeline(e);

                //If first event - start and create death event
                if(server.onlyItem()){
                    // Start the req
                    server.startReq(req);
                    req.start_time = currentTime;
                    System.out.println("X" + req.id + " START: " + req.start_time);
                    // Create death event for that req
                    e = new Event('D',currentTime + req.length);
		            events.addToTimeline(e);
                }
                
            }
            if(currEvent.type == 'D'){
                // Finish the running request
                req = server.removeRequest();
                req.finish_time = currentTime;
                System.out.println("X" + req.id + " DONE: " + req.finish_time);
                completedRequests += 1;

                totalResponseTime += (req.finish_time-req.arrival_time);

                // If queue is not empty select next request
                if(!server.IsEmpty()){
                    // Start the next request
                    Request next = server.top();
                    server.startReq(next);
                    next.start_time = currentTime;
                    System.out.println("X" + next.id + " START: " + currentTime);

                    totalWaitTime += next.start_time - next.arrival_time;

                    e = new Event('D',currentTime + next.length);
                    events.addToTimeline(e);
                }

            }
            if(currEvent.type == 'M'){
                // Update state varibales
                totalSystemTime = totalSystemTime + server.size();
                queueLength = queueLength + server.waitSize();
                numMonitors += 1;
                // Push a new monitor event
                e = new Event('M',currentTime + generator.getExp(arrival));
		        events.addToTimeline(e);
            }
        }
        // Avg Utilization = Avg time spent processing / Total Time

        System.out.println("UTIL: " + ((totalResponseTime-totalWaitTime)/time));
        // Avg number of services waiting + Avg number of services being processed
        // System.out.println("QAVG "+ (((queueLength/numMonitors) + ((totalResponseTime-totalWaitTime)/time))));
        System.out.println("QAVG: "+ ((queueLength/numMonitors) + ((totalResponseTime-totalWaitTime)/time)));
        // Avg services waiting in Queue
        System.out.println("WAVG: "+ (queueLength/numMonitors));

        // Avg Response time and Wait time
        System.out.println("TRESP: "+ (totalResponseTime/completedRequests));
        System.out.println("TWAIT: " + (totalWaitTime/completedRequests)); 

    }
}
