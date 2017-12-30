import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

//[tk] ID'd problem! When parent has a child with same nft but lower wkrNum, it gets that one first.

public class JobQueue {
    private static int numWorkers;
    private static int[] jobs;
    private static Worker freeWorkers[];
    private static Worker nextFreeWorker[];
    private static int jobNum=0;
    private static long time = 0;
    private static int[] assignedWorker;
    private static long[] startTime;
    private static int nfwHeapSize;
    private static int fwHeapSize;

    private static FastScanner in;
    private static PrintWriter out;
    public static void main(String[] args) throws IOException {
        new JobQueue();
		JobQueue.solve();
        //JobQueue.unitTest();
    }

    
    private static class Worker{
    	public Worker(int wkrNum, long nextFreeTime){
    		this.wkrNum = wkrNum;
    		this.nextFreeTime = nextFreeTime;
    	}
    	protected long getProp(char p){
    		if(p=='w')
    			return wkrNum;
    		if(p == 'n'){
    			return nextFreeTime;
    		}
    		return -1;
    	}
    	protected int wkrNum;
    	protected long nextFreeTime;
    }
    
    private static void unitTest() throws IOException{
    	Random rnd = new Random();
    	       
        	
        	numWorkers = rnd.nextInt(99998)+1;
            
            
        	nextFreeWorker = new Worker[numWorkers];
            freeWorkers = new Worker[numWorkers];
            
            
            
            int m = rnd.nextInt(99998)+1;
            jobs = new int[m];
            for (int i = 0; i < m; ++i) {
             		jobs[i] = 999999-i;//rnd.nextInt(99999999);
            }
        	assignedWorker = new int[jobs.length];
        	startTime = new long[jobs.length];

        	
        	for(int i=0;i<nextFreeWorker.length;i++){
            	nextFreeWorker[i] = new nullWorker();
            }
        	nfwHeapSize = 0;
            for(int i=0;i<freeWorkers.length;i++){
            	freeWorkers[i] = new Worker(i,0);
            }
            fwHeapSize = freeWorkers.length;
    		
        	Worker[] nfwBackup = new Worker[numWorkers];
        	System.arraycopy(nextFreeWorker, 0, nfwBackup, 0, nextFreeWorker.length);
        	Worker[] fwBackup = new Worker[numWorkers];
        	System.arraycopy(freeWorkers, 0, fwBackup, 0, freeWorkers.length);
       	
        	assignJobs();
        	long[][] myAnswers = new long[m][2];
        	for(int i=0;i<m;i++){
        		myAnswers[i][0] = (int)new Integer(assignedWorker[i]);
        		myAnswers[i][1] = (long)new Long(startTime[i]);
        	}
        	assignedWorker = new int[jobs.length];
        	startTime = new long[jobs.length];
        	System.arraycopy(nfwBackup, 0, nextFreeWorker, 0, nextFreeWorker.length);
        	System.arraycopy(fwBackup, 0, freeWorkers, 0, freeWorkers.length);
        	
        	assignJobsNaive();
        	long[][] naiveAnswers = new long[m][2];
        	for(int i=0;i<m;i++){
        		naiveAnswers[i][0] = (int)new Integer(assignedWorker[i]);
        		naiveAnswers[i][1] = (long)new Long(startTime[i]);
        	}
        	
        	if(!Arrays.deepEquals(myAnswers, naiveAnswers)){
        		System.out.println("Wrong answer with input:");
        		System.out.printf("%d %d%n", numWorkers, m);
        		System.in.read();
        		for(int i=0;i<m;i++){
        			System.out.printf("%d ", jobs[i]);
        		}
        		System.in.read();
        		int printedItems = 0;
        		for(int i=0;i<m;i++){
        			if(myAnswers[i][0]!=naiveAnswers[i][0] || myAnswers[i][1]!=naiveAnswers[i][1]){
        				System.out.println("On index" + i + "NA got [" + naiveAnswers[i][0] +", " +  naiveAnswers[i][1] + "]");
        				System.out.println("MA got [" + myAnswers[i][0] +", " +  myAnswers[i][1] + "]}");
        				printedItems++;
        				if(printedItems>20){
        					break;
        				}
        			}
        		}
        		/*System.out.println();
        		System.out.println("Naive Answer:");
        		for(int i=0;i<m;i++){
        			System.out.printf("[" + naiveAnswers[i][0] + " " + naiveAnswers[i][1] + "]");
        		}
    			System.out.println();
        		System.out.println("My Answer:");
        		for(int i=0;i<m;i++){
        			System.out.printf("[" + myAnswers[i][0] + " " + myAnswers[i][1] + "]");
        		}
    			System.out.println();
    			//System.in.read();*/
    		
        	} else {
        		System.out.println("Right answer.");
        		/*System.out.printf("%d %d%n", numWorkers, m);
        		for(int i=0;i<m;i++){
        			System.out.printf("%d ", jobs[i]);
        		}*/

        	}
    	
    }
    
    private static class nullWorker extends Worker{
    	public nullWorker(){
    		super(Integer.MAX_VALUE, Long.MAX_VALUE);
    	}
    }
    private static void readData() throws IOException {
        
    	
    	numWorkers = in.nextInt();
        
        
    	nextFreeWorker = new Worker[numWorkers];
        freeWorkers = new Worker[numWorkers];
        
        
        
        int m = in.nextInt();
        jobs = new int[m];
        for (int i = 0; i < m; ++i) {
            jobs[i] = in.nextInt();
        }
    	assignedWorker = new int[jobs.length];
    	startTime = new long[jobs.length];

    	
    	for(int i=0;i<nextFreeWorker.length;i++){
        	nextFreeWorker[i] = new nullWorker();
        }
    	nfwHeapSize = 0;
        for(int i=0;i<freeWorkers.length;i++){
        	freeWorkers[i] = new Worker(i,0);
        }
        fwHeapSize = freeWorkers.length;
    }

