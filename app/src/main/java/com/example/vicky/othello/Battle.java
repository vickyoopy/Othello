package com.example.vicky.othello;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;


public class Battle extends AppCompatActivity {

    public Integer[][] mThumbIds;

    public GridView gridview;

    public ProgressBar progress_black;
    public ProgressBar progress_white;
    public TextView progress_b;
    public TextView progress_w;
    public ImageView current_player;

    public Button btn_regret;
    public Button btn_new_game;
    public Button btn_hint;

    public int current_turn; //0:black; 1:white
    public static final Integer[] colors = new Integer[]{R.drawable.b, R.drawable.w, R.drawable.b, R.drawable.t}; //0:black; 1:white
    public int[] counts;

    ArrayList<Point> available_points; //存放 下一步 哪些可走
    ArrayList<Point>[][] future_steps; //存放 如果在(x,y)放子，哪些坐标会变色 (变色图)

    Stack<BattleState> history;

    static boolean hint_on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battlegrid);

        btn_regret = (Button)findViewById(R.id.buttonRegret);
        btn_new_game = (Button)findViewById(R.id.buttonNewgame);
        btn_hint = (Button)findViewById(R.id.buttonShowHint);

        progress_black = (ProgressBar) findViewById(R.id.progressBarBlack);
        progress_white = (ProgressBar) findViewById(R.id.progressBarWhite);

        progress_b = (TextView)findViewById(R.id.text_b);
        progress_w = (TextView)findViewById(R.id.text_w);

        current_player = (ImageView) findViewById(R.id.current_player);

        gridview = (GridView) findViewById(R.id.gridview);

        gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridview.setAdapter(new ImageAdapter(this));

        //initUI
        init();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                int x=position%8;
                int y=position/8;
                if (!available_points.isEmpty()){//有棋可下
                    if (future_steps[x][y]!=null && !future_steps[x][y].isEmpty()) {//不是非法区域
                        flip(x, y);
                        changeTurn();
                        checkSolution();
                        updateUI();

                    }
                }
            }
        });

        btn_regret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (history.size() < 1) return;

                BattleState battle_state = history.pop();

                current_turn = (current_turn + 1) % 2;
                available_points = battle_state.available_points;
                future_steps = battle_state.future_steps;
                counts = battle_state.counts.clone();
                for (int i = 0; i < 8; i++)
                    mThumbIds[i] = Arrays.copyOf(battle_state.point_state[i], 8);

                current_player.setImageResource(colors[current_turn]);

                updateUI();
            }
        });

        btn_new_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext()).setTitle("New Game")
                        .setMessage("Start a new game?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                init();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        })
                        .show();
            }
        });

        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hint_on) showHint();
                else hideHint();
            }
        });
    }

    void init(){
        mThumbIds = new Integer[8][8];
        for (int i=0;i<8;i++)
            for (int j=0;j<8;j++)
            mThumbIds[i][j]=R.drawable.t;
        
        mThumbIds[3][3]=R.drawable.w;
        mThumbIds[4][3]=R.drawable.b;
        mThumbIds[3][4]=R.drawable.b;
        mThumbIds[4][4]=R.drawable.w;

        current_turn = 0;
        current_player.setImageResource(R.drawable.b);

        counts = new int[]{2, 2};

        progress_black.getBackground().setAlpha(200);
        progress_white.getBackground().setAlpha(200);
        progress_black.setMax(64);
        progress_white.setMax(64);

        available_points = new ArrayList<>();
        future_steps = new ArrayList[8][8];
        history = new Stack<>();

        hint_on = false;
        btn_regret.setEnabled(true);
        btn_hint.setEnabled(true);
        gridview.setEnabled(true);

        calcAvailableArea(0);
        updateUI();
    }

    void updateUI() {
        ((ImageAdapter) gridview.getAdapter()).notifyDataSetChanged();

        progress_black.setProgress(counts[0]);
        progress_white.setProgress(counts[1]);
        progress_b.setText(counts[0] + "");
        progress_w.setText(counts[1] + "");
    }

    //计算可放置位置
    public boolean calcAvailableArea(int turn){
        available_points = new ArrayList<>();
        future_steps = new ArrayList[8][8];
        for (int i=0; i<8; i++)
            for (int j=0; j<8; j++) {
                if (canPut(turn, i, j))
                    available_points.add(new Point(i,j));
            }
        return !available_points.isEmpty();
    }

    public void showHint(){
        if (!available_points.isEmpty()) {
            hint_on = true;
            for (int i = 0; i < available_points.size(); i++) {
                Point point = available_points.get(i);
                if (current_turn == 0)
                    mThumbIds[point.x][point.y] = R.drawable.tmp_b;
                else mThumbIds[point.x][point.y] = R.drawable.tmp_w;
            }
            updateUI();
        }
    }

    public void hideHint(){
        if (!available_points.isEmpty()) {
            hint_on = false;
            for (int i = 0; i < available_points.size(); i++) {
                Point point = available_points.get(i);
                mThumbIds[point.x][point.y] = R.drawable.t;
            }
            updateUI();
        }
    }

    public void checkSolution(){
        if (calcAvailableArea(current_turn)) return;
        else {//无棋可下
            if (calcAvailableArea(nextTurn())) {//另一方有棋
                String[] user_color = new String[]{"Black", "White", "Black"};
                String msg = String.format("%s can't go any further more, turn to %s", user_color[current_turn], user_color[current_turn + 1]);

                new AlertDialog.Builder(this).setTitle("No available points")
                        .setMessage(msg)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                changeTurn();
                            }
                        })
                        .show();
            } else {//双方均无棋可下
                String msg = "";
                if (counts[0] > counts[1]) msg = "Black wins out!";
                else if (counts[0] < counts[1]) msg = "White wins out!";
                else msg = "Draw.";
                new AlertDialog.Builder(this).setTitle(msg)
                        .setMessage("Want to start a new game?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                init();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                lock();
                                return;
                            }
                        })
                        .show();
            }
        }
    }

    //判断某个点能否放置
    public boolean canPut(int turn, int x, int y){
        int myColor = colors[turn];
        int oppositeColor = colors[turn+1];
        if (mThumbIds[x][y]==myColor || mThumbIds[x][y]==oppositeColor) return false;

        Integer[][] p = mThumbIds;
        future_steps[x][y] = new ArrayList<>();
        int i;

        //left top
        if (x>=1 && y>=1 && p[x-1][y-1]==oppositeColor) {
            i = 2;
            while (x>=i && y>=i && p[x-i][y-i] == oppositeColor) i++;
            if (x>=i && y>=i && p[x-i][y-i] == myColor){
                for (int j=1; j<i; j++)
                    future_steps[x][y].add(new Point(x-j, y-j));
            }
        }

        //top
        if (y-1>=0 && p[x][y-1]==oppositeColor) {
            i = 2;
            while (y-i>=0 && p[x][y-i]==oppositeColor) i++;
            if (y-i>=0 && p[x][y-i]==myColor) {
                for (int j = 1; j < i; j++)
                    future_steps[x][y].add(new Point(x, y-j));
            }
        }

        //right top
        if (x+1<8 && y-1>=0 && p[x+1][y-1]==oppositeColor) {
            i = 2;
            while (x+i<8 && y-i>=0 && p[x+i][y-i] == oppositeColor) i++;
            if (x+i<8 && y-i>=0 && p[x+i][y-i] == myColor){
                for (int j = 1; j < i; j++)
                    future_steps[x][y].add(new Point(x+j, y-j));
            }
        }

        //left
        if (x-1>=0 && p[x-1][y]==oppositeColor) {
            i = 2;
            while (x-i>= 0 && p[x-i][y] == oppositeColor) i++;
            if (x-i>= 0 && p[x-i][y] == myColor) {
                for (int j = 1; j < i; j++)
                    future_steps[x][y].add(new Point(x-j, y));
            }
        }

        //right
        if (x+1<8 && p[x+1][y]==oppositeColor) {
            i = 2;
            while (x+i<8 && p[x+i][y] == oppositeColor) i++;
            if (x+i<8 && p[x+i][y] == myColor) {
                for (int j = 1; j < i; j++)
                    future_steps[x][y].add(new Point(x+j, y));
            }
        }

        //left bottom
        if (x-1>=0 && y+1<8 && p[x-1][y+1]==oppositeColor){
            i = 2;
            while (x-i>=0 && y+i<8 && p[x-i][y+i]==oppositeColor) i++;
            if (x-i>=0 && y+i<8 && p[x-i][y+i]==myColor)
                for (int j=1; j<i; j++)
                    future_steps[x][y].add(new Point(x-j, y+j));
        }

        //bottom
        if (y+1<8 && p[x][y+1]==oppositeColor){
            i = 2;
            while (y+i<8 && p[x][y+i]==oppositeColor) i++;
            if (y+i<8 && p[x][y+i]==myColor)
                for (int j=1; j<i; j++)
                    future_steps[x][y].add(new Point(x, y+j));
        }

        //right bottom
        if (x+1<8 && y+1<8 && p[x+1][y+1]==oppositeColor){
            i = 2;
            while (x+i<8 && y+i<8 && p[x+i][y+i]==oppositeColor) i++;
            if (x+i<8 && y+i<8 && p[x+i][y+i]==myColor)
                for (int j=1; j<i; j++)
                    future_steps[x][y].add(new Point(x+j, y+j));
        }

        if (future_steps[x][y].size()>0) return true;
        else return false;
    }

    //翻转棋子
    public void flip(int x, int y){
        Integer[][] p = mThumbIds;
        int myColor = colors[current_turn];

        //动棋子之前，保存当前棋盘状态
        BattleState battleState = new BattleState(p, available_points, future_steps, counts);
        history.push(battleState);

        int affected_count = future_steps[x][y].size();
        p[x][y]=myColor;
        for (int i=0; i<affected_count; i++) {
            Point interval_point = future_steps[x][y].get(i);
            p[interval_point.x][interval_point.y] = myColor;
        }

        counts[current_turn] += affected_count+1;
        counts[nextTurn()] -= affected_count;

        //hint区域变回透明
        if (hint_on) {
            for (int i = 0; i < available_points.size(); i++) {
                Point point = available_points.get(i);
                if (point.x != x || point.y != y)
                    p[point.x][point.y] = colors[3];
            }
            hint_on = false;
        }

        current_player.setImageResource(colors[nextTurn()]);
    }

    //轮
    public void changeTurn(){
        current_turn = nextTurn();
    }

    public int nextTurn(){
        return (current_turn + 1) % 2;
    }

    public void lock(){
        btn_regret.setEnabled(false);
        btn_hint.setEnabled(false);
        gridview.setEnabled(false);
    }

}






