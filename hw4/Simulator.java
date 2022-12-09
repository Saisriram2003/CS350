package HW1;

public class Simulator {
    public static double time;

    public static double arrivalX;
    public static double arrivalY;

    public static double S0serviceX;
    public static double S0serviceY;
    public static double S1service;
    public static double S2service;

    public static double probability;
    public static double ps0s1x;
    public static double ps0s1y;
    public static double ps1s0;
    public static double ps2s0;

    public static double K;



    public static String policy;

    public static void main(String args[]){
        // Total Simulation time
        time = Double.parseDouble(args[0]);

        // Arrival lambda 
        arrivalX = Double.parseDouble(args[1]);
        arrivalY = Double.parseDouble(args[2]);

        //Service time
        S0serviceX = Double.parseDouble(args[3]);
        S0serviceY = Double.parseDouble(args[4]);

        // Policy 
        policy = args[5];

        // Probability of S0 to S1 for X
        ps0s1x = Double.parseDouble(args[6]);

        // Probability of S0 to S1 for Y
        ps0s1y = Double.parseDouble(args[7]);

        //Service time
        S1service = Double.parseDouble(args[8]);
        S2service = Double.parseDouble(args[9]);

        // Queue size limit for S2
        K = Integer.parseInt(args[10]);

        // Probability of S1 to S0 
        ps1s0 = Double.parseDouble(args[11]);

        // Probability of S2 to S0
        ps2s0 = Double.parseDouble(args[12]);

        simulate(time);

    }
    public static void simulate(double time){
        int numX = 0;
        int numY = 0;

        double xCompleted = 0;
        double yCompleted = 0;

        double numMonitors = 0;

        double S0totalBusyTime = 0;
        double S1totalBusyTime = 0;
        double S2totalBusyTime = 0;

        double totalResponseTimeX = 0;
        double totalResponseTimeY = 0;

        double S0totalWaitTime = 0;
        double S1totalWaitTime = 0;
        double S2totalWaitTime = 0;

        double S0queueLength = 0;
        double S1queueLength = 0;
        double S2queueLength = 0;

        double totalWaitTimeX = 0;
        double totalWaitTimeY = 0;

        int numDropped = 0;

        // Initializing generator,timeline, and a request queue and counters
        Exp generator = new Exp();
        Timeline events = new Timeline();
        Server server0 = new Server(policy);
        Server server1 = new Server("FIFO");
        Server server2 = new Server("FIFO");

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
                    req.length = generator.getExp(1/S0serviceX);
                    birthTime = currentTime + (generator.getExp(arrivalX));
                    numX++;
                }
                else{
                    req = new Request('Y',numY);
                    req.length = generator.getExp(1/S0serviceY);
                    birthTime = currentTime + (generator.getExp(arrivalY));
                    numY++;
                }
                
                req.s0_original_arrival_time = currentTime;
                req.s0_arrival_time = currentTime;
                req.server = 0;
                server0.addRequest(req);

                // Print out arrival time of request
                System.out.println("" + req.type + req.id + " ARR: " + currentTime);

                e = new Event('B',birthTime);
                e.reqType = req.type;
                events.addToTimeline(e);

