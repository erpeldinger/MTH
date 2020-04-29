package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
        	Task aux = order.tasksByMachine[machine][t1];
        	order.tasksByMachine[machine][t1] = order.tasksByMachine[machine][t2];
        	order.tasksByMachine[machine][t2] = aux;
        }
    }
       
    @Override
    public Result solve(Instance instance, long deadline) {
    	//Initialisation
    	GreedySolver greedy = new GreedySolver(2);
    	Result pb = greedy.solve(instance, deadline);
    	//Memorisation de la solution
    	Result solution = pb;
    	Result aux;
    	//Creation des blocs et swaps du chemin critique
    	ResourceOrder orderSol = new ResourceOrder(solution.schedule);
    	List<Block> blocks = blocksOfCriticalPath(orderSol);
    	List<Swap> swapTodo = new ArrayList<Swap>();
    	List<ResourceOrder> neighborsPb = new ArrayList<ResourceOrder>();
    	boolean improve = true;
    	
    	//Creation du voisinage de la solution
    	for (int i=0; i<blocks.size()-1; i++) {    		
    		for (Swap s : neighbors(blocks.get(i))) {
    			swapTodo.add(s);
    		}
    	}    	
    	//Creation de tous les voisins du probleme
    	for (Swap s : swapTodo) {
    		orderSol = new ResourceOrder(solution.schedule);
    		s.applyOn(orderSol);
    		neighborsPb.add(orderSol);
    	}    	    	    	
    	//Exploration des voisinages successifs pour trouver le meilleur voisin
    	while(improve && (deadline - System.currentTimeMillis() >= 1)) {
    		improve = false;
    		//Recherche du meilleur voisin
    		for (ResourceOrder resource : neighborsPb) {
    			aux = new Result(resource.instance, resource.toSchedule(), solution.cause); //solution.provedOptimal ??
    			//Si la solution est améliorante, on retient le voisin
    			if (aux.schedule.makespan() < solution.schedule.makespan()) {
    				solution = aux;
    				improve = true;
    			}
    		}   
    	}
        return solution;
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
    	
    	List<Block> list = new ArrayList<Block>();
    	Schedule sched = order.toSchedule();
    	Instance instance = sched.pb;
    	List<Task> criticalPath = sched.criticalPath();
    	int first = Integer.MIN_VALUE;
    	int last = Integer.MIN_VALUE;
    	Task previous = null;
    	int belongs = -1; // Si belongs = -1 -> pas de bloc existant
    					 // Si belongs = 0 -> creation d'un nouveau bloc
    					 // Si belongs = 1 -> appartient a un bloc existant
    					
    	//On parcourt les taches du chemin critique
    	for (Task current : criticalPath)	{
    		//Absence de bloc, il faut en creer un nouveau
    		if(belongs == -1) {
    			belongs = 0;
    		}    		
    		//Creation d'un nouveau bloc
    		else if (belongs == 0) {
    			if(instance.machine(current) == instance.machine(previous)) {    			 
		    		first = getIndex(order.tasksByMachine[instance.machine(previous)], previous);
		    		belongs = 1;
    			}
    		}		    	
		    else if(!(instance.machine(current) == instance.machine(previous))) {
	    		last = getIndex(order.tasksByMachine[instance.machine(previous)], previous);
	    		list.add(new Block(instance.machine(previous), first, last));
	    		belongs = 0;
	    		first = Integer.MIN_VALUE;
	    		last = Integer.MIN_VALUE;	    			    		
	    	}
    		previous = current;   
    	}
    	//Cas du dernier bloc
    	if (first != Integer.MIN_VALUE) {
    		last = getIndex(order.tasksByMachine[instance.machine(previous)], previous); 
    		list.add(new Block(instance.machine(previous), first, last));
    	}    	
        return list;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
    	List<Swap> neighbors = new ArrayList<Swap>() ;
    	int nbTask = block.lastTask - block.firstTask +1;
    	
    	if(block.firstTask>=0 && block.lastTask>=0) {
	    	if (block.firstTask == block.lastTask) {
	    		//System.out.println("[DESCENT SOLVER] neighbors : 1 tache -> pas de voisinage \n");
	    	}
	    	else if (block.firstTask != block.lastTask) {
	    		// Si il y a 2 taches dans le bloc => 1 solution voisine
	    		if (nbTask == 2) {
	    			neighbors.add(new Swap(block.machine, block.firstTask, block.lastTask));
	    		}
	    		//Si il y a plus de 2 taches dans le bloc => 2 solutions voisines
	    		else if(nbTask > 2) {
	    			neighbors.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
	    			neighbors.add(new Swap(block.machine, block.lastTask-1, block.lastTask));	
	    		}
	    	}	    	
    	} 
    	else {
	    	System.out.println("[DESCENT SOLVER] neighbors : pas de taches -> pas de voisinage \n");
    	}
        return neighbors;
    }
    
    /** Gets the index of a given task in the critical path*/
    int getIndex(Task[] criticalPath, Task task) {
    	int index = Integer.MIN_VALUE;
    	int size = 0;
    	for (Task t : criticalPath) {
			if(t.equals(task)) {
				index = size;
			}
			size++;
		}
    	if(index == Integer.MIN_VALUE) {
    		System.out.println("[DESCENT SOLVER] get index : index introuvable");
    	}
    	return index;
    }
    
}
