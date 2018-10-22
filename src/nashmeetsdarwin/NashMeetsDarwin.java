/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nashmeetsdarwin;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author pwinzell
 */
public class NashMeetsDarwin extends Application implements NewBestListener{
    
    ArrayList<Sprite> sprites = null;
    ArrayList<Sprite> displaysprites = null;
    
    static int runningTime = 0;
    static long system_time;
    
    GeneticEngineHandler darwin = null;
    final Object spritesLock = new Object();
    
    //statistics 
    long gens = 0;
    double fitness = 1000;
    
    @Override
    public void start(Stage primaryStage) {
        
       
        StackPane wrapperPane = new StackPane();
        wrapperPane.setId("wrapperPane");
        wrapperPane.getStylesheets().add(this.getClass().getResource("roadCSS.css").toExternalForm());
        
        Scene scene = new Scene(wrapperPane, 500, 200);
        
        primaryStage.setTitle("Highway 1");
        primaryStage.setScene(scene);
      
        

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());
        
        final ChangeListener<Number> listener = new ChangeListener<Number>() {
            final Timer timer = new Timer(); // uses a timer to call your resize method
            TimerTask task = null; // task to execute after defined delay
            final long delayTime = 200; // delay that has to pass in order to consider an operation done

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue) {
                if (task != null) { // there was already a task scheduled from the previous operation ...
                    task.cancel(); // cancel it, we have a new size to consider
                }

                task = new TimerTask() // create new task that calls your resize operation
                {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                   wrapperPane.getChildren().remove(0);
                                    
                                     //Bounds bounds = getVisibleBounds(wrapperPane);
                                     //if (bounds == null)
                                     Bounds   bounds = wrapperPane.getBoundsInParent();
                                   
                                    int width = (int)bounds.getWidth();
                                    int height = (int)bounds.getHeight();
                                    
                                    final Canvas temp_canvas = new Canvas(width, height); 
                                    
                             
                                    initSprites(width,height,true);
                                    drawRoad(temp_canvas);
                                    wrapperPane.getChildren().add(0,temp_canvas);
                                    System.out.println("drawing");
                                    
                            }
                        });   
                        
                        
                    }
                };
                // schedule new task
                timer.schedule(task, delayTime);
            }
        };
        
        // game loop, repanting the graph every 40ms 
       
        Bounds   bounds = wrapperPane.getBoundsInParent();
                                   
        int width = (int)bounds.getWidth();
        int height = (int)bounds.getHeight();
        
       
        
        wrapperPane.getChildren().add(new Canvas());
        
        primaryStage.widthProperty().addListener(listener);
        primaryStage.heightProperty().addListener(listener);

        primaryStage.show();
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                    drawRoad(wrapperPane);
            }
        }.start();
        
    }

    private void updateSprites(double maxwidth,double height){
        double currentY = (height / 2) - 48;
        for (int i = 0 ; i < 10; i++){
            Sprite s = displaysprites.get(i);
            s.setPositionX(s.getPositionX() + s.getVelocityX());
            s.setPositionY(s.getPositionY() + s.getVelocityY());
            if (s.getPositionX()  >= maxwidth){
                s.setPositionX(0);
                s.setPositionY(currentY);
            }
            
        }
        
        currentY = currentY + 64;
        for (int i = 10 ; i < 20; i++){
            Sprite s = displaysprites.get(i);
            s.setPositionX(s.getPositionX() + s.getVelocityX());
            s.setPositionY(s.getPositionY() + s.getVelocityY());
            if (s.getPositionX()  <=0 ) {
                s.setPositionX(maxwidth - 32);
                s.setPositionY(currentY); 
            }
        }
    }
    
   
    
    // Initialize or update
    private void initSprites(double width, double height, boolean invalidate) {
            
            if (invalidate || sprites == null) {
                darwin = new GeneticEngineHandler(width,height);
                darwin.addListener(this);
                sprites = displaysprites = darwin.getCurrentSpriteList();
                system_time = System.currentTimeMillis();
                startSimulation();
            }
            else{
                checkforupdates();
                updateSprites(width,height);
            }
    }
    
    
    private void printStats(GraphicsContext gc,double width,double height){
                
        gc.setFont(Font.font("Verdana", FontPosture.ITALIC, 40));
        gc.setStroke(Color.AQUAMARINE);
        gc.strokeText("Generation: " + gens + " Fitness: " + fitness,width/2 - 40, height - 70 );
       
    }
    
    // Check if there is something new to display every 30 seconds.
    private void checkforupdates(){
        synchronized(this.spritesLock){
            if (sprites == displaysprites || (sprites == null))
                return;
            long current_time = System.currentTimeMillis();
           
            if ( ((current_time - system_time)/1000) > 10){
                displaysprites = sprites;
               system_time = System.currentTimeMillis();
            }
        }
    }
    // Draw road and "cars"
    private void drawRoad(StackPane pane){
        
        pane.getChildren().remove(0);
        final Canvas temp_canvas = new Canvas(pane.getWidth(), pane.getHeight()); 
        drawRoad(temp_canvas);
        pane.getChildren().add(0,temp_canvas);
    }
    
    
    private void drawRoad(Canvas canvas){
        
        initSprites(canvas.getWidth(),canvas.getHeight(),false);
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setStroke(Color.WHITE);
        double height = canvas.getHeight();
        
        double carheight = 16;
        double roadheight = carheight * 8;
        double curbestart = (height/2) - (roadheight/2);
        
        gc.strokeLine(0, curbestart, canvas.getWidth(), curbestart);
        gc.strokeLine(0,curbestart+roadheight,canvas.getWidth(),curbestart + roadheight);
        
        gc.setStroke(Color.YELLOW);
        double middleline = curbestart + roadheight/2;
        gc.strokeLine(0,middleline-2,canvas.getWidth(),middleline-2);
        gc.strokeLine(0,middleline+2,canvas.getWidth(),middleline+2);
        
        for (Sprite sprite: displaysprites){
            sprite.render(gc);
        }
        
        printStats(gc,canvas.getWidth(),canvas.getHeight());
    }
    
    // Start the genetic engine from a thread pool
    public void startSimulation(){
        ExecutorService service = Executors.newFixedThreadPool(5);
        service.submit(darwin);
        System.out.println("simulationstarted");
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void setNewBest(ArrayList<Sprite> sprites,long generation,double fitness) {
        // System.out.println("Generation: " + generation + " Fitness: " + fitness);
        this.gens = generation;
        this.fitness = fitness;
        this.sprites = sprites;
    }
    
}
