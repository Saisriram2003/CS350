package HW1;

public class Simulator {
    public static double time;

    public static double arrivalX;
    public static double arrivalY;

    public static double serviceX;
    public static double serviceY;

    public static double probability;

    public static String policy;

    public static void main(String args[]){
        // Total Simulation time
        time = Double.parseDouble(args[0]);

        // Arrival lambda 
        arrivalX = Double.parseDouble(args[1]);
        arrivalY = Double.parseDouble(args[2]);

        //Service time
        serviceX = Double.parseDouble(args[3]);
        serviceY = Double.parseDouble(args[4]);

        // Policy 
        policy = args[5];

        // Probability of loop
        probability = Double.parseDouble(args[6]);
         
        simulate(time);

    }
    public static void simulate(double time){
        int numX = 0;
        int numY = 0;
        double numRuns = 0;

        double xCompleted = 0;
        double yCompleted = 0;
        double completedRequests = 0;

        double numMonitors = 0;

        double totalResponseTime = 0;
        double totalResponseTimeX = 0;
        double totalResponseTimeY = 0;

        double totalWaitTime = 0;
        double totalWaitTimeX = 0;
        double totalWaitTimeY = 0;

        double queueLength = 0;

        // Initializing generator,timeline, and a request queue and counters
        Exp generator = new Exp();
        Timeline events = new Timeline();
        Server server = new Server(policy);
        Event e;
        Request req;

        // Adding first birth and monitor event to timeline
        Event e1 = new Event('B',generator.getExp(arrivalX));
        e1.reqType = 'X';

        Event e2 = new Event('B',generator.getExp(arrivalY));
        e2.reqType = 'Y';

        events.addToTimeline(e1);
        events.addToTimeline(e2);

        Event e3 = new Event('M',generator.getExp(arrivalX));
        events.addToTimeline(e3);

        double currentTime = 0;
        double birthTime;

        while(currentTime < time){
            // Update time and event we're at
            Event currEvent = events.popNext();
            currentTime = currEvent.timestamp;

            if (currEvent.type == 'B'){
                // Create new request based on what type of Req it is
                if (currEvent.reqType == 'X'){
                    req = new Request('X',numX);
                    req.length = generator.getExp(1/serviceX);
                    birthTime = currentTime + (generator.getExp(arrivalX));
                    numX++;
                }
                else{
                    req = new Request('Y',numY);
                    req.length = generator.getExp(1/serviceY);
                    birthTime = currentTime + (generator.getExp(arrivalY));
                    numY++;
                }
                
                req.original_arrival_time = currentTime;
                req.arrival_time = currentTime;
                server.addRequest(req);

                // Print out arrival time of request
                System.out.println("" + req.type + req.id + " ARR: " + currentTime + " LEN: " + req.length);

                e = new Event('B',birthTime);
                e.reqType = req.type;
                events.addToTimeline(e);

                //If first event - start and create death event
                if(server.onlyItem()){
                    // Start the req
                    server.startReq(req);
                    req.start_time = currentTime;
                    System.out.println("" + req.type + req.id + " START: " + req.start_time);
                    // Create death event for that req
                    e = new Event('D',currentTime + req.length);
                    e.reqType = req.type;
		            events.addToTimeline(e);
                }
                
            }
            if(currEvent.type == 'D'){
                // Generate random number between 0 and 1
                double rnd = Math.random();
                req = server.removeRequest();

                // Anything less than that is fine
                if (rnd > probability){
                    // Add it again to Queue
                    System.out.println("" + req.type + req.id + " LOOP: " + currentTime);
                    if(req.type == 'X'){
                        req.length = generator.getExp(1/serviceX);
                    }
                    else{
                        req.length = generator.getExp(1/serviceY);
                    }
                    req.arrival_time = currentTime;
                    req.runs++;
                    server.addRequest(req);
                }
                else{
                // Finish the running request
                req.finish_time = currentTime;
                System.out.println("" + req.type + req.id + " DONE: " + req.finish_time);
                numRuns += req.runs;
                if(req.type == 'X'){
                    xCompleted++;
                    totalResponseTimeX += (req.finish_time-req.original_arrival_time);
                }
                else if(req.type == 'Y'){
                    yCompleted++;
                    totalResponseTimeY += (req.finish_time-req.original_arrival_time);
                }
                completedRequests += 1;

                totalResponseTime += (req.finish_time-req.original_arrival_time);
                }

                // If queue is not empty select next request
                if(!server.IsEmpty()){
                    // Start the next request
                    Request next = server.top();
                    server.startReq(next);
                    next.start_time = currentTime;
                    System.out.println("" + next.type + next.id + " START: " + next.start_time);
                    if(next.type == 'X'){
                        totalWaitTimeX += (next.start_time-next.arrival_time);
                    }
                    else if(next.type == 'Y'){
                        totalWaitTimeY += (next.start_time-next.arrival_time);
                    }
                    totalWaitTime += next.start_time - next.arrival_time;

                    e = new Event('D',currentTime + next.length);
                    e.reqType = next.type;
                    events.addToTimeline(e);
                }

            }
            if(currEvent.type == 'M'){
                // Update state varibales
                // queueLength = queueLength + server.size();
                if (server.size() <= 1){
                    // no req waiting
                }
                else{
                    queueLength += server.waitSize();
                }
                numMonitors += 1;
                // Push a new monitor event
                e = new Event('M',currentTime + generator.getExp(arrivalX));
		        events.addToTimeline(e);
            }
        }
        // Avg Utilization = Avg time spent processing / Total Time

        System.out.println("UTIL: " + ((totalResponseTime-totalWaitTime)/time));
        // Avg number of services waiting + Avg number of services being processed
        // System.out.println("QAVG "+ (((queueLength/numMonitors) + ((totalResponseTime-totalWaitTime)/time))));
        System.out.println("QAVG: "+ ((queueLength/numMonitors) + ((totalResponseTime-totalWaitTime)/time)));
        // Avg services waiting in Queue
        System.out.println("WAVG: "+ (queueLength /numMonitors));

        // Avg Response time and Wait time
        System.out.println("TRESP X: "+ (totalResponseTimeX/xCompleted));
        System.out.println("TWAIT X: "+ (totalWaitTimeX/xCompleted));
        System.out.println("TRESP Y: "+ (totalResponseTimeY/yCompleted));
        System.out.println("TWAIT Y: "+ (totalWaitTimeY/yCompleted));

        // Avg number of runs
        System.out.println("RUNS: "+ (numRuns/completedRequests));

    }
}

