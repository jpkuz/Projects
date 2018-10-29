#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#define R 6
#define C 7


struct Gameboard {
   int board[R][C];
};

void init(struct Gameboard *this) {
    for (int r=0; r<R; r++) {
        for (int c=0; c<C; c++) {
            this->board[r][c] = 0;
        }
    }
}

void display(struct Gameboard *this) {
    for (int r=0; r<R; r++) {
        for (int c=0; c<C; c++) {
            printf("| %d ", this->board[r][c]);
        } printf("|\n");
    }
    printf("-----------------------------\n");
    for (int c=0; c<C; c++) {
       printf("  %d ", c);
    } printf("\n\n\n");
}

bool dropChip(struct Gameboard *this, int col, int player) {
    for (int r=0; r<R; r++) {
         if (this->board[r][col]!=0) {
             if (r!=0){
                 this->board[r-1][col]=player;
                 return true;
             } else {
                 return false;
             }
         }
    }
    this->board[R-1][col]=player;
    return true;
}

void cpybrd(struct Gameboard *gb, struct Gameboard *tgt) {
    for (int r=0; r<R; r++) {
         for (int c=0; c<C; c++) {
              tgt->board[r][c] = gb->board[r][c];
         } 
    }
}

char *getGameMode() {
   printf("Game Modes: 'PvP', 'PvC', 'CvP', 'CvC'\n");
   printf("Enter Game Mode: ");
   char s[4], *r;
   r=(char *)malloc(4*sizeof(char));
   scanf("%s", s);
   while (strcmp("PvP", s)!=0 && strcmp("PvC", s)!=0 && strcmp("CvC", s)!=0 && strcmp("CvP", s)!=0) {
      printf("\nError: invalid game mode.\nGame modes are 'PvP', 'PvC', and 'CvC' WITHOUT quotations.\nEnter valid game mode: ");
      scanf("%s", s);
   }
   strcpy(r, s);
   return r;
}

int addVal(bool b) {
   return b?1:-1;
}

int *getValue(struct Gameboard *gb, int p) {
    int *val;
    val = (int *) malloc(3*sizeof(int));
    for (int i=0; i<3; i++) {val[i]=0;}
    for (int row=0; row<R; row++) {
        for (int col=0; col<C; col++) {
            int chip = gb->board[row][col];
            if (chip!=0) {
               for (int r=row+1; r<R && r-row<4; r++) {
                  if (gb->board[r][col]==chip) {
                     val[r-row-1]+=addVal(p==chip);
                  } else {
                     break;
                  }
               } 
               for (int c=col+1; c<C && c-col<4; c++) {
                  if (gb->board[row][c]==chip) {
                     val[c-col-1]+=addVal(p==chip);
                  } else {
                     break;
                  }
               } 
               for (int i=1; row+i<R && col+i<C && i<4; i++) {
                  if (gb->board[row+i][col+i]==chip) {
                     val[i-1]+=addVal(p==chip);
                  } else {
                     break;
                  }
               } 
               for (int i=1; row+i<R && col-i>-1 && i<4; i++) {
                  if (gb->board[row+i][col-i]==chip) {
                     val[i-1]+=addVal(p==chip);
                  } else {
                     break;
                  }
               }
            }
        }
    }
    return val;
}

void displayVal(struct Gameboard *gb, int p) {
    int *v;
    v=getValue(gb, p);
    for (int i=0; i<3; i++) {
       printf("%d ", v[i]);
    } printf("\n");
}

int getPlayerMove(int p){
   int col;
   do {
       printf("Player %d, input column 0-6:   ", p);
       scanf("%d", &col);
   } while (col>6 || col<0);
   return col;
}

int compareVal(int *val1, int *val2) {
    for (int i=2; i>-1; i--) {
        if (val1[i]!=val2[i]) {
            return (int) (val1[i]-val2[i]*pow(1000,i));
        }
     } return 0;
}

bool isFull(struct Gameboard *gb) {
   for (int c=0; c<C; c++) {
       if (gb->board[0][c]==0) {
          return false;
       }
   } return true;
}

bool isOver(struct Gameboard *gb) {
   int *v;
   v=getValue(gb, 1); 
   return (v[2] != 0 || isFull(gb));
}


int *search(struct Gameboard *gb, int p, int i) {
    int *best, *current, bestCol;
    struct Gameboard tgb;
    best=malloc(4*sizeof(int));
    current=malloc(4*sizeof(int));
    best[3]=-1;

    if (i<7 && !isOver(gb)) {
       for (int c=0; c<C; c++) {
           cpybrd(gb, &tgb);
           if (dropChip(&tgb, c, p)) {
               current=search(&tgb, p%2+1, i+1);
               if (compareVal(current, best) > 0 || best[3]==-1) {
                   for (int k=0; k<3; k++) {
                       best[k]=current[k];
                   } best[3]=c;
               }
           }
       }
    } else {
       current=getValue(gb, p);
       for (int k=0; k<3; k++) {
          best[k]=current[k];
       }
       best[3]=0;
    }

    for (int k=0; k<3; k++) {
       best[k]=-best[k];
    } return best;
        

}

int getCompMove(struct Gameboard *gb, int p) {
    int *m;
    m=search(gb, p, 0);
    printf("Computer moved: %d\n", m[3]);
    return m[3];
}

void play() {
    char *s;
    s=getGameMode();
    struct Gameboard gb;
    init(&gb);
    int col, p=2;
    bool b, isHuman[2];

    isHuman[0]=(s[0]=='P' ? true : false);
    isHuman[1]=(s[2]=='P' ? true : false);


    do {
       p=p%2+1;
       display(&gb);
       do {
          col=(isHuman[p-1] ? getPlayerMove(p) : getCompMove(&gb, p));
          b=dropChip(&gb, col, p);
       } while(!b);  
    } while (!isOver(&gb));
    display(&gb);
    if (isFull(&gb)) {
        printf("draw\n");
    } else {
        printf("Player %d wins!\n", p);
    }
}


int main() {
   char c;
   do {
     play();
     printf("'R' to restart, 'Q' to quit:   ");
     do {
        scanf("%c", &c);
     } while (c!='R' && c!='r' && c!='Q' && c!='q');
   } while (c=='R' || c=='r');
}