                //If first event - start and create death event
                if(server0.onlyItem()){
                    // Start the req
                    req.s0_start_time = currentTime;
                    server0.startReq(req);
                    System.out.println("" + req.type + req.id + " START " + "S0: " + req.s0_start_time);
                    // Create death event for that req
                    e = new Event('D',currentTime + req.length);
                    e.request = req;
                    e.reqType = req.type;
		            events.addToTimeline(e);
                }
                
            }
            if(currEvent.type == 'D'){
                // Generate random number between 0 and 1
                double rnd = Math.random();
                // Get finished request
                // Pop request from server
                req = currEvent.request;
                // System.out.println("The request I want to finish off is " + req.type+ req.id + " from Server " + req.server);

                if(req.server == 0){
                    // System.out.println("This is the top of my Server 0 queue " + server0.top().type +  server0.top().id);
                    server0.removeRequest();
                }
                else if(req.server == 1){
                    // System.out.println("This is the top of my Server 1 queue " + server1.top().type +  server1.top().id);
                    server1.removeRequest();
                }
                else if(req.server == 2){
                    // System.out.println("This is the top of my Server 2 queue " + server2.top().type +  server2.top().id);
                    server2.removeRequest();
                }

                // Death event for Server 0 req
                if(req.server == 0){
                    S0totalBusyTime += req.length;
                    System.out.println("" + req.type + req.id + " DONE S0: " + currentTime);
                    if(req.type == 'X'){
                        // Go to Server 1
                        if(rnd <= ps0s1x){
                            System.out.println("" + req.type + req.id + " FROM S0 TO S1: " + currentTime);
                            req.server = 1;
                            req.length = generator.getExp(1/S1service);
                            // Check if original arrival has not been already set
                            if (req.s1_original_arrival_time == -1){
                                req.s1_original_arrival_time = currentTime;
                            }
                            req.s1_arrival_time = currentTime;
                            server1.addRequest(req);
                        }
                        // Go to Server 2
                        else{
                            System.out.println("" + req.type + req.id + " FROM S0 TO S2: " + currentTime);
                            if(server2.size() < K){
                                req.server = 2;
                                req.length = generator.getExp(1/S2service);
                                // Check if original arrival has not been already set
                                if (req.s2_original_arrival_time == -1){
                                    req.s2_original_arrival_time = currentTime;
                                }
                                req.s2_arrival_time = currentTime;
                                server2.addRequest(req);
                            }
                            else{
                                numDropped += 1;
                                System.out.println("" + req.type + req.id + " DROP S2: " + currentTime);
                            }
                        }
                    }
                    else if(req.type == 'Y'){
                        // Go to Server 1
                        if(rnd <= ps0s1y){
                            System.out.println("" + req.type + req.id + " FROM S0 TO S1: " + currentTime);

                            req.server = 1;
                            req.length = generator.getExp(1/S1service);
                            // Check if original arrival has not been already set
                            if (req.s1_original_arrival_time == -1){
                                req.s1_original_arrival_time = currentTime;
                            }
                            req.s1_arrival_time = currentTime;
                            server1.addRequest(req);
                        }
                        // Go to Server 2
                        else{
                            System.out.println("" + req.type + req.id + " FROM S0 TO S2: " + currentTime);
                            if(server2.size() < K){
                                req.server = 2;
                                req.length = generator.getExp(1/S2service);
                                // Check if original arrival has not been already set
                                if (req.s2_original_arrival_time == -1){
                                    req.s2_original_arrival_time = currentTime;
                                }
                                req.s2_arrival_time = currentTime;

                                server2.addRequest(req);
                            }
                            else{
                                // We already removed the request from server 0 and now we don't add it back to server2 so it's gone
                                numDropped += 1;
                                System.out.println("" + req.type + req.id + " DROP S2: " + currentTime);
                            }
                        }
                    }
                }

                // Death event for Server 1
                else if(req.server == 1){
                    S1totalBusyTime += (req.length);

                    if(rnd <= ps1s0){
                        // Add it back to S0
                        System.out.println("" + req.type + req.id + " FROM S1 TO S0: " + currentTime);
                        if(req.type == 'X'){
                            req.length = generator.getExp(1/S0serviceX);
                        }
                        else{
                            req.length = generator.getExp(1/S0serviceY);
                        }
                        req.server = 0;
                        req.s0_arrival_time = currentTime;

                        server0.addRequest(req);
                    }
                    else{
                        // Finish request
                        req.s1_finish_time = currentTime;
                        // S1totalResponseTime += (req.s1_finish_time - req.s1_original_arrival_time);
                        
                        if(req.type == 'X'){
                            xCompleted++;
                            totalResponseTimeX += (req.s1_finish_time-req.s0_original_arrival_time);
                        }
                        else if(req.type == 'Y'){
                            yCompleted++;
                            totalResponseTimeY += (req.s1_finish_time-req.s0_original_arrival_time);
                        }
                        System.out.println("" + req.type + req.id + " FROM S1 TO OUT: " + req.s1_finish_time);
                    }
                }
                // Death event for Server 2
                else if(req.server == 2){
                    S2totalBusyTime += (req.length);

                    if(rnd <= ps2s0){
                        // Add it back to S0
                        System.out.println("" + req.type + req.id + " FROM S2 TO S0: " + currentTime);
                        if(req.type == 'X'){
                            req.length = generator.getExp(1/S0serviceX);
                        }
                        else{
                            req.length = generator.getExp(1/S0serviceY);
                        }
                        req.s0_arrival_time = currentTime;
                        req.server = 0;

                        server0.addRequest(req);
                    }
                    else{
                        // Finish request
                        req.s2_finish_time = currentTime;
                        // S2totalResponseTime += (req.s2_finish_time - req.s2_original_arrival_time);

                        if(req.type == 'X'){
                            xCompleted++;
                            totalResponseTimeX += (req.s2_finish_time-req.s0_original_arrival_time);
                        }
                        else if(req.type == 'Y'){
                            yCompleted++;
                            totalResponseTimeY += (req.s2_finish_time-req.s0_original_arrival_time);
                        }
                        System.out.println("" + req.type + req.id + " FROM S2 TO OUT: " + req.s2_finish_time);
                    }
                }

                // Start requests in servers
                if(!server0.IsEmpty() & !server0.hasRunning()){
                    // Start the next request
                    Request next = server0.top();
                    next.s0_start_time = currentTime;
                    server0.startReq(next);

                    System.out.println("" + next.type + next.id + " START " + "S0: " + next.s0_start_time);
                    S0totalWaitTime += (next.s0_start_time-next.s0_arrival_time);
                    if(next.type == 'X'){
                        totalWaitTimeX += (next.s0_start_time-next.s0_arrival_time);
                    }
                    else if(next.type == 'Y'){
                        totalWaitTimeY += (next.s0_start_time-next.s0_arrival_time);
                    }
                    e = new Event('D',currentTime + next.length);
                    e.reqType = next.type;
                    e.request = next;
                    events.addToTimeline(e);
                }

                if(!server1.IsEmpty() & !server1.hasRunning()){
                    // Start the next request
                    Request next = server1.top();
                    next.s1_start_time = currentTime;
                    server1.startReq(next);

                    System.out.println("" + next.type + next.id + " START " + "S1: " + next.s1_start_time);
                    S1totalWaitTime += (next.s1_start_time-next.s1_arrival_time);
                    if(next.type == 'X'){
                        totalWaitTimeX += (next.s1_start_time-next.s1_arrival_time);
                    }
                    else if(next.type == 'Y'){
                        totalWaitTimeY += (next.s1_start_time-next.s1_arrival_time);
                    }

                    e = new Event('D',currentTime + next.length);
                    e.reqType = next.type;
                    e.request = next;
                    events.addToTimeline(e);
                }
                if(!server2.IsEmpty() & !server2.hasRunning()){
                    // Start the next request
                    Request next = server2.top();
                    next.s2_start_time = currentTime;
                    server2.startReq(next);

                    System.out.println("" + next.type + next.id + " START " + "S2: " + next.s2_start_time);
                    S2totalWaitTime += (next.s2_start_time-next.s2_arrival_time);
                    if(next.type == 'X'){
                        totalWaitTimeX += (next.s2_start_time-next.s2_arrival_time);
                    }
                    else if(next.type == 'Y'){
                        totalWaitTimeY += (next.s2_start_time-next.s2_arrival_time);
                    }

                    e = new Event('D',currentTime + next.length);
                    e.reqType = next.type;
                    e.request = next;
                    events.addToTimeline(e);
                }

            }

            if(currEvent.type == 'M'){
                // Update state varibales
                if (server0.size() > 1){
                    S0queueLength += server0.waitSize();
                }
                if (server1.size() > 1){
                    S1queueLength += server1.waitSize();
                }
                if (server2.size() > 1){
                    S2queueLength += server2.waitSize();
                }
                numMonitors += 1;
                // Push a new monitor event
                e = new Event('M',currentTime + generator.getExp(arrivalX));
		        events.addToTimeline(e);
            }
        }
        double S0util = ((S0totalBusyTime)/time);
        double S1util = ((S1totalBusyTime)/time);
        double S2util = ((S2totalBusyTime)/time);

        double S0qavg = ((S0totalBusyTime)/time) + ((S0queueLength/numMonitors));
        double S1qavg = ((S1util) + ((S1queueLength)/numMonitors));
        double S2qavg = ((S2util) + ((S2queueLength)/numMonitors));


        // Server 0

        // Avg Utilization = Avg time spent processing / Total Time
        System.out.println("S0 UTIL: " + S0util);
        // Avg number of services waiting + Avg number of services being processed
        // System.out.println("QAVG "+ (((queueLength/numMonitors) + ((totalResponseTime-totalWaitTime)/time))));
        System.out.println("S0 QAVG: " + S0qavg);
        // Avg services waiting in Queue
        System.out.println("S0 WAVG: " + ((S0queueLength)/numMonitors));

        // Server 1

        // System.out.println("S1 UTIL: " + ((S1totalResponseTime-S1totalWaitTime)/time));
        System.out.println("S1 UTIL: " + S1util);

        System.out.println("S1 QAVG: " + S1qavg);

        System.out.println("S1 WAVG: " + ((S1queueLength)/numMonitors));

        // Server 2

        System.out.println("S2 UTIL: " + S2util);

        System.out.println("S2 QAVG: " + S2qavg);

        System.out.println("S2 WAVG: " + ((S2queueLength)/numMonitors));
        
        System.out.println("S2 DROPPED: " + numDropped);

        // Avg Response time and Wait time for classes
        System.out.println("TRESP X: "+ (totalResponseTimeX/xCompleted));
        System.out.println("TWAIT X: "+ (totalWaitTimeX/xCompleted));
        System.out.println("TRESP Y: "+ (totalResponseTimeY/yCompleted));
        System.out.println("TWAIT Y: "+ (totalWaitTimeY/yCompleted));

        // QAVG for entire system
        System.out.println("QAVG: "+ (S0qavg + S1qavg + S2qavg));
    }
}