    private static void writeResponse() {
        for (int i = 0; i < jobs.length; ++i) {
            out.println(assignedWorker[i] + " " + startTime[i]);
        }
    }

    private static void assignJobs() {
    	jobNum = 0;
    	while(jobNum<jobs.length){
			getNextWorker();
			while(fixChildBug(0)){
				
			}
			Worker wk = nextFreeWorker[0];
			assignedWorker[jobNum] = wk.wkrNum;
			startTime[jobNum] = wk.nextFreeTime;
			wk.nextFreeTime += (jobs[jobNum]);
			siftDown(nextFreeWorker, 'n', 0);
			
			if(freeWorkers[0].getClass().equals(nullWorker.class)){
				time+=nextFreeWorker[0].nextFreeTime;
			}
    		while(expiredWorker()){
    			addFreeWorker(nextFreeWorker[0]);
    			nextFreeWorker[0]=nextFreeWorker[nfwHeapSize - 1];
    			nextFreeWorker[nfwHeapSize - 1] = new nullWorker();
    			nfwHeapSize--;
    			siftDown(nextFreeWorker, 'n', 0);
    		}
			jobNum++;
    		} 
    	}
    
    private static boolean fixChildBug(int p){
    	int children[] = new int[2];
    	children[0] = (p + 1) * 2 -1;
    	children[1] = (p + 1) * 2;
    	for(int i=0;i<2;i++){
    		if(children[i]<nextFreeWorker.length){
    			if(nextFreeWorker[p].nextFreeTime==nextFreeWorker[children[i]].nextFreeTime &&
    					nextFreeWorker[p].wkrNum>nextFreeWorker[children[i]].wkrNum){
    				swapArrayVals(nextFreeWorker, p, children[i]);
    				fixChildBug(children[i]);
    				return true;
    			}
    		}
    	}
    	return false;
    }

	private static void addFreeWorker(Worker w){
		freeWorkers[fwHeapSize] = w;
		siftUp(freeWorkers, 'w', fwHeapSize);
		fwHeapSize++;
	}
    private static boolean expiredWorker(){
    	if(nextFreeWorker[0].getClass().equals(nullWorker.class))
    		return false;
    	if(nextFreeWorker[0].nextFreeTime==time){
    		return true;
    	}
    	return false;
    }
    
    
    // then we need to use a worker already assigned
    private static void getNextWorker(){
    	if(!freeWorkers[0].getClass().equals(nullWorker.class)){
    		
    		
    		nextFreeWorker[nfwHeapSize] = freeWorkers[0];
    		nextFreeWorker[nfwHeapSize].nextFreeTime = time;
    		freeWorkers[0] = freeWorkers[fwHeapSize - 1];
    		freeWorkers[fwHeapSize - 1] = new nullWorker();
    		siftUp(nextFreeWorker,'n', nfwHeapSize);
    		siftDown(freeWorkers, 'w', 0);
    		fwHeapSize--;
    		nfwHeapSize++;
    	}
    }
    
    private static void siftDown(Worker[] heap, char prprty, int index){
    	int maxIndex = index;
    	long childVal;
    	int child0 = (index + 1) * 2 -1;
    	int child1 = (index + 1) * 2;
    	if(child0<heap.length){
    		childVal = heap[child0].getProp(prprty);
    		if(heap[maxIndex].getProp(prprty)>childVal){
    			maxIndex = child0;
    		 			
    		}
    		if(child1<heap.length){
        		childVal = heap[child1].getProp(prprty);
        		if(heap[maxIndex].getProp(prprty)>childVal)
        			maxIndex = child1;
    		} 
    	}
    	if(maxIndex!=index){
    		swapArrayVals(heap, index, maxIndex);
    		siftDown(heap, prprty, maxIndex);
    	}
    }

    private static void siftUp(Worker[] heap, char prpty, int index){
    	if(index>0){
    		int parent = (index + 1)/2 - 1;
    		long childVal = heap[index].getProp(prpty);
    		long parentVal = heap[parent].getProp(prpty);
    		if(childVal<parentVal){
    			swapArrayVals(heap, index, parent);
    			siftUp(heap, prpty, parent);
    		}
    	}
    }
    private static void swapArrayVals(Worker[] heap, int a, int b){
    	Worker temp = heap[a];
    	heap[a] = heap[b];
    	heap[b] = temp;
    }

    
    static class FastScanner {
        private BufferedReader reader;
        private StringTokenizer tokenizer;

        public FastScanner() {
            reader = new BufferedReader(new InputStreamReader(System.in));
            tokenizer = null;
        }

        public String next() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                tokenizer = new StringTokenizer(reader.readLine());
            }
            return tokenizer.nextToken();
        }

        public int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }

 
        
        public static void solve() throws IOException {
            in = new FastScanner();
            out = new PrintWriter(new BufferedOutputStream(System.out));
            readData();
            assignJobs();
            writeResponse();
            out.close();
        }
        
        
        
        
        
        
        
        private static void assignJobsNaive() {
            // TODO: replace this code with a faster algorithm.
            assignedWorker = new int[jobs.length];
            startTime = new long[jobs.length];
            long[] nextFreeTime = new long[numWorkers];
            for (int i = 0; i < jobs.length; i++) {
                int duration = jobs[i];
                int bestWorker = 0;
                for (int j = 0; j < numWorkers; ++j) {
                    if (nextFreeTime[j] < nextFreeTime[bestWorker])
                        bestWorker = j;
                }
                assignedWorker[i] = bestWorker;
                startTime[i] = nextFreeTime[bestWorker];
                nextFreeTime[bestWorker] += duration;
            }
        }

}

