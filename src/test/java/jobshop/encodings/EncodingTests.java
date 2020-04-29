package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.TabooSolver;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {
	
    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 14;
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

    @Test
    public void testToSchedule() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        ResourceOrder enc = new ResourceOrder(instance);

        enc.tasksByMachine[0][0] = new Task(0,0);
        enc.tasksByMachine[0][1] = new Task(1,1);
        enc.tasksByMachine[1][0] = new Task(1,0);
        enc.tasksByMachine[1][1] = new Task(0,1);
        enc.tasksByMachine[2][0] = new Task(0,2);
        enc.tasksByMachine[2][1] = new Task(1,2);

        Schedule sched = enc.toSchedule();
        System.out.println("Schedule de TestToSchedule : " + sched + " fin ") ;
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
        
        result.toString();
    }
    
    @Test
    public void testGreedySolverft06SPT() throws IOException {
    	Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
        long deadline = System.currentTimeMillis() + 100;
        
        // SPT
        GreedySolver solverSPT = new GreedySolver(0);
        System.out.println("SolverSPT created");
        Result resSPT = solverSPT.solve(instance,deadline);
        System.out.println("SolverSPT.solve terminated");
        assert resSPT.schedule.isValid();

        // LRPT
        GreedySolver solverLRPT = new GreedySolver(1);
        System.out.println("SolverLRPT created");
        Result resLRPT = solverLRPT.solve(instance,deadline);
        System.out.println("SolverLRPT.solve terminated");
        assert resLRPT.schedule.isValid();
        
        // EST_SPT
        GreedySolver solverESTSPT = new GreedySolver(2);
        System.out.println("SolverESTSPT created");
        Result resESTSPT = solverESTSPT.solve(instance,deadline);
        System.out.println("SolverESTSPT.solve terminated");
        assert resESTSPT.schedule.isValid();
        
        // EST_LRPT
        GreedySolver solverESTLRPT = new GreedySolver(3);
        System.out.println("SolverESTLRPT created");
        Result resESTLRPT = solverESTLRPT.solve(instance,deadline);
        System.out.println("SolverESTLRPT.solve terminated");
        assert resESTLRPT.schedule.isValid();

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        //assert result.schedule.makespan() == resSPT.schedule.makespan(); // should have the same makespan
        //assert result.schedule.makespan() == resLRPT.schedule.makespan(); // should have the same makespan
    }

    @Test
    public void testDescentSolver() throws IOException {
    	Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
        long deadline = System.currentTimeMillis() + 10;

        DescentSolver solverDescent = new DescentSolver();
        System.out.println("Descent Solver created");
        Result res = solverDescent.solve(instance,deadline);
        assert res.schedule.isValid();

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        //assert result.schedule.makespan() == res.schedule.makespan(); // should have the same makespan
    }
    
    @Test
    public void testTabooSolver() throws IOException {
    	Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
        long deadline = System.currentTimeMillis() + 10;

        TabooSolver solverTaboo = new TabooSolver();
        System.out.println("Taboo Solver created");
        Result res = solverTaboo.solve(instance,deadline);
        assert res.schedule.isValid();

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        //assert result.schedule.makespan() == res.schedule.makespan(); // should have the same makespan
    }
}
