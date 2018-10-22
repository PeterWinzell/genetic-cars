/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nashmeetsdarwin;

import io.jenetics.Chromosome;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.MultiPointCrossover;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.MinMax;
import io.jenetics.util.Factory;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author pwinzell
 */
public class GeneticEngineHandler implements Runnable{
    
    private static ArrayList<Sprite> sprites = new ArrayList();
    
    // graphic model prerequisite data data
    private static double pixelscreenwidth;
    private static double pixelscreenheight;
    
    private static final double xstart = 32; // start 32 pixels inside the canvas
    private static final double carheight = 16;
    private static final double roadheight = carheight * 8;
    
    // chromosome length and genetype constraint(s)
    private static final double velocityconstraint = 10;
    private static final int  chromosomelength = 40;
    
    // genetic algorithm parameters
    private static final double mutationfreq = 0.2;
    private static final double crossoverporob = 0.5; 
    
    //notification frequency
    private static final double notifyfreq = 50;
    
    private NewBestListener listener = null;
    
    private static Object newbestLock; // used to synchronize around sprites...
    
    public  GeneticEngineHandler(double pixelscreenwidth,double pixelscreenheight){
        
        this.pixelscreenheight = pixelscreenheight;
        this.pixelscreenwidth = pixelscreenwidth;
               
        newbestLock = new Object();
        setCurrentBest();
    }
    
    
    
    public void addListener(NewBestListener listener){
        this.listener = listener;
    }
    
    protected ArrayList<Sprite> getCurrentSpriteList(){
        synchronized(newbestLock){
            return sprites;
        }
    }
    
    protected void setCurrentBest(){
        synchronized(newbestLock){
            sprites = getTemporarySprites();
        }
    }
    
    
    private void setCurrentBest(double bestfitness, Phenotype phenotype){
        synchronized(newbestLock){
            long generation = phenotype.getGeneration();
            if ( phenotype.getGeneration() % 50 == 0){
                // notify listener after every 10 generations
                ArrayList<Sprite> bestsprites = null;
                bestsprites = getTemporarySprites( (DoubleChromosome) phenotype.getGenotype().getChromosome());
                listener.setNewBest(bestsprites,generation,bestfitness);
            }
        }
    }
    
    private ArrayList<Sprite> getTemporarySprites(){
            // Update UI here.

            sprites = new ArrayList();

            double screenWidth = this.pixelscreenwidth;
            double screenHeight = this.pixelscreenheight;

            double currentX = 32;
            double currentY = (screenHeight / 2) - 48;
            double xstep = screenWidth / 10;

            for (int i = 0; i < 10; i++) {

                Sprite s = new Sprite();
                s.setImage("file:src/redcar.png");

                s.setPositionX(currentX);
                s.setPositionY(currentY);
                s.addVelocity(getVelocityX(), getVelocityY());

                s.setRotatedImage();
                sprites.add(s);

                currentX = (int) (currentX + xstep);

            }

            currentY = currentY + 64;
            currentX = 32;

            for (int i = 10; i < 20; i++) {

                Sprite s = new Sprite();
                s.setImage("file:src/bluecar.png");

                s.setPositionX(currentX);
                s.setPositionY(currentY);
                s.addVelocity(-getVelocityX(), getVelocityY());
                s.setRotatedImage();

                sprites.add(s);

                currentX = (int) (currentX + xstep);
            }

            return sprites;
       
    }
    
    private static ArrayList<Sprite> getTemporarySprites(DoubleChromosome chromosome){
        
        
        ArrayList<Sprite> sprites = new ArrayList();

        double screenWidth = GeneticEngineHandler.pixelscreenwidth;
        double screenHeight = GeneticEngineHandler.pixelscreenheight;

        double currentX = 32;
        double currentY = (screenHeight / 2) - 48;
        double xstep = screenWidth / 10;
        
        
        int j = 0;
        for (int i = 0; i < 10; i++) {

            Sprite s = new Sprite();
            s.setImage("file:src/redcar.png");

            s.setPositionX(currentX);
            s.setPositionY(currentY);
            
            DoubleGene veloX = chromosome.getGene(j);
            DoubleGene veloY = chromosome.getGene(j+1);
            
            s.addVelocity(Math.abs(veloX.doubleValue()), veloY.doubleValue());
            
           /* Platform.runLater(new Runnable() {
                 @Override
                 public void run() {   
                     s.setRotatedImage();     
                 }
            });*/   
             
            sprites.add(s);

            currentX = (int) (currentX + xstep);
            j = j + 2;

        }

        currentY = currentY + 64;
        currentX = 32;

        j = 10;
        for (int i = 10; i < 20; i++) {

            Sprite s = new Sprite();
            s.setImage("file:src/bluecar.png");

            s.setPositionX(currentX);
            s.setPositionY(currentY);
            
            DoubleGene veloX = chromosome.getGene(j);
            DoubleGene veloY = chromosome.getGene(j+1);
            
            s.addVelocity(-Math.abs(veloX.doubleValue()), veloY.doubleValue());
            
            
            /*Platform.runLater(new Runnable() {
                 @Override
                 public void run() {   
                     s.setRotatedImage();     
                 }
            });   */

            sprites.add(s);

            currentX = (int) (currentX + xstep);
            j = j + 2;
        }
        
        return sprites;
    }
    
