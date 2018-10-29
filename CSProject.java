import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.Thread;
import java.io.*;
import java.util.Scanner;

public class CSProject extends JPanel{
    private final int X = 20, Y = 20, length = 20;
    private int[][] board = new int[X][Y]; // 0 = empty; 1 = snake; 2 = food;
    private Queue snake;
    private Direction dir;
    private int score = 0;
    private boolean stop = false;
    private String moveDirection = "";
    private int f_x, f_y;
    private static JFrame jf = new JFrame();
    private static Color back, bord, snk, food;
    private static boolean wait = true, end = false;
    
    public CSProject(){// initializes snake in the middle of the board;
        snake = new Queue(X/2, Y/2);
        board[X/2][Y/2] = 1;
        addFood();
        dir = new Direction(0);
    }
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        setBackground(back);
        
        for (int x = 0; x < X; x++){
            for (int y = 0; y < Y; y++){
                switch(board[x][y]){
                    case 1:
                        g.setColor(snk);
                        g.fillRect(x*20+5, y*20+5, length, length);
                        break;
                    case 2:
                        g.setColor(food);
                        g.fillRect(x*20+5, y*20+5, length, length);
                        break;
                }
            }
        }
        
        g.setColor(bord);
        g.fillRect(0,0,410,5);
        g.fillRect(0,0,5,410);
        g.fillRect(0,405,410,5);
        g.fillRect(405,0,5,410);
    }
    
    private void moveSnake(String str){
        Node n = snake.last;
        int x = n.x_c;
        int y = n.y_c;
        
        
        if (str.equals("LEFT")){
            dir.turnLeft();
        }
        
        else if (str.equals("RIGHT")){
            dir.turnRight();
        }
        
        int[] d = dir.returnDirection();
        x += d[0];
        y += d[1];
        
        moveSnake(x,y);
    }
    
    private void moveSnake(int x, int y){// moves the snake to x, y, preserving length
        
        //adds the front of the snake
        snake.append(x, y);
        
        if (x > 19 || y > 19 || x < 0 || y < 0 || board[x][y] == 1){
            // if the snake moves into a wall or itself
            stop = true;
        }
        
        else if (board[x][y] != 2){// reomves the tail IF there is NO food
            Node last = snake.remove();
            board[last.x_c][last.y_c] = 0;
                        
            board[x][y] = 1;   
        }
        else{// if there is food, initializes food at another random part of the board
            addFood();
            score++;

            board[x][y] = 1;
        }
    }
    
    private void addFood(){
        int rand_x;
        int rand_y;
        while (true){
                rand_x = (int) (Math.random()*X);
                rand_y = (int) (Math.random()*Y);
                if (board[rand_x][rand_y] == 0){
                    board[rand_x][rand_y] = 2;
                    break;
                }
        }
        f_x = rand_x;
        f_y = rand_y;
    }
    
    
    public static void main(String[] args){
        
        jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(410, 430);

        
        //Integer.parseInt(JOptionPane.showInputDialog("Set speed"));
        
        JPanel p = new JPanel();
        jf.add(p);
        
        CheckboxGroup speeds = new CheckboxGroup();
        Choice snakeCol = new Choice();
        snakeCol.add("Blue");
        snakeCol.add("Red");
        snakeCol.add("Black");
        snakeCol.add("White");
        
        Choice foodCol = new Choice();
        foodCol.add("Red");
        foodCol.add("Blue");
        foodCol.add("Black");
        foodCol.add("White");
        
        Choice backCol = new Choice();
        backCol.add("Black");
        backCol.add("Red");
        backCol.add("Blue");
        backCol.add("White");
        
        Choice bordCol = new Choice();
        bordCol.add("White");
        bordCol.add("Red");
        bordCol.add("Black");
        bordCol.add("Blue");
        
        Button start = new Button("Start");
        
        Checkbox easy = new Checkbox("Easy", speeds, true);
        Checkbox med = new Checkbox("Medium", speeds, false);
        Checkbox hard = new Checkbox("Hard", speeds, false);
        
        p.add(new Label("Difficulty:"));
        p.add(easy);
        p.add(med);
        p.add(hard);
        p.add(new Label("              "));
        p.add(new Label("                                                                                         "));
        p.add(new Label("Snake Color:"));
        p.add(snakeCol);
        p.add(new Label("Food Color:"));
        p.add(foodCol);
        p.add(new Label("                                                                                         "));
        p.add(new Label("Background Color:"));
        p.add(backCol);
        p.add(new Label("Border Color:"));
        p.add(bordCol);
        p.add(new Label("                                                                                         "));
        p.add(start);
        
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                wait = false;}});
                
        
        
        jf.setVisible(true);
        
        while (wait){
            pause(500);}
            
        wait = true;
            
        int speed;
        int line;
        String dif;
        if (speeds.getSelectedCheckbox().equals(easy)){
            speed = 120; line = 0; dif = "Easy";}
        else if (speeds.getSelectedCheckbox().equals(med)){
            speed = 100; line = 1; dif = "Med";}
        else{
            speed = 80; line = 2; dif = "Hard";}
        
        
        String s = backCol.getSelectedItem();
        if (s.equals("Blue")){
            back = Color.CYAN;}
        else if (s.equals("Red")){
            back = Color.RED;}
        else if (s.equals("Black")){
            back = Color.BLACK;}
        else{
            back = Color.WHITE;}
            
        s = snakeCol.getSelectedItem();
        if (s.equals("Blue")){
            snk = Color.CYAN;}
        else if (s.equals("Red")){
            snk = Color.RED;}
        else if (s.equals("Black")){
            snk = Color.BLACK;}
        else{
            snk = Color.WHITE;}
            
        s = bordCol.getSelectedItem();
        if (s.equals("Blue")){
             bord = Color.CYAN;}
        else if (s.equals("Red")){
             bord = Color.RED;}
        else if (s.equals("Black")){
             bord = Color.BLACK;}
        else{
             bord = Color.WHITE;}
             
        s = foodCol.getSelectedItem();
        if (s.equals("Blue")){
             food = Color.CYAN;}
        else if (s.equals("Red")){
             food = Color.RED;}
        else if (s.equals("Black")){
             food = Color.BLACK;}
        else{
             food = Color.WHITE;}
            
        jf.remove(p);
        
        
        CSProject cs = new CSProject();
        
        jf.add(cs);
 
        jf.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent ke){
                switch(ke.getKeyCode()){
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        cs.moveDirection = "LEFT";
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        cs.moveDirection = "RIGHT";
                        break;       
                }
            }
        });
        
        while (!cs.stop){
            pause(speed);
            
            cs.moveSnake(cs.moveDirection);
            cs.moveDirection = "";
            cs.repaint();
            jf.setVisible(true);
            jf.requestFocus();
        }
        jf.remove(cs);
        p = new JPanel();
        p.add(new Label(String.format("Your score is %d on %s", cs.score, dif), Label.CENTER));
        
        int[] highscores = highscores(cs.score, line);
        newHighscore(highscores);
        
        p.add(new Label("                                                                                         "));
        p.add(new Label(String.format("HIGHSCORES: Easy: %d, Medium: %d, Hard: %d",highscores[0],highscores[1],highscores[2])));
        p.add(new Label("                                                                                         "));
        
        
        
        //JOptionPane.showMessageDialog(null, "Your score is " + cs.score);
        
        // add a restart button
        Button restart = new Button("Restart");
        
        
        
        restart.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                wait = false;}});
                
        Button quit = new Button("Quit");
        
        quit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                end = true;}});
                
        p.add(restart);
        p.add(quit);
        jf.add(p);
        
        jf.setVisible(true);
        while (wait && !end){
            pause(500);}
            
        
        wait = true;
        if (!end){
            main(new String[1]);}
        else{
            jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
        }
        }
        
        public static int[] highscores(int newScore, int line){
            File f1 = new File("Highscores.txt");
            Scanner input = null;
            int[] highscores = new int[3];
            try{
                input = new Scanner(f1);
                for (int k = 0; k < 3; k++){
                    highscores[k] = input.nextInt();}
            }
            catch (FileNotFoundException ex){
                System.out.println("file not found");
            }
            input.close();
            highscores[line] = Math.max(highscores[line], newScore);
            return highscores;
        }
        
        public static void newHighscore(int[] highscores){
            File f1 = new File("Highscores.txt");
            PrintWriter output = initPW(f1);
            for (int i = 0; i < 3; i++){
                output.printf("%d\n",highscores[i]);}
            output.close();
        }
        
        private static PrintWriter initPW(File f){
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(f);
            return pw;
        }
        catch (FileNotFoundException ex){
            return null;
        }
    }
    

        
    
    
    private static void pause(int s){
        try {
            Thread.sleep(s);
        }
        catch (InterruptedException ex){
            System.out.println("error sleeping");
        }
    }
    
    private double userMove(){
        if (moveDirection.equals("LEFT")){
            return 1;
        }
        else if (moveDirection.equals("RIGHT")){
            return 2;
        }
        return 0;
    }

    
    class Direction{
        int[][] dir = new int[][] {{0,-1}, {1,0}, {0,1}, {-1,0}}; // NESW
        int i;
        
        public Direction(int i){
            this.i = i;
        }
        
        public void turnLeft(){
            i = (i + 3) % 4;
        }
        
        public void turnRight(){
            i = (i + 1) % 4;
        }
        
        public int[] returnDirection(){
            return dir[i];
        }
    }
}