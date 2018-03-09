package net.coderodde.finance.loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.coderodde.finance.loan.support.BinaryHeapMostCostEffectiveLoanFinder;
import net.coderodde.finance.loan.support.FibonacciHeapMostCostEffectiveLoanFinder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MostCostEffectiveLoanFinderBenchmark {

    private static final int ACTOR_GRAPH_NUMBER_OF_ACTORS = 10_000;
    private static final int ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_1 = 50_000;
    private static final int ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_2 = 500_000;
    private static final int ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_3 = 5_000_000;
    
    private static final double ACTOR_POTENTIAL = 100.0;
    private static final double MAXIMUM_ARC_INTEREST_RATE = 0.01;
    private static final double REQUESTED_POTENTIAL = 1_000_000;
    private static final double MAXIMUM_AFFORDABLE_INTEREST_RATE = 100.0;
    
    private static final ActorGraph<Integer> GRAPH1;
    private static final ActorGraph<Integer> GRAPH2;
    private static final ActorGraph<Integer> GRAPH3;
    
    private static final Actor<Integer> START_ACTOR1;
    private static final Actor<Integer> START_ACTOR2;
    private static final Actor<Integer> START_ACTOR3;
    
    private static final MostCostEffectiveLoanFinder<Integer>
            BINARY_HEAP_FINDER = 
            new BinaryHeapMostCostEffectiveLoanFinder<>();
    
    private static final MostCostEffectiveLoanFinder<Integer>
            FIBONACCI_HEAP_FINDER = 
            new FibonacciHeapMostCostEffectiveLoanFinder<>();
    
    static {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);
        
        List<Actor<Integer>> actorList = 
                createActorList(ACTOR_GRAPH_NUMBER_OF_ACTORS);
        
        START_ACTOR1 = choose(actorList, random);
        GRAPH1 = createRandomActorGraph(actorList, 
                                        ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_1,
                                        random);
        
        START_ACTOR2 = choose(actorList, random);
        GRAPH2 = createRandomActorGraph(actorList, 
                                        ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_2,
                                        random);
        
        START_ACTOR3 = choose(actorList, random);
        GRAPH3 = createRandomActorGraph(actorList, 
                                        ACTOR_GRAPH_NUMBER_OF_ARCS_SIZE_3,
                                        random);
    }
    
    private static List<Actor<Integer>> createActorList(int size) {
        List<Actor<Integer>> actorList = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            actorList.add(new Actor<>(i));
        }
        
        return actorList;
    }
    
    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
    
    private static ActorGraph<Integer> 
        createRandomActorGraph(List<Actor<Integer>> actorList,
                               int numberOfArcs,
                               Random random) {
        ActorGraph<Integer> actorGraph = new ActorGraph<>();
        
        for (Actor<Integer> actor : actorList) {
            actorGraph.addActor(actor, ACTOR_POTENTIAL);
        }
        
        while (actorGraph.getNumberOfArcs() < numberOfArcs) {
            Actor<Integer> sourceActor = choose(actorList, random);
            Actor<Integer> targetActor = choose(actorList, random);
            
            if (!sourceActor.equals(targetActor)) {
                actorGraph.addArc(sourceActor, 
                                  targetActor, 
                                  MAXIMUM_ARC_INTEREST_RATE * 
                                          random.nextDouble());
            }
        }
        
        return actorGraph;
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testBinaryHeapFinderGraphSize1() {
        BINARY_HEAP_FINDER.findLenders(START_ACTOR1, 
                                       REQUESTED_POTENTIAL,
                                       MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testBinaryHeapFinderGraphSize2() {
        BINARY_HEAP_FINDER.findLenders(START_ACTOR2, 
                                       REQUESTED_POTENTIAL,
                                       MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testBinaryHeapFinderGraphSize3() {
        BINARY_HEAP_FINDER.findLenders(START_ACTOR3, 
                                       REQUESTED_POTENTIAL,
                                       MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testFibonacciHeapFinderGraphSize1() {
        FIBONACCI_HEAP_FINDER.findLenders(START_ACTOR1, 
                                          REQUESTED_POTENTIAL,
                                          MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testFibonacciHeapFinderGraphSize2() {
        FIBONACCI_HEAP_FINDER.findLenders(START_ACTOR2, 
                                          REQUESTED_POTENTIAL,
                                          MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testFibonacciHeapFinderGraphSize3() {
        FIBONACCI_HEAP_FINDER.findLenders(START_ACTOR3, 
                                          REQUESTED_POTENTIAL,
                                          MAXIMUM_AFFORDABLE_INTEREST_RATE);
    }
    
    public static void main(String[] args) throws RunnerException {
        Options options = 
            new OptionsBuilder()
            .include(MostCostEffectiveLoanFinderBenchmark.class.getSimpleName())
            .warmupIterations(5)
            .measurementIterations(10)
            .forks(1)
            .build();
        
        new Runner(options).run();  
    }
}
