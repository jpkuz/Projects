import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;
public class Quoridor {
    private final int ROWS, COLS;
    private Player[] players;

    public Quoridor(int numPlayers, int r, int c) {
        ROWS=r;
        COLS=c;
        players = new Player[numPlayers];
        int[] start = new int[] {0,(c-1)/2, r-1,(c-1)/2, (r-1)/2,c-1, (r-1)/2,0};
        
        for (int i=0; i<players.length; i++) {
            players[i] = new Player(start[i*2], start[i*2+1], 20/numPlayers);
        }
    }
    
    public Quoridor(int numPlayers) {
        this(numPlayers, 9, 9);
    } 
    
    public void displayBoard() {
        for (int r=0; r<ROWS; r++) {
            boolean[] temp = new boolean[COLS-1];
            for (int c=0; c<COLS; c++) {
                char wall=' ', pawn='.';
                for (int p=0; p<players.length && (pawn=='.' || wall==' '); p++) {
                    if (players[p].pawn.isCoord(r,c)) {
                        pawn=(char) (p+1+'0');
                    }
                    for (int w=0; w<players[p].wallIndex; w++) {
                        if (players[p].walls[w].isCoord(r,c,true) || players[p].walls[w].isCoord(r-1,c,true)) {
                            wall='|';
                            if (players[p].walls[w].isCoord(r,c,true)) {
                                temp[c]=true;
                            }
                            break;
                        }
                    }
                }
                System.out.printf("%c%c",pawn, wall);
            }
            System.out.println();
            for (int c=0; c<COLS-1 && r<ROWS-1;c++){
                char wall1=' ', wall2=' ';
                if (temp[c]) {
                    wall2='|';
                }
                for (int p=0; p<players.length; p++) {
                    for (int w=0; w<players[p].wallIndex && wall1==' '; w++) {
                        if (players[p].walls[w].isCoord(r,c-1,false) || players[p].walls[w].isCoord(r,c,false)){
                            wall1='-';
                        }
                        if (players[p].walls[w].isCoord(r,c,false)) {
                            if (wall2=='|') {
                                wall2='+';
                            }
                            else{
                                wall2='-';
                            }
                        }
                    }
                } System.out.printf("%c%c", wall1, wall2);
            } if (r<ROWS-1) {
                System.out.printf("  %d\n", r);
            }
        }
        for (int c=0; c<COLS-1; c++) {
            System.out.printf(" %c", (char) ('A' + c));
        } System.out.println();
    }
    
    public void displayStats() {
        for (int i=0; i<players.length; i++) {
            System.out.printf("Player %d:\n", i+1);
            players[i].display();
            System.out.println();
        }
        displayGoalDistances();
    }
    
    public void depthLim(int p) {
        depthLim(p, 0);
    }
    
