package hw7;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

/***************************************************/
/* CS-350 Fall 2022 - Homework 7 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a super-entity, called Pirate that uses the   */
/*   Dispatcher class multiple times to create     */
/*   two stages of computation. In the first stage */
/*   the system cracks simple hashes, while in the */
/*   second, compound hashes are cracked.          */
/*                                                 */
/***************************************************/

public class Pirate {

    String fileName;
    int numCPUs;
    int timeoutMillis;
	String cipher;
    Dispatcher dispatcher;
	boolean notDone = true;
	// ArrayList<Integer> finalL = new ArrayList<Integer>();
	List<Integer> finalL = new ArrayList<Integer>() {
		public boolean add(Integer mt) {
			int index = Collections.binarySearch(this, mt);
			if (index < 0) index = ~index;
			super.add(index, mt);
			return true;
		}
	};


    /* Queue for inputs to be processed */
    LinkedList<WorkUnit> workQueue;

    /* Queue for processed outputs */
    LinkedList<WorkUnit> resQueue;

    /* Mutex to protect input queue */   
    Semaphore wqMutex;
    
    /* Mutex to protect output queue */
    Semaphore rsMutex;

	Semaphore lock;

    
    public Pirate (String fileName, int N, int timeout,String cipher) {
	this.fileName = fileName;
	this.numCPUs = N;
	this.timeoutMillis = timeout;
	this.cipher = cipher;

	/* Now build the other data structures */
	workQueue = new LinkedList<WorkUnit>();
	resQueue = new LinkedList<WorkUnit>();	

	wqMutex = new Semaphore(1);
	rsMutex = new Semaphore(1);

	
	/* Construct the dispatcher which will also start the worker threads */
        this.dispatcher = new Dispatcher(workQueue, resQueue, wqMutex, rsMutex, N, timeout);	
    }

    private void __initWorkQueue() throws InterruptedException {
        /* The fileName parameter contains the full path to the input file */
        Path inputFile = Paths.get(fileName);

		/* Attempt to open the input file, if it exists */
        if (Files.exists(inputFile)) {

	    /* It appears to exists, so open file */
            File fHandle = inputFile.toFile();
            
            /* Use a buffered reader to be a bit faster on the I/O */
            try (BufferedReader in = new BufferedReader(new FileReader(fHandle)))
            {

                String line;
		
		/* Pass each line read in input to the dehasher */
                while((line = in.readLine()) != null){
					WorkUnit work = new WorkUnit(line);
					dispatcher.addWork(work);
                }
		
            } catch (FileNotFoundException e) {
                System.err.println("Input file does not exist.");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Unable to open input file for read operation.");
                e.printStackTrace();
            }	    
	    
        } else {
            System.err.println("Input file does not exist. Exiting.");        	
        }
	
    }

    private void __prepareCompoundWork() throws InterruptedException {
	/* This function will execute when no pending work exists. But
	 * as it goes along, worker threads might start crunching
	 * data. Hence, copy over all the result so far. */

	List<Integer> L = new ArrayList<Integer>() {
		public boolean add(Integer mt) {
			int index = Collections.binarySearch(this, mt);
			if (index < 0) index = ~index;
			super.add(index, mt);
			return true;
		}
	};
	ArrayList<String> uncracked = new ArrayList<String>();
	
	for (WorkUnit w : resQueue) {
	    String res = w.getResult();
		
	    
	    if (res != null) {
			// if(!w.isSimple()){
			// 	String[] resSplit = res.split(";");
			// 	res = resSplit[1];
			// }
			L.add(Integer.parseInt(res));
			
			/* We might as well print this first round of results already */
			// System.out.println(res);
	    } 
		else {
		uncracked.add(w.getHash());
	    }
	}

	/* Done splitting result -- we can clean up the result queue */
	
	resQueue.clear();


	/* Sort list L of integers */
	// Collections.sort(L);

	// finalL.addAll(L);
	// Go through L and add every element to finalL
	for(int i = 0; i < L.size(); i++){
		finalL.add(L.get(i));
	}

	// System.out.println("L currently is: ");
	// System.out.println(L.toString());

	/* Possibly the worst way of doing this. Generate all the
	 * possible tuples of the form <a, b, hash> to be cracked. The
	 * work queue might explode after this. A work-queue pruning
	 * strategy is required to meet some reasonable timing
	 * constraints. */
	int len = L.size();
	for (int i = 0; i < len-1; ++i) {
	    for (int j = i + 1; j < len; ++j) {
			for (String h : uncracked) {
				WorkUnit work = new WorkUnit(h, L.get(i), L.get(j));
				dispatcher.addWork(work);
			}
	    }
	}
	
    }

