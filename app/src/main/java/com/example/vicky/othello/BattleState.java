package com.example.vicky.othello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WTIFS-Mac on 7/11/15.
 */
public class BattleState {
    public Integer[][] point_state;
    public ArrayList<Point> available_points;
    public ArrayList<Point>[][] future_steps;
    public int[] counts;

    public BattleState(){
        point_state = new Integer[8][8];
        available_points = new ArrayList<>();
        future_steps = new ArrayList[8][8];
    }

    public BattleState(Integer[][] point_state, ArrayList<Point> available_points, ArrayList[][] future_steps, int[] counts){
        this.point_state = new Integer[8][8];
        int black = Battle.colors[0];
        int white = Battle.colors[1];
        for (int i=0; i<8; i++)
            for (int j=0; j<8; j++) {
                if (point_state[i][j]==black || point_state[i][j]==white) this.point_state[i][j] = point_state[i][j];
                else this.point_state[i][j] = Battle.colors[3];
            }

        this.available_points = available_points;
        this.future_steps = future_steps;
        this.counts = counts;

        this.counts = counts.clone();
    }

}
