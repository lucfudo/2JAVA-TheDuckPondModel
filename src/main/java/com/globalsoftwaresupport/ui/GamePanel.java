package main.java.com.globalsoftwaresupport.ui;

import main.java.com.globalsoftwaresupport.callbacks.Time;
import main.java.com.globalsoftwaresupport.constants.Constants;
import main.java.com.globalsoftwaresupport.image.Image;
import main.java.com.globalsoftwaresupport.image.ImageFactory;
import main.java.com.globalsoftwaresupport.model.Duck;
import main.java.com.globalsoftwaresupport.model.Rock;
import main.java.com.globalsoftwaresupport.model.WaterLily;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;

import java.util.*;
import java.util.List;


public class GamePanel extends JPanel {

    private boolean inGame = true;

    private ImageIcon backgroundImage;
    private ImageIcon rockImage;
    private Timer timer;

    private Time time;
    private java.util.Timer chronoSpawn;
    private java.util.Timer chronoDeath;
    private int timeDeath;

    private List<Rock> rocks;
    private List<WaterLily> waterLilies;
    private List<Duck> ducks;

    GamePanel(){
        initializeVariables();
        initializeLayout();
        initializeWaterlilies();
        initializeDucks();
    }
    // INITIALIZATION
    private void initializeVariables() {
        this.time = new Time();
        this.chronoSpawn = time.getChrono();
        this.chronoDeath = new java.util.Timer();
        this.timeDeath = 0;

        this.rocks = new ArrayList<>();
        this.waterLilies = new ArrayList<>();
        this.ducks = new ArrayList<>();

        this.backgroundImage = ImageFactory.createImage(Image.BACKGROUND);
        this.rockImage = ImageFactory.createImage(Image.ROCK);
        this.timer = new Timer(Constants.GAME_SPEED, new GameLoop(this));
        this.timer.start();

    }
    private void initializeLayout() {
        setPreferredSize(new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT));
    }

    // WATERLILIES
    private void initializeWaterlilies(){
        // Waterlilies
        for(int i=0; i<Constants.WATERLILIES_ROW; i++){
            for(int j=0; j<Constants.WATERLILIES_COLUMN; j++){
                if(i == 0 || i == Constants.WATERLILIES_ROW-1 || j == 0 || j == Constants.WATERLILIES_COLUMN-1){
                    WaterLily waterLily = new WaterLily(Constants.WATERLILY_X + Constants.WATERLILY_GAP_X * j,
                                                        Constants.WATERLILY_Y + Constants.WATERLILY_GAP_Y * i);
                    this.waterLilies.add(waterLily);
                }
                else{
                    Rock rock = new Rock(Constants.WATERLILY_X + Constants.WATERLILY_GAP_X * j,
                                         Constants.WATERLILY_Y + Constants.WATERLILY_GAP_Y * i);
                    this.rocks.add(rock);
                }
            }
        }
        for(WaterLily waterLily : this.waterLilies){
            int r = new Random().nextInt(Constants.RANDOM_WATERLILIES);
            if (r != 1){
                waterLily.setVisible(false);
            }
        }
    }
    private void drawRocks(Graphics g){
        for(Rock rock : this.rocks){
            g.drawImage(rock.getImage(), rock.getX(), rock.getY(), this);
        }
    }
    private void drawWaterLilies(Graphics g){
        for(WaterLily waterLily : this.waterLilies){
            if(waterLily.isVisible()){
                g.drawImage(waterLily.getImage(), waterLily.getX(), waterLily.getY(), this);
            }
        }
    }

    // DUCKS
    private void initializeDucks(){
        // Ducks
        Duck duck = new Duck();
        this.ducks.add(duck);
    }
    private void drawDuck(Graphics g){
        for(Duck duck : this.ducks){
            if(!duck.isDead()){
                g.drawImage(duck.getImage(), duck.getX(), duck.getY(), this);
            }
        }

    }
    private void eating(){
        for(Duck duck : this.ducks){
            for(WaterLily waterLily : this.waterLilies){
                int waterlilyX = waterLily.getX();
                int waterlilyY = waterLily.getY();
                int duckX = duck.getX();
                int duckY = duck.getY();
                if(waterLily.isVisible())
                if(waterlilyX-20 <= duckX && duckX <= waterlilyX+20){
                    if(waterlilyY-20 <= duckY && duckY <= waterlilyY+20){
                        waterLily.setVisible(false);
                        if(duck.getColor() != Constants.DUCK_MAX_COLOR)
                        duck.setColor(duck.getColor()+1);
                    }
                }
            }
        }
    }
    private void duckDecrease(){
        for(Duck duck: ducks){
            if(!duck.isDead()){
                duck.setColor(duck.getColor()-1);
            }
        }
    }

    // DRAWING
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage.getImage(), 0, 0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT, null);

        doDrawing(g);
    }
    private void doDrawing(Graphics g){
        if(inGame){
            drawRocks(g);
            drawWaterLilies(g);
            drawDuck(g);
        } else{
            if(timer.isRunning()){
                timer.stop();
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }

    // UPDATE
    public void doOneLoop(){
        update();
        repaint();
    }
    private void update(){
        // ducks eating
        eating();
        // reset waterlilies
        this.chronoSpawn.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Constants.SPAWN);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initializeWaterlilies();
                initializeDucks();
            }
        }, Constants.SPAWN, Constants.SPAWN);
        // decrease weight of the duck
        this.chronoDeath.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timeDeath == Constants.DECREASE){
                    duckDecrease();
                    timeDeath = 0;
                }
                timeDeath ++;
            }
        }, 0);

        // move ducks
        for(Duck duck : this.ducks){
            if(!duck.isDead()){
                duck.move();
            }
        }
    }
}