    private void __postProcessResult() throws InterruptedException {

		HashMap<String, Boolean> uncrackable = new HashMap<String, Boolean>();
		List<Integer> L = new ArrayList<Integer>() {
			public boolean add(Integer mt) {
				int index = Collections.binarySearch(this, mt);
				if (index < 0) index = ~index;
				super.add(index, mt);
				return true;
			}
		};
		ArrayList<String> unsolved = new ArrayList<String>();

		for (WorkUnit w : resQueue) {
			String res = w.getResult();
			String h = w.getHash();

			/* Add all of them for now */
			uncrackable.put(h, true);
		}
		
		/* Done! Print result */
		for (WorkUnit w : resQueue) {
			String res = w.getResult();
			String h = w.getHash();
			
			if (res != null) {
				String[] resSplit = res.split(";");
				res = resSplit[1];
				L.add(Integer.parseInt(res));

			/* Remove what we know has been cracked*/
			uncrackable.remove(h);
			}
		}
		// Collections.sort(L);

		// System.out.println("L currently is: ");
		// System.out.println(L.toString());
		
		// finalL.addAll(L);
		for(int i = 0; i < L.size(); i++){
			finalL.add(L.get(i));
		}
		resQueue.clear();

		int len = L.size();
		for (String h : uncrackable.keySet()) {
			unsolved.add(h);
		}
		if(unsolved.size() == 0){
			notDone = false;
		}
		for (int i = 0; i < len-1; ++i) {
			for (int j = i + 1; j < len; ++j) {
				for (String h : unsolved) {
					WorkUnit work = new WorkUnit(h, L.get(i), L.get(j));
					dispatcher.addWork(work);
				}
			}
		}



		/* Print the uncrackable hashes */
	
    }

    
    public void findTreasure () throws InterruptedException
    {

	/* Read the input file and initialize the input queue */
	
		__initWorkQueue();
		dispatcher.dispatch();

		notDone = true;

		rsMutex.acquire();	
		/* We have the result. Generate the new work queue to crack compound hashes */
		__prepareCompoundWork();
		rsMutex.release();
			
			// Check if work queue has any work left to do
			// wqMutex.acquire();
			// if (workQueue.isEmpty()) {
			// 	done = true;
			// }
			// wqMutex.release();


			// rsMutex.acquire();	
			// /* Check if done */
			// done = true;
			// for (WorkUnit w : resQueue) {
			// 	if (w.getResult() == null) {
			// 		done = false;
			// 		break;
			// 	}
			// }
			// rsMutex.release();

		// }
		while(notDone){
			dispatcher.dispatch();

			rsMutex.acquire();
			__postProcessResult();
			rsMutex.release();
		}

		/* Done! Terminate the dispatcher (and any worker thread with that) */
		dispatcher.terminate();
		// Collections.sort(finalL);
		// System.out.println("Final L is: ");
		// System.out.println(finalL.toString());
		// form the result by using the values in finalL as indexes in cipher
		String result = "";
		for(int i = 0; i < finalL.size(); i++){
			result += this.cipher.charAt(finalL.get(i));
		}
		System.out.println(result);

    }

	public static byte[] convertFile(File file) {
        try {

            FileInputStream fs = new FileInputStream(file);
            byte[] arr = new byte[(int)file.length()];
            fs.read(arr);
            fs.close();
  
            return arr;
        } catch (IOException e){
            byte[] arr = new byte[2];
            return arr;
        }
    }

    /* Entry point of the code */
    public static void main(String[] args) throws InterruptedException
    {
		/* Read path of input file */       
			String inputFile = args[0];

		/* Read number of available CPUs */
		int N = Integer.parseInt(args[1]);

		/* If it exists, read in timeout, default to 10 seconds otherwise */
		int timeoutMillis = 100000;
		if (args.length > 2) {
			timeoutMillis = Integer.parseInt(args[2]);
		}
		File f = new File(args[3]);
		byte[] dec = convertFile(f);
		String cipher = new String(dec);

	

		/* Construct the dispatcher with all the necessary parameters */
			Pirate thePirate = new Pirate(inputFile, N, timeoutMillis,cipher);

		/* Start the work */
			thePirate.findTreasure();
	}
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