    private int[] depthLim(int p, int d) {
        String bestMove = "";
        int[] best = new int[] {-1};
        if (d < 2 && !isOver()) {
            char[] dir=new char[] {'U', 'D', 'L','R'};
            int[] cur;
            
            // add goal check
            Coord temp = new Coord(players[p].pawn.row, players[p].pawn.col);
            for (int i=0; i<dir.length; i++) {
                if (players[p].movePawn(dir[i])) {
                    cur=depthLim((p+1)%players.length, d+1);
                    if (best[0]==-1 || compareCost(best, cur, p)>0) {
                        best=cur;
                        bestMove="P" + dir[i];
                    }
                    players[p].pawn.setCoord(temp);
                }
                
                for (int k=0; k<dir.length; k++) {
                    if (players[p].jumpMove(dir[i], dir[k])) {
                        cur=depthLim((p+1)%players.length, d+1);
                        if (best[0]==-1 || compareCost(best, cur, p)>0) {
                            best=cur;
                            bestMove="P" + dir[i] + dir[k];
                        }
                        players[p].pawn.setCoord(temp);
                    }
                }
            }
            
            
            for (int r=0; r<ROWS-1; r++) {
                for (int c=0; c<COLS-1; c++) {
                    if (players[p].setWall(r,c,true)) {
                        cur=depthLim((p+1)%players.length, d+1);
                        if (best[0]==-1 || compareCost(best, cur, p)>0) {
                            best=cur;
                            bestMove="W" + Integer.toString(r) + Integer.toString(c) + "V";
                        }
                        players[p].removeRecentWall();
                    } if (players[p].setWall(r,c,false)) {
                        cur=depthLim((p+1)%players.length, d+1);
                        if (best[0]==-1 || compareCost(best, cur, p)>0) {
                            best=cur;
                            bestMove="W" + Integer.toString(r) + Integer.toString(c) + "H";
                        }
                        players[p].removeRecentWall();
                    }
                }
            }
            
            
            
            if (d == 0) {
                if (bestMove.charAt(0) == 'P') {
                    if (bestMove.length() == 2) {
                        players[p].movePawn(bestMove.charAt(1));
                    } else {
                        players[p].jumpMove(bestMove.charAt(1), bestMove.charAt(2));
                    }
                } else {
                    players[p].setWall(bestMove.charAt(1)-'0', bestMove.charAt(2)-'0', bestMove.charAt(3)=='V');
                }
                System.out.println(bestMove);
            }
        } else {
            // return current values
            best=currentCost();
        } return best;
    }
    
    public void greedyDepthLim(int p) {
        greedyDepthLim(p, 0);
    }
    
    private int[] greedyDepthLim(int p, int d) {
        String bestMove = "";
        int[] best = new int[] {-1};
        if (d < 4 && !isOver()) {
            char[] dir=new char[] {'U', 'D', 'L','R'};
            int[] cur;
            
            // add goal check
            Coord temp = new Coord(players[p].pawn.row, players[p].pawn.col);
            for (int i=0; i<dir.length; i++) {
                if (players[p].movePawn(dir[i])) {
                    cur=greedyDepthLim((p+1)%players.length, d+1);
                    if (best[0]==-1 || compareCost(best, cur, p)>0) {
                        best=cur;
                        bestMove="P" + dir[i];
                    }
                    players[p].pawn.setCoord(temp);
                }
                
                for (int k=0; k<dir.length; k++) {
                    if (players[p].jumpMove(dir[i], dir[k])) {
                        cur=greedyDepthLim((p+1)%players.length, d+1);
                        if (best[0]==-1 || compareCost(best, cur, p)>0) {
                            best=cur;
                            bestMove="P" + dir[i] + dir[k];
                        }
                        players[p].pawn.setCoord(temp);
                    }
                }
            }
            
            
            ArrayList<MoveCost> mc = new ArrayList<MoveCost>();
            int nWalls=3;
            for (int r=0; r<ROWS-1; r++) {
                for (int c=0; c<COLS-1; c++) {
                    if (players[p].setWall(r,c,true)) {
                        MoveCost m = new MoveCost("W" + Integer.toString(r) + Integer.toString(c) + "V", getCost(currentCost(), p));
                        for (int i=mc.size()-1; i>-1; i--) {
                            if (m.cost > mc.get(i).cost) {
                                if (i!=nWalls-1) {
                                    mc.add(i+1, m);
                                    if (mc.size() > nWalls) {
                                        mc.remove(nWalls);
                                    }
                                } break;
                            }
                        }
                        if (mc.size()==0) {
                            mc.add(m);
                        } else if (m.cost < mc.get(0).cost) {
                            mc.add(0, m);
                            if (mc.size() > nWalls) {
                                mc.remove(nWalls);
                            }
                        }
                        players[p].removeRecentWall();
                    } if (players[p].setWall(r,c,false)) {
                       MoveCost m = new MoveCost("W" + Integer.toString(r) + Integer.toString(c) + "H", getCost(currentCost(), p));
                        for (int i=mc.size()-1; i>-1; i--) {
                            if (m.cost > mc.get(i).cost) {
                                if (i!=nWalls-1) {
                                    mc.add(i+1, m);
                                    if (mc.size() > nWalls) {
                                        mc.remove(nWalls);
                                    }
                                } break;
                            }
                        }
                        if (mc.size()==0) {
                            mc.add(m);
                        } else if (m.cost < mc.get(0).cost) {
                            mc.add(0, m);
                            if (mc.size() > nWalls) {
                                mc.remove(nWalls);
                            }
                        }
                        players[p].removeRecentWall();
                    }
                }
            }
            
            // traverse mc and get the values and compare to best
            for (MoveCost m: mc) {
                players[p].setWall(m.move.charAt(1)-'0', m.move.charAt(2)-'0', m.move.charAt(3)=='V');
                cur=greedyDepthLim((p+1)%players.length, d+1);
                if (best[0]==-1 || compareCost(best, cur, p)>0) {
                    best=cur;
                    bestMove=m.move;
                }
                players[p].removeRecentWall();
            }
            
            
            if (d == 0) {
                if (bestMove.charAt(0) == 'P') {
                    if (bestMove.length() == 2) {
                        players[p].movePawn(bestMove.charAt(1));
                    } else {
                        players[p].jumpMove(bestMove.charAt(1), bestMove.charAt(2));
                    }
                } else {
                    players[p].setWall(bestMove.charAt(1)-'0', bestMove.charAt(2)-'0', bestMove.charAt(3)=='V');
                }
                System.out.println(bestMove);
            }
        } else {
            // return current values
            best=currentCost();
        } return best;
    }
    
