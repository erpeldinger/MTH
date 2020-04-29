package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;

import java.util.ArrayList;
import java.util.List;

public class TabooSolver extends DescentSolver {

	@Override
    public Result solve(Instance instance, long deadline) {
    	//Initialisation
    	GreedySolver greedy = new GreedySolver(2);
    	Result pb = greedy.solve(instance, deadline);
    	//Memorisation de la meilleure solution
    	Result solution = pb;
    	Result aux;
    	//Solution courante
    	Result current = pb;    	
    	//Solution tabou contenant les swaps temporairement interdits de 2 taches 
    	int[][][] sTaboo = new int[instance.numMachines][instance.numJobs][instance.numJobs];
    	//Compteur d'iterations
    	int k = 0;
    	int dureeTabou = 3;
    	int maxIter = 100;
    		
    	//Initialisation du tableau de swaps tabou
    	for(int i=0; i<instance.numMachines-1; i++) {
    		for(int j=0; j<instance.numJobs-1; j++) {
    			for(int l=0; l<instance.numJobs; l++) {
    	    		sTaboo[i][j][l] = 0;
    	    	}
        	}
    	}       	
    	//Exploration des voisinages successifs 
    	while((k<maxIter) && (deadline - System.currentTimeMillis() >= 1)) {
    		k++;  
    		//Creation des blocs et swaps du chemin critique
        	ResourceOrder orderSol = new ResourceOrder(current.schedule);
        	List<Block> blocks = blocksOfCriticalPath(orderSol);
        	List<Swap> swapTodo = new ArrayList<Swap>();
        	List<ResourceOrder> neighborsPb = new ArrayList<ResourceOrder>();
        	
    		//Creation du voisinage de la solution
        	for (int i=0; i<blocks.size()-1; i++) {    		
        		for (Swap s : neighbors(blocks.get(i))) {
        			swapTodo.add(s);
        		}
        	}         	
        	//Creation de tous les voisins du probleme
        	for (Swap s : swapTodo) {
        		orderSol = new ResourceOrder(current.schedule);
        		s.applyOn(orderSol);
        		neighborsPb.add(orderSol);
        	}   		
    		//Choix du meilleur voisin non tabou
    		for (Swap s : swapTodo) {
    			if(sTaboo[s.machine][s.t1][s.t2] <= k) {
    				orderSol = new ResourceOrder(current.schedule);
            		s.applyOn(orderSol);
            		aux = new Result(current.instance, orderSol.toSchedule(), current.cause); //solution.provedOptimal ??
            		//if(!sTaboo.contains(aux)) {
	    				sTaboo[s.machine][s.t1][s.t2] = k + dureeTabou;
	    			//}            		
            		if (current.schedule.makespan() < solution.schedule.makespan()) {
            			solution = current;
            		}
            		current = aux;
    			}    			
    		}
    	}    		
        return solution;
    }	
	
}
