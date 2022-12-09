package HW6;
import java.io.*;
import java.security.*;
import java.util.*;
import java.nio.file.*;

public class Dispatcher {
    String filepath;
    int numCPU;
    int time;
    UnHash[] workerQueue;
    LinkedList<String> workQueue;

    public Dispatcher(String file, int numCPU, int time){
        this.filepath = file;
        this.numCPU = numCPU;
        this.time = time;
        workerQueue = new UnHash[numCPU];
        workQueue = new LinkedList<String>();
        // Create numCPU workers 
        for(int i = 0; i < numCPU; i++){
            UnHash worker = new UnHash();
            workerQueue[i] = worker;
            worker.timeout = time;
            worker.start();
        }

    }
    public void dispatch() {
        File f = new File(filepath);

        try (BufferedReader in = new BufferedReader(new FileReader(f)))
        {
            // Get the work that needs to be done
            String line;
            while((line = in.readLine()) != null){
                workQueue.add(line);
            }
            while (!workQueue.isEmpty()){
                for (int i=0; i<workerQueue.length; i++){
                    if (workerQueue[i].idle()){ 
                        String currWork = workQueue.remove();
                        workerQueue[i].addWork(currWork);
                    }
                    if (workQueue.isEmpty()){
                        break;
                    }

                }
            }
            for (int i=0; i<workerQueue.length; i++){
                workerQueue[i].removeWork();
            } 

        } catch (FileNotFoundException e) {
            System.err.println("Input file does not exist.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Unable to open input file for read operation.");
            e.printStackTrace();
        }

        // Distribute work to worker threads

    }
 
    public static void main(String args[]) throws NoSuchAlgorithmException {
        String filepath = args[0];
        int numCPU = Integer.parseInt(args[1]);
        int time = 0;
        if(args.length == 3){
            time = Integer.parseInt(args[2]);
        }

        Dispatcher dispatcher = new Dispatcher(filepath, numCPU, time);
        dispatcher.dispatch();
    }
    
}
