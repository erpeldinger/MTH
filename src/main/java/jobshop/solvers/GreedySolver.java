package jobshop.solvers;

import java.util.ArrayList;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class GreedySolver implements Solver {
	
	private int priority;
	
	public GreedySolver(int priority) {
		this.priority = priority;
	}
	
    @Override
    public Result solve(Instance instance, long deadline) {
    	ResourceOrder resource = new ResourceOrder(instance);
    	int[] currentTimesPerMachine = new int[instance.numMachines];
    	int[] currentTimesPerJob = new int[instance.numJobs];
    	ArrayList<Task> toDo = new ArrayList<Task>(); 
    	ArrayList<Task> done = new ArrayList<Task>();
    	ArrayList<Task> realisable = new ArrayList<Task>();
    	int[] remainingTime = new int[instance.numJobs];
    	int[] tasksPerJob = new int[instance.numMachines];
    	Task next = null;
    	
    	for (int i=0; i<instance.numMachines; i++) {
    		tasksPerJob[i] = 0;
    	}  
    	//Temps restant pour LRPT
    	for (int i=0; i<instance.numJobs; i++) {
    		remainingTime[i]=0;
            for(int j=0; j< instance.numTasks; j++){
                remainingTime[i]+=instance.duration(i,j);
            }
    	}    	
    	/*for (int i=0; i<instance.numJobs; i++) {
    		for(int j=0; j<instance.numTasks; j++) {
    			times[i][j]=0;
    		}
    	}*/
    	for (int i=0; i<instance.numMachines; i++) {
    		currentTimesPerMachine[i] = 0;
    	}
    	for (int i=0; i<instance.numJobs; i++) {
    		currentTimesPerJob[i] = 0;
    	}


    	//Schedule sched = new Schedule(instance,times);
    	//EST est = new EST(times, currentTimesPerMachine, currentTimesPerJob, next);
    	
    	//Recuperation des taches realisables (premieres taches de tous les jobs)
    	for(int j=0; j<instance.numJobs; j++) {
    		for(int t=1; t<instance.numTasks; t++) {
    			toDo.add(new Task(j,t));
    		}
    		realisable.add(new Task(j,0));
    	}
    	/*    	
    	switch (this.priority) {
			case 0 : case 1 : //SPT LRPT
				resource = new ResourceOrder(instance) ;
				break;
				
			case 2 : case 3 : // EST_SPT EST_LRPT
				resource = new ResourceOrder(sched) ;
				break;
		
			default :
				System.out.println("[GreegySolver] Bad priority \n");
				break;
    	}*/
    	    	
    	//Tant qu’il y a des tâches réalisables
    	while(!realisable.isEmpty()) {    		
    		switch (this.priority) {
	    		case 0 : //SPT
	    			next = getSPT(realisable, instance);
	    			//est = getSPT(realisable, instance, times, currentTimesPerMachine, currentTimesPerJob);
	    			//next = est.getTask();
	    			break;	    			
	    			
	    		case 1 : //LRPT
	    			next = getLRPT(realisable, remainingTime, instance);
	    			//est = getLRPT(realisable, remainingTime, instance, times, currentTimesPerMachine, currentTimesPerJob);
	    			//next = est.getTask();
	    			remainingTime[next.job] -= instance.duration(next);
	    			break;
	    			
	    			
	    		case 2 :  //EST_SPT
	    			next = getEST_SPT(realisable, instance, currentTimesPerJob, currentTimesPerMachine);
					currentTimesPerJob[next.job] += instance.duration(next);
					currentTimesPerMachine[instance.machine(next)] += instance.duration(next);
					break;
					/*realisable = getEST(sched, realisable)
	    			est = getSPT(realisable, instance, times, currentTimesPerMachine, currentTimesPerJob);
	    			next = est.getTask();
	    			sched = new Schedule(instance, times);
	    			break;*/
	    			
	    		case 3 : //EST_LRPT
	    			next = getEST_LRPT(realisable, instance, currentTimesPerJob, currentTimesPerMachine, remainingTime);
	    			//maj des tableaux 
	    			currentTimesPerJob[next.job] += instance.duration(next);
	    			currentTimesPerMachine[instance.machine(next)] += instance.duration(next);
	    			remainingTime[next.job] -= instance.duration(next);
	    			break;
	    			/*
	    			realisable = getEST(sched, realisable);
	    			est = getLRPT(realisable, remainingTime, instance, times, currentTimesPerMachine, currentTimesPerJob);
	    			next = est.getTask();
	    			sched = new Schedule(instance, times);
	    			break;*/
    		
	    		default :
	    			System.out.println("[GreegySolver] Bad priority \n");
	    			break;
    		}
    		
    		int k=0;
            boolean trouve = false;
            while(k<toDo.size() && !trouve){
                if(toDo.get(k).job == next.job){
                    if(toDo.get(k).task-1 == next.task){
                        realisable.add(toDo.get(k));
                        toDo.remove(k);
                        trouve = true;
                    }
                }
                k++;
            }
    		resource.tasksByMachine[instance.machine(next)][tasksPerJob[instance.machine(next)]]=next;
			tasksPerJob[instance.machine(next)]++;
			done.add(next);	    			
			realisable.remove(next);
			
			if(deadline - System.currentTimeMillis() < 1){
                return(new Result(instance,resource.toSchedule(),Result.ExitCause.Timeout));
            }
    	}

        return new Result(instance, resource.toSchedule(), Result.ExitCause.Blocked);
    }
    
    
    public Task getSPT(ArrayList<Task> realisable, Instance instance) {
    	int min = instance.duration(realisable.get(0));
    	Task minTask = realisable.get(0);
    	for(Task task : realisable) {
    		if(instance.duration(task) <min) {
    			min = instance.duration(task); 
    			minTask = task;
    		}
    	}    	
        //times[minTask.job][minTask.task]= min;
        //currentTimesPerMachine[instance.machine(minTask)] += instance.duration(minTask);
        return minTask;
    }
    
    public Task getLRPT(ArrayList<Task> realisable, int[] remaining, Instance instance) {
    	int maxJob = 0;   
    	int max = -1;
    	Task maxTask = null;    	
    	
    	for (int j=0; j<instance.numJobs; j++) {
    		if (remaining[j] > max) {
    			max = remaining[j];
    			maxJob = j;
    		}
    	}    	
    	for (Task t : realisable) {
    		if(t.job == maxJob) {
    			maxTask = t;
    		}
    	}    
    	//times[maxTask.job][maxTask.task]= max;
    	//currentTimesPerMachine[instance.machine(maxTask)] += instance.duration(maxTask);
    	return maxTask;    	
    }
    /*
    public ArrayList<Task> getEST(Schedule schedule, ArrayList<Task> realisable) {
        Instance pb = schedule.pb;
        int min  = Integer.MAX_VALUE;
		ArrayList<Task> tabMin = new ArrayList<Task>();
        Task[][] sorted = new Task[pb.numMachines][];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for this machine, find all tasks that are executed on it and sort them by their start time
            sorted[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine
        }
        
        for(Task[] tab : sorted) {
        	for(Task t : tab) {        		        	
	        	if (schedule.startTime(t) < min && realisable.contains(t)) {
	        		min = schedule.startTime(t);
	        	}
        	}
        }        
        
        for(Task[] tab : sorted) {
        	for(Task t : tab) {        		        	
        		if (schedule.startTime(t) == min && realisable.contains(t)) {
            		tabMin.add(t);
            	}
        	}
        }
    	return tabMin;
    }*/
    
    public int getMaxJobMachine(Task t, Instance instance, int[] currentTimesPerMachine, int[] currentTimesPerJob) {
    	int numJob=-1;
    	if (currentTimesPerJob[t.job] > currentTimesPerMachine[instance.machine(t)]) {
    		numJob = currentTimesPerJob[t.job];
    	}
    	else {
    		numJob = currentTimesPerMachine[instance.machine(t)];
    	}
    	return numJob;
    }

    public Task getEST_SPT(ArrayList<Task> realisable, Instance instance, int[] currentTimesPerJob, int[] currentTimesPerMachine) {
    	int min = Integer.MAX_VALUE;
    	int aux;
    	Task task = null;
    	for (Task currentTask : realisable) {
    		aux = getMaxJobMachine(currentTask, instance, currentTimesPerMachine, currentTimesPerJob);
    		if (min > aux) { 
    			min = aux;
    			task = currentTask;
    		}
    		//si egalite entre les taches
    		else if (min == aux) { 
    			if (instance.duration(task) > instance.duration(currentTask)) {
    				task = currentTask;
    			}
    		}		
    	}
    	if (task ==null) {
    		System.out.println("[GREEDY SOLVER] getEST_SPT -> pas de tache trouvee \n");
    	}
    	return task;
    }

    public Task getEST_LRPT(ArrayList<Task> realisable, Instance instance, int[] currentTimesPerJob, int[] currentTimesPerMachine, int[] remaining) {
    	int min = Integer.MAX_VALUE;
    	int aux;
    	Task task = null;
    	for (Task currentTask : realisable) {
    		aux = getMaxJobMachine(currentTask, instance, currentTimesPerMachine, currentTimesPerJob);
    		if (min > aux) {
    			min = aux;
    			task = currentTask;
    		}
    		else if (min == aux) {
    			if (remaining[task.job] > remaining[currentTask.job]) {
    				task = currentTask;
    			}
    		}		
    	}
    	if (task ==null) {
    		System.out.println("[GREEDY SOLVER] getEST_LRPT -> pas de tache trouvee \n");
    	}
    	return task;
    }
    
}
