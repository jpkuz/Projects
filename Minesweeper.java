import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.Thread;

public class Minesweeper extends JPanel {
  private int[][] board;
  private boolean[][] revealed, flagged;
  private int bombsLeft, squaresLeft, gameState, next;
  public static final int X = 420, Y = 420;
  public final int ROWS, COLS;
  private static JFrame jf;
  private boolean init;
  private long startTime;
  public static int[] input = {-1,-1,-1,-1};

  public Minesweeper(int x, int y, int bombs) {
    ROWS = x;
    COLS = y;
    revealed = new boolean[ROWS][COLS];
    flagged = new boolean[ROWS][COLS];
    board = new int[ROWS][COLS];
    bombsLeft = bombs;
    squaresLeft = x*y-bombs;
    init = false;
    startTime = System.currentTimeMillis();
    gameState = 0;
    next = 0;
  }

  public void autoClear(int row, int col) {
     if (!revealed[row][col]) {
        reveal(row,col);
        if (board[row][col] == 0) {
           for (int r = row-1; r <= row+1; r++) {
              for (int c = col-1; c <= col+1; c++) {
                 if (isBounded(r, c)) {autoClear(r, c);}
              }
            }
         }
      }
  }

  public boolean isInit() {return init;}

  public static void pause(long m) {
    try {Thread.sleep(m);}
    catch (Exception e) {System.out.println("pause error");}
  }

  public void reveal(int row, int col) {
    if (!flagged[row][col] && !revealed[row][col]){
       if (board[row][col] == -1) {
          for (int r = 0; r < ROWS; r++) {
             for (int c = 0; c < COLS; c++) {
                revealed[r][c] = true;
                gameState = -1;
             }
          }
       } else{
          squaresLeft--;
          revealed[row][col] = true;
          if (squaresLeft == 0) {gameState = 1;}
       }
    }
  }

  public void toggleFlagged(int r, int c) {
    if (flagged[r][c]) {bombsLeft++;}
    else {bombsLeft--;}
    flagged[r][c] = !flagged[r][c];
  }

  private void initBoard(int row, int col) {
    init = true;
    int bombs = bombsLeft;
    while (bombs > 0) {
      int r = (int) (Math.random()*ROWS);
      int c = (int) (Math.random()*COLS);
      if ((r!=row || c!=col) && board[r][c] != -1) {
        board[r][c] = -1;
        bombs--;
        addToAdj(r, c);
      }
    }
  }

  private boolean isBounded (int r, int c) {
    return (r > -1 && c > -1 && r < ROWS && c < COLS);
  }

  private void addToAdj(int row, int col) {
    for (int r = row-1; r <= row+1; r++) {
      for (int c = col-1; c <= col+1; c++) {
        if (isBounded(r, c) && board[r][c] != -1) {
          board[r][c]++;
        }
      }
    }
  }

  private double timeElapsed() {
     return (System.currentTimeMillis() - startTime) / 1000.0;
  }

  private String getState() {
     String s = "";
     if (gameState == 0) {return "";}
     else if (gameState == -1) {s = "YOU LOSE";}
     else {s = "YOU WIN";}
     return s + "  'Q' = quit 'R' = restart";
  }
    

  public void paint(Graphics g) {
    super.paintComponent(g);
    setBackground(Color.BLACK);
    int width = X/ROWS, length = Y/COLS;
    g.setColor(Color.WHITE);
    String s;
    if (gameState == 0) {
        s = String.format("Bombs Left: %d\t\tSquares Left:%d\t\tTime: %.1f", bombsLeft, squaresLeft, timeElapsed());
    } else {
        s = String.format("Time: %.1f\t\t%s",timeElapsed(), getState());
    } g.drawString(s, 2, Y+18);
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (flagged[r][c]) {
          g.setColor(Color.RED);
          g.fillRect(r*width, c*length, width, length);
       }
        if (revealed[r][c]) {
          if (board[r][c] == -1) {g.setColor(Color.RED);}
          else {g.setColor(Color.WHITE);}
          g.fillRect(r*width, c*length, width, length);
          if (board[r][c] != 0) {
           g.setColor(Color.BLACK);
           g.drawString(Integer.toString(board[r][c]), r*width+2, (c+1)*length-2);
         }
        }
      }
    }
  }


  public static void main(String[] args) {

    jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.setSize(X, Y+45);

    for (int i = 0; i < 4; i++) {input[i] = -1;}

    TextField rowText = new TextField("Number of Rows");
    TextField colText = new TextField("Number of Columns");
    TextField bombText = new TextField("Number of Bombs");
    Button submit = new Button("Submit");

    submit.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          input[0] = 1;
          try {input[1] = Integer.parseInt(rowText.getText());
               input[2] = Integer.parseInt(colText.getText());
               input[3] = Integer.parseInt(bombText.getText());}
          catch (Exception ex) {input[0] = -1;}
       }
    });

           
    JPanel jp = new JPanel();  

    jp.add(rowText);
    jp.add(colText);
    jp.add(bombText);
    jp.add(submit);

    jf.add(jp);
    jf.setVisible(true);

    while (input[0] == -1 || input[3] >= input[2] * input[1] || input[1] < 0 || input[2] < 0 || input[3] < 0 || input[1] > 50 || input[2] > 50) {
       pause(300);
    }

    jf.remove(jp);

    Minesweeper ms = new Minesweeper(input[1], input[2], input[3]);

    MouseListener ml = new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        int r = evt.getX() * ms.ROWS / X;
        int c = evt.getY() * ms.COLS / Y;
        if (!ms.isInit()) {
           ms.initBoard(r, c);
        } if (evt.getButton() == MouseEvent.BUTTON1){
           ms.autoClear(r, c);
        } if (evt.getButton() == MouseEvent.BUTTON3) {
           ms.toggleFlagged(r, c);
        } 
        ms.repaint();
        ms.requestFocus();
      }
    };

    ms.addMouseListener(ml);    

    
    jf.add(ms);
    jf.setVisible(true);
    do {
       ms.repaint();
       pause(100);
    } while (true && ms.gameState == 0);
    ms.removeMouseListener(ml);

    ms.addKeyListener(new KeyAdapter(){
       @Override
       public void keyPressed(KeyEvent ke) {
          switch(ke.getKeyCode()) {
             case KeyEvent.VK_Q:
                 ms.next = -1; break;
             case KeyEvent.VK_R:
                 ms.next = 1; break;
          }
       } 
    });

    while (ms.next == 0) {
       pause(500);
    }
    
    if (ms.next == 1) {main(new String[1]);}
    else{jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));}
          
   
  }
    
}

