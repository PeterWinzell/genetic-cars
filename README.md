# genetic-cars 

![alt text](https://github.com/PeterWinzell/genetic-cars/blob/master/src/images/genhighway.png "Highway 1")

This application uses the [jenetics](http://jenetics.io) library to illustrate how we can use genetic algorithms as a self-learning tool. In this case we have a highway of 20 cars, where 10 cars are traveling west to east and 10 cars are traveling east to west. The genome in this case is a double double which respresent the velocity x and y direction. The genomes form a chromosome which we apply multiple-point crossover and mutation on.

```java
...
  Factory<Genotype<DoubleGene>> gtf
                = Genotype.of(DoubleChromosome.of(-GeneticEngineHandler.velocityconstraint, 
                        GeneticEngineHandler.velocityconstraint, GeneticEngineHandler.chromosomelength));

  Engine<DoubleGene, Double> engine = Engine
                .builder(GeneticEngineHandler::eval, gtf)
                .alterers(new Mutator<>(GeneticEngineHandler.mutationfreq),
                        new MultiPointCrossover<>(GeneticEngineHandler.crossoverporob)) // two-point crossover
                .build();
...                
```

The fitness function which is used by genetic algorithms to select the best individuals gives points for genomes(cars) reaching the goal and punishes for driving outside the lane and for colliding with other vehicles. The actual fitness value is calculated running the highway chromosome t milliseconds.

The code was written using Apache Netbeans 9.0 and JavaFX was used for chromosome visualization.