    private double getVelocityX(){
        return Math.random() * 10;
    }
    
    private double getVelocityY(){
        return (Math.random() * 10) - 5;
    }

   
    // Is the ypos outside the lane.
    private static boolean outsideLane(double ylanestart,double ylaneend,double ypos){
        return (ypos > ylaneend ) || (ypos < ylanestart);
    }
    
    private static double evalchromosome(ArrayList<Sprite> sprites, double maxwidth,double height){
        double points = 1000;
        boolean dothis = true;
        int steps = 0;
        
        while (dothis){
            
            double currentY = (height / 2) - 48;
           
            double curbestart = (height/2) - (roadheight/2);
            double middleline = curbestart + (roadheight/2);
       
        
            
            for (int i = 0 ; i < 10; i++){
                Sprite s = sprites.get(i);
                s.setPositionX(s.getPositionX() + s.getVelocityX());
                s.setPositionY(s.getPositionY() + s.getVelocityY());
                
                if (outsideLane(curbestart,middleline,s.getPositionY())){
                    points = points - 10;
                }
                else{
                    points = points + 10;
                }
                
                if (s.getPositionX()  >= maxwidth){
                    s.setPositionX(0);
                    s.setPositionY(currentY);
                    points = points + 20;
                }
                else if (s.getPositionX() <= 0){
                    s.setPositionX(0);
                    s.setPositionY(currentY);
                    points = points - 10;
                }

            }

            currentY = currentY + 64;
            
            for (int i = 10 ; i < 20; i++){
                Sprite s = sprites.get(i);
                s.setPositionX(s.getPositionX() + s.getVelocityX());
                s.setPositionY(s.getPositionY() + s.getVelocityY());
                
                if (outsideLane(middleline,curbestart+roadheight,s.getPositionY())){
                    points = points - 10;
                }
                else{
                    points = points + 10;
                }
                
                if (s.getPositionX()  <=0 ) {
                    s.setPositionX(maxwidth - 32);
                    s.setPositionY(currentY); 
                    
                    points = points + 20;
                }
                else if (s.getPositionX()  >= maxwidth ) {
                    s.setPositionX(maxwidth - 32);
                    s.setPositionY(currentY); 
                    
                    points = points - 10;
                }
            }
            points = points - collisionCheck(sprites);
            
            
            steps = steps + 1;
            dothis = (steps < 2000);
        }    
        
        return points;
    }
    
     private static boolean rearEnding(int index1,int index2){
         return ( (index1 < 10) && (index2 < 10)) || ( (index1 > 10) && (index2 > 10) );
     }
     

     private static double collisionCheck(ArrayList<Sprite> sprites){
        double points = 0; 
        int i = 0;    
        while (i < 20){
            Sprite s = sprites.get(i);
            int j = i + 1;
            while (j < 20){
               
                Sprite s2 = sprites.get(j);
                if (s.Collision(s2)){
                    points = points - 50;
                    /*if (rearEnding(i,j)){
                        s.setVelocityX(0);
                        s.setVelocityY(0);
                    }*/    
                }
                j++;
            }
            i++;
        }    
        
        return points;        
    }
    
    
    private static Double eval(Genotype<DoubleGene> gt) {
        
        // retrieve the chromosome
        DoubleChromosome chromosome = (DoubleChromosome) gt.getChromosome();
        //copy solution to a sprite list  
        ArrayList<Sprite> evalSolution = getTemporarySprites(chromosome);
        // simulate runs for t seconds 
        double points = evalchromosome(evalSolution,GeneticEngineHandler.pixelscreenwidth,GeneticEngineHandler.pixelscreenheight);
        
        return  points;
    }
    
    @Override
    public void run() {
        
        Factory<Genotype<DoubleGene>> gtf
                = Genotype.of(DoubleChromosome.of(-GeneticEngineHandler.velocityconstraint, 
                        GeneticEngineHandler.velocityconstraint, GeneticEngineHandler.chromosomelength));

        Engine<DoubleGene, Double> engine = Engine
                .builder(GeneticEngineHandler::eval, gtf)
                .alterers(new Mutator<>(GeneticEngineHandler.mutationfreq),
                        new MultiPointCrossover<>(GeneticEngineHandler.crossoverporob)) // two-point crossover
                .build();

        final EvolutionStatistics<Double, ?> statistics
                = EvolutionStatistics.ofNumber();
        //final MinMax<EvolutionResult<DoubleGene, Double>> best = MinMax.of();
        
        final Phenotype<DoubleGene, Double> result = engine.stream()
                .limit(50000)
                .peek(r -> setCurrentBest(r.getBestFitness(),r.getBestPhenotype()))
                .peek(statistics)
                .collect(toBestPhenotype());
        
        System.out.println(statistics);
                
    }
     
    
}
