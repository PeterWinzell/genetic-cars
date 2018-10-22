package nashmeetsdarwin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author pwinzell
 */
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;


public class Sprite {
    
    private Image image;
    private Image rotatedImage;
    
    private double positionX;
    private double positionY;
    
    private double velocityX;
    private double velocityY;
    
    private double width;
    private double height;

    
    public Sprite(){
        positionX = positionY = velocityX = velocityY = 0;
    }
    
    public void setImage(Image image) {
        this.rotatedImage = this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }
    
    public void setImage(String filename)
    {
        Image i = new Image(filename);
        setImage(i);
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Image getImage() {
        return image;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getWidth() {
        return width;
    }
    
    

    public double getHeight() {
        return height;
    }
    
    
       public void addVelocity(double x, double y)
    {
        velocityX += x;
        velocityY += y;
    }

    public void update(double time)
    {
        positionX += velocityX * time;
        positionY += velocityY * time;
    }

    public void render(GraphicsContext gc)
    {
        // gc.drawImage( image, positionX, positionY );
        gc.drawImage(rotatedImage, positionX, positionY);
    }

    public Image rotateImage(Image image, int rotation) {
        
        ImageView iv = new ImageView(image);
        
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setTransform(new Rotate(rotation, image.getHeight() / 2, image.getWidth() / 2));
        //params.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getWidth()));
        
        return iv.snapshot(params, null);
    }
    
    
    public Rectangle2D getBoundary()
    {
        return new Rectangle2D(positionX,positionY,width,height);
    }

    public boolean intersects(Sprite s)
    {
        return s.getBoundary().intersects( this.getBoundary() );
    }
    
    public String toString()
    {
        return " Position: [" + positionX + "," + positionY + "]" 
        + " Velocity: [" + velocityX + "," + velocityY + "]";
    }
    
    private double sqr(double x){
        return x*x;
    }
    
    public boolean Collision(Sprite s){
        double radius = Math.sqrt(sqr(height) + sqr(width)) / 2 ; //this could be a constant for us since we have rectagles of the same length.
        
        double x1 = positionX + radius;
        double y1 = positionY + radius;
        
        double x2 = s.positionX + radius;
        double y2 = s.positionY + radius;
        
        double squaredistance = sqr(x2 - x1) + sqr(y2 - y1);
        
        
        return (squaredistance <= sqr(radius) );
    }
    
    private double angle(){
        
        double hyp = Math.sqrt(sqr(velocityX) + sqr(velocityY));
        double angleradians = Math.acos(velocityX/hyp);
        
        //System.out.println(Math.toDegrees(angleradians));
        if (velocityY < 0) angleradians = -1*angleradians;
        return Math.toDegrees(angleradians);
    }
    
     public void setRotatedImage(){
         rotatedImage = rotateImage(image,(int)Math.round(angle()));
     }
}