    public int[] currentCost() {
        int[] best = new int[players.length];
        for (int i=0; i<players.length; i++) {
            best[i]=Math.abs(players[i].shortestPath());
        }
        return best;
    }
    
    int getCost(int[] values, int p) {
        int val=-(ROWS*COLS+1);
        for (int i=0; i<values.length; i++) {
            if (i!=p) {
                val=Math.max(values[p]-values[i], val);
            }
        } return val;
    }
    
    int compareCost(int[] best, int[] cur, int p) {
        int b=-(ROWS*COLS+1), c=b;
        for (int i=0; i<best.length; i++) {
            if (i!=p) {
                b=Math.max(best[p]-best[i], b);
                c=Math.max(cur[p]-cur[i], c);
            }
        } return b-c;
    }
    
    public void displayGoalDistances() {
        for (int p=0; p<players.length; p++) {
            System.out.printf("Player %d, goal distance = %d, shortest path = %d\n",p+1,players[p].g.distanceFromGoal(players[p].pawn.row, players[p].pawn.col), players[p].shortestPath());
        }
    }
    
    public boolean isUnblocked() {
        for (int p=0; p<players.length; p++) {
            if (players[p].shortestPath()==0) {
                return false;
            }
        } return true;
    }
    
    public static void main(String[] args) {
        Quoridor q = new Quoridor(2);
        q.play();
    }

    public void play() {
        int p=0;
        boolean[] human = new boolean[] {true, false, false, false};
        boolean end;
        do {
            displayStats();
            displayBoard();
            System.out.printf("Player %d to move\n",p+1);
            if (human[p]) {
                players[p].moveHuman();
            } else {
                if (0==0){
                    greedyDepthLim(p);
                } else {
                    depthLim(p);
                }
            }
            end=players[p].isWin(players[p].pawn.row, players[p].pawn.col);
            if (!end){
                p=(p+1)%players.length;
            }
        } while (!end);
        System.out.printf("Player %d Wins!\n", p+1);
    }
    
    public boolean isOver() {
        for (int p=0; p<players.length; p++) {
            if (players[p].isWin(players[p].pawn.row, players[p].pawn.col)) {
                return true;
            }
        } return false;
    }
    
    public boolean boundedPawn(int r, int c) {
        return r>-1 && c>-1 && r<ROWS && c<COLS;
    }
    
    public boolean boundedWall(int r, int c) {
        return r>-1 && c>-1 && r<ROWS-1 && c<COLS-1;
    }
    
