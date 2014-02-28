package com.myrdyr.blecontroller.demo;

/**
 * Created by myrdyr on 28.02.14.
 */
public class Robot {
    private boolean[] state = new boolean[4];
    public enum COMMAND {LEFT, RIGHT, UP, DOWN};
    private static final int CMD_DRIVE_DIR_POS = 0;
    private static final int CMD_TURN_DIR_POS = 1;
    private static final int CMD_TURN_CMD_POS = 2;
    private static final int CMD_DRIVE_CMD_POS = 3;
    private static final boolean DIR_LEFT = true;
    private static final boolean DIR_RIGHT = false;
    private static final boolean DIR_FORWARD = true;
    private static final boolean DIR_BACKWARD = false;

    public void updateCommand(COMMAND command)
    {
        /* 1. Driving and turning are mutually exclusive.
           2. Turning or driving in the same direction as it is moving will toggle it off.
         */
        switch (command) {
            case LEFT:
                state[CMD_DRIVE_CMD_POS] = false;                   /* Stop driving */
                if(state[CMD_TURN_CMD_POS] == true) {               /* Already turning...*/
                    if(state[CMD_TURN_DIR_POS] == DIR_LEFT) {       /* ...left */
                        state[CMD_TURN_CMD_POS] = false;            /* Stop turning */
                    }
                    else if(state[CMD_TURN_DIR_POS] == DIR_RIGHT) { /* ...right */
                        state[CMD_TURN_DIR_POS] = DIR_LEFT;         /* Swap directions */
                    }
                }
                else if(state[CMD_TURN_CMD_POS] == false) {         /* Not turning */
                    state[CMD_TURN_DIR_POS] = DIR_LEFT;             /* Set direction to left */
                    state[CMD_TURN_CMD_POS] = true;                 /* Start turning */
                }
                break;
            case RIGHT:
                state[CMD_DRIVE_CMD_POS] = false;                   /* Stop driving */
                if(state[CMD_TURN_CMD_POS] == true) {               /* Already turning...*/
                    if(state[CMD_TURN_DIR_POS] == DIR_LEFT) {       /* ... left */
                        state[CMD_TURN_DIR_POS] = DIR_RIGHT;         /* Swap directions */
                    }
                    else if(state[CMD_TURN_DIR_POS] == DIR_RIGHT) { /* ... right */
                        state[CMD_TURN_CMD_POS] = false;            /* Stop turning */
                    }
                }
                else {                                              /* Not turning */
                    state[CMD_TURN_DIR_POS] = DIR_RIGHT;            /* Set direction to right */
                    state[CMD_TURN_CMD_POS] = true;                 /* Start turning */
                }
                break;
            case UP:
                state[CMD_TURN_CMD_POS] = false;                    /* Stop turning */
                if(state[CMD_DRIVE_CMD_POS] == true) {              /* Already driving... */
                    if (state[CMD_DRIVE_DIR_POS] == DIR_FORWARD) {  /* ...forward */
                        state[CMD_DRIVE_CMD_POS] = false;
                        state[CMD_TURN_CMD_POS]  = false;           /* Stop completely */
                    }
                    else if (state[CMD_DRIVE_DIR_POS] == DIR_BACKWARD) {  /* ...backward */
                        state[CMD_DRIVE_DIR_POS]  = DIR_FORWARD;           /* Swap directions */
                    }
                }
                else {                                              /* Not driving */
                    state[CMD_DRIVE_DIR_POS] = DIR_FORWARD;         /* Set direction and drive */
                    state[CMD_DRIVE_CMD_POS] = true;
                }
                break;
            case DOWN:
                state[CMD_TURN_CMD_POS] = false;                    /* Stop turning */
                if(state[CMD_DRIVE_CMD_POS] == true) {              /* Already driving... */
                    if (state[CMD_DRIVE_DIR_POS] == DIR_BACKWARD) {  /* ...backward */
                        state[CMD_DRIVE_CMD_POS] = false;
                        state[CMD_TURN_CMD_POS]  = false;           /* Stop completely */
                    }
                    else if (state[CMD_DRIVE_DIR_POS] == DIR_FORWARD) {  /* ...forward */
                        state[CMD_DRIVE_DIR_POS]  = DIR_BACKWARD;   /* Swap directions */
                    }
                }
                else {                                              /* Not driving */
                    state[CMD_DRIVE_DIR_POS] = DIR_BACKWARD;         /* Set direction and drive */
                    state[CMD_DRIVE_CMD_POS] = true;
                }
                break;
        }
    }

    public int getCommand() {
        int command = 0;
        if (state[3])
            command |= (1 << 3);
        if (state[2])
            command |= (1 << 2);
        if (state[1])
            command |= (1 << 1);
        if (state[0])
            command |= (1 << 0);
        return command;
    }
}
