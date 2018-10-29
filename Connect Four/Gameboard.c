#include <stdio.h>
#include <stdlib.h>
#include "Gameboard.h"

static struct Gameboard new() {
    struct Gameboard gb;
    return gb;
}

const struct GameboardClass Gameboard={.new=&new};