    public boolean blockedWall(int row, int col, boolean v) {
        int r=0, c=0;
        if (v) {
            r=1;
        } else {
            c=1;
        }
        for (int p=0; p<players.length; p++) {
            for (int w=0; w<players[p].wallIndex; w++) {
                if (players[p].walls[w].isCoord(row,col,v) || players[p].walls[w].isCoord(row+r,col+c,v) || players[p].walls[w].isCoord(row-r,col-c,v) || players[p].walls[w].isCoord(row,col,!v)) {
                    return true;
                }
            }
        } return false;
    }
    
    public boolean blockedPawn(int row, int col, char dir) {
        return blockedPawn(row, col, dir, false);
    }
    
    public boolean blockedPawn(int row, int col, char dir, boolean override) {
        int r=0, c=0, ra=0, ca=0;
        boolean v=true;
        switch (dir) {
            case 'D':
                r=-1; v=false; break;
            case 'U':
                v=false; break;
            case 'R':
                c=-1; break;
        }
        if (v) {
            ra=-1;
        } else {
            ca=-1;
        }
        for (int p=0; p<players.length; p++) {
            if (!override && players[p].pawn.isCoord(row,col)) {
                return true;
            }
            for (int w=0; w<players[p].wallIndex; w++) {
                if (players[p].walls[w].isCoord(row+r,col+c,v) || players[p].walls[w].isCoord(row+r+ra,col+c+ca,v)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean blockedByPawn(int row, int col, char dir) {
        int r=0, c=0, ra=0, ca=0;
        boolean v=true;
        switch (dir) {
            case 'D':
                r=-1; v=false; break;
            case 'U':
                v=false; break;
            case 'R':
                c=-1; break;
        }
        if (v) {
            ra=-1;
        } else {
            ca=-1;
        }
        for (int p=0; p<players.length; p++) {
            if (players[p].pawn.isCoord(row,col)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean blockedByWall(int row, int col, char dir) {
        int r=0, c=0, ra=0, ca=0;
        boolean v=true;
        switch (dir) {
            case 'D':
                r=-1; v=false; break;
            case 'U':
                v=false; break;
            case 'R':
                c=-1; break;
        }
        if (v) {
            ra=-1;
        } else {
            ca=-1;
        }
        for (int p=0; p<players.length; p++) {
            for (int w=0; w<players[p].wallIndex; w++) {
                if (players[p].walls[w].isCoord(row+r,col+c,v) || players[p].walls[w].isCoord(row+r+ra,col+c+ca,v)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    class Player {
        Goal g;
        Coord pawn;
        Coord[] walls;
        int wallIndex;
        
        Player(int r, int c, int numWalls){
            pawn = new Coord(r,c);
            walls = new Coord[numWalls];
            for (int i=0; i<numWalls; i++) {
                walls[i] = new Coord();
            }
            wallIndex=0;
            if (r==ROWS-1) {
                g = new Goal(true, -1);
            } else if (r==0) {
                g = new Goal(true, ROWS);
            } else if (c==COLS-1) {
                g = new Goal(false, -1);
            } else {
                g = new Goal(false, COLS);
            }
        }
        
        boolean movePawn(char dir) {
            return movePawn(dir, false);
        }
        
        boolean movePawn(char dir, boolean override){
            dir=Character.toUpperCase(dir);
            int r=pawn.row, c=pawn.col;
            switch (dir) {
                case 'U':
                    r-=1; break;
                case 'D':
                    r+=1; break;
                case 'L':
                    c-=1; break;
                case 'R':
                    c+=1; break;
                default:
                    return false;
            }
            if (illegalPawn(r,c,dir, override)) {
                return false;
            }
            pawn.setCoord(r,c);
            return true;
        }
        
        boolean jumpMove(char dir1, char dir2) {
            int row=pawn.row, col=pawn.col;
            int r1=0, c1=0, r2=0, c2=0;
            switch (dir1) {
                case 'U':
                    r1=-1; break;
                case 'D':
                    r1=1; break;
                case 'L':
                    c1=-1; break;
                case 'R':
                    c1=1; break;
                default:
                    return false;
            } if (!blockedByPawn(row+r1,col+c1,dir1) || blockedByWall(row+r1,col+c1,dir2)) {
                return false;
            }
            
            switch (dir2) {
                case 'U':
                    r2=-1; break;
                case 'D':
                    r2=1; break;
                case 'L':
                    c2=-1; break;
                case 'R':
                    c2=1; break;
                default:
                    return false;
            } if (illegalPawn(row+r1+r2,col+c1+c2,dir2)) {
                return false;
            }
            
            if (dir1==dir2 || illegalPawn(row+r1+r1,col+c1+c1,dir1)) {
                pawn.setCoord(row+r1+r2,col+c1+c2);
                return true;
            }
            return false;
        }
        
        void displayMoveInstructions() {
            System.out.printf("Input N for number of walls remaining: \n\n");
            System.out.printf("Code for moving:\nP for pawn, W for wall.\n(if wall) 0-7 for row.\n(if wall) A-H for col.\nV for vertical, H for horizontal.\nExample: W6AV\n\n");
            System.out.printf("(if pawn) U = up, L = left, R = right, D = down\n(if pawn) if doing a jump input the direction of movement then the direction of the jump\nExample PL or PLU\n\n");
            System.out.printf("NOT case senstive.\nInput Move:  ");
        }
        
        int shortestPath() {
            Coord org = new Coord(pawn.row, pawn.col);
            LinkedList<Coord> visited = new LinkedList<Coord>(), frontier = new LinkedList<Coord>();
            char[] dir = new char[] {'D', 'U', 'R','L'};
            frontier.addFirst(new Coord (pawn.row, pawn.col, 0));
            while (frontier.size() > 0) {
                Coord cur=frontier.peekFirst();
                boolean unvisit=true;
                for (Coord c: visited) {
                    if (cur.isCoord(c.row, c.col)) {
                        frontier.removeFirst();
                        unvisit=false;
                        break;
                    }
                }
                if (unvisit) {
                    if (g.isGoal(cur.row, cur.col)) {
                        pawn.setCoord(org.row, org.col);
                        return cur.dist;
                    }
                    pawn.setCoord(cur.row, cur.col);
                    for (int i=0; i<4; i++) {
                        if (movePawn(dir[i])) {
                            frontier.addLast(new Coord(pawn.row, pawn.col, cur.dist+1));
                            pawn.setCoord(cur.row, cur.col);
                        }
                        for (int k=0; k<4; k++) {
                            if (jumpMove(dir[i],dir[k])) {
                                frontier.addLast(new Coord(pawn.row, pawn.col, cur.dist+1));
                                pawn.setCoord(cur.row, cur.col);
                            }
                        }
                    } visited.addLast(frontier.removeFirst());
                }
            }
            pawn.setCoord(org.row, org.col);
            return -auxShortestPath();    
        }
        
        int auxShortestPath() {
            Coord org = new Coord(pawn.row, pawn.col);
            LinkedList<Coord> visited = new LinkedList<Coord>(), frontier = new LinkedList<Coord>();
            char[] dir = new char[] {'D', 'U', 'R','L'};
            frontier.addFirst(new Coord (pawn.row, pawn.col, 0));
            while (frontier.size() > 0) {
                Coord cur=frontier.peekFirst();
                boolean unvisit=true;
                for (Coord c: visited) {
                    if (cur.isCoord(c.row, c.col)) {
                        frontier.removeFirst();
                        unvisit=false;
                        break;
                    }
                }
                if (unvisit) {
                    if (g.isGoal(cur.row, cur.col)) {
                        pawn.setCoord(org.row, org.col);
                        return cur.dist;
                    }
                    pawn.setCoord(cur.row, cur.col);
                    for (int i=0; i<4; i++) {
                        if (movePawn(dir[i], true)) {
                            frontier.addLast(new Coord(pawn.row, pawn.col, cur.dist+1));
                            pawn.setCoord(cur.row, cur.col);
                        }
                    } visited.addLast(frontier.removeFirst());
                }
            }
            pawn.setCoord(org.row, org.col);
            return 0;    
        }
        
        void moveHuman() {
            Scanner in = new Scanner(System.in);
            String s;
            System.out.printf("Input H for instructions, or input move: ");
            boolean legal;
            do {
                legal=false;
                s=in.nextLine();
                s=s.toUpperCase();
                try {
                    if (s.charAt(0)=='P') {
                        if (s.length()==2) {
                            legal=movePawn(s.charAt(1));
                        } else {
                            legal=jumpMove(s.charAt(1), s.charAt(2));
                        }
                    }
                    else if (s.charAt(0)=='W') {
                        boolean v=s.charAt(3)=='V';
                        legal=setWall((int) (s.charAt(1)-'0'), (int) (s.charAt(2)-'A'),v);
                    }
                    else if (s.charAt(0)=='H') {
                        displayMoveInstructions();
                    }
                    else if (s.charAt(0)=='N') {
                        System.out.printf("Walls remaining: %d\n", walls.length-wallIndex);
                    }
                } catch (Exception ex) {
                    System.out.println("Error: try again");
                }
            } while (!legal);
        }
        
        boolean setWall(int r, int c, boolean v) {
            if (illegalWall(r,c,v)) {
                return false;
            }
            walls[wallIndex++].setCoord(r,c,v);
            if (isUnblocked()) {
                return true;
            } else {
                walls[--wallIndex].setCoord(-1,-1,false);
                return false;
            }
        }
        
        boolean removeRecentWall() {
            if (wallIndex!=0) {
                walls[--wallIndex].setCoord(-1,-1,false);
                return true;
            } else {
                return false;
            }
        }
        
        boolean illegalWall(int r, int c, boolean v) {
            return wallIndex==walls.length || blockedWall(r,c,v) || !boundedWall(r,c);
        }
        
        boolean illegalPawn(int r, int c, char dir) {
            return illegalPawn(r, c, dir, false);
        }
        boolean illegalPawn(int r, int c, char dir, boolean override) {
            return !isWin(r, c) && (blockedPawn(r,c,dir, override) || !boundedPawn(r,c));
        }
        
        boolean isWin(int r, int c) {
            return g.isGoal(r,c);
        }
        
        void display() {
            System.out.printf("Pawn:  ");
            pawn.display();
            System.out.printf("Wall index = %d\n", wallIndex);
            for (int i=0; i<walls.length; i++) {
                System.out.printf("Wall %d:  ",i);
                walls[i].display();
            }
        }
        
        class Goal {
            boolean row;
            int num;
            Goal(boolean r, int n) {
                row=r;
                num=n;
            }
            boolean isGoal(int r, int c) {
                if (row) {
                    return r==num;
                } else {
                    return c==num;
                }
            }
            int distanceFromGoal(int r, int c) {
                if (row) {
                    return Math.abs(r-num);
                } else {
                    return Math.abs(c-num);
                }
            }
        }
    }
    

    
    
    class Coord {
        int row, col, dist;
        boolean vertical;
        Coord(int r, int c, boolean v){
            setCoord(r,c,v);
            setDist(0);
        }
        Coord(int r, int c, int d) {
            setCoord(r,c,false);
            setDist(d);
        }
        Coord(int r, int c) {
            this(r,c,false);
        }
        Coord(){
            this(-1,-1,false);
        }
        void setCoord(int r, int c) {
            setCoord(r,c,false);
        }
        void setCoord(int r, int c, boolean v) {
            row=r;
            col=c;
            vertical=v;
        }
        
        void setCoord(Coord c) {
            row=c.row;
            col=c.col;
            vertical=false;
        }
        
        boolean isCoord(int r, int c) {
            return isCoord(r,c,false);
        }
        
        boolean isCoord(int r, int c, boolean v) {
            return r==row && c==col && v==vertical;
        }
        
        void setDist(int d) {
            dist=d;
        }
        
        void display() {
            System.out.printf("row %d, col %d, is vertical %b\n",row,col,vertical);
        }
    }

    class MoveCost {
        String move;
        int cost;
        MoveCost(String m, int c) {
            move=m;
            cost=c;
        }
    }
    

}