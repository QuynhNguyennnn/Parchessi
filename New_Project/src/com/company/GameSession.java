package com.company;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class GameSession extends Game {

    private final int ONE_BONUS = -1;
    private final int NO_BONUS = 0;

    private int turn = 1, turnBonus = 0;
    private GameMap map;
    private boolean endGameFlag;
    private GameGraphic graphic;
    private Dice dice;
    private String[] PlayerName = {"","","",""};
    private int check = 0;
    final JFrame frame = new JFrame();

    GameSession() {
        setPlayerName();
        map = new GameMap();
        dice = new Dice();
        graphic = new GameGraphic();
        drawBackGround();
        endGameFlag = false;
        graphic.drawMap(map);
        graphic.drawControl(dice);
        playGame();
    }

    public void drawBackGround(){
        frame.setSize(910,710);
        JPanel background;
        Image image = new ImageIcon(GameGraphic.getImage("Background.png")).getImage();
        background = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, -20, 910 , 710 , this); //ve nen
            }
        };
        background.setPreferredSize(new Dimension(910, 710));
        background.setLayout(null);

        frame.setSize(910, 710);
        frame.setBackground(Color.black);
        frame.setTitle("Parcheesi");
        frame.setIconImage(new ImageIcon(GameGraphic.getImage("H2.png")).getImage());
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        URL url = GameGraphic.getImage("PlayButton.png");
        Image play = Toolkit.getDefaultToolkit().createImage(url);

        play = play.getScaledInstance(300, 150, Image.SCALE_SMOOTH);
        JButton playButton = new JButton();
        playButton.setIcon(new ImageIcon(play));
        playButton.setBounds(300,450, 300, 150);
        playButton.setBorder(new EmptyBorder(0,0,0,0));
        playButton.setContentAreaFilled(false);

        background.add(playButton);
        frame.add(background);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        playButton.addActionListener(e -> {
            frame.setVisible(false);
            graphic.drawFrame();
        });
    }

    /*public void setPlayerName(){
        JFrame jFrame = new JFrame("Nhập tên người chơi");
        JButton jButton = new JButton("Xác nhận");
        jButton.setBounds(150, 300, 100, 50);
        jFrame.add(jButton);
        JTextField t1,t2,t3,t4;
        jFrame.setBackground(Color.black);
        jFrame.setIconImage(new ImageIcon(GameGraphic.getImage("H2.png")).getImage());
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        t1=new JTextField("Player 1");
        t1.setBounds(130,50, 250,30);
        JLabel j1 = new JLabel();
        j1.setText("Nhập tên player 1:");
        j1.setBounds(20,50, 110,30);
        t2=new JTextField("Player 2");
        t2.setBounds(130,100, 250,30);
        JLabel j2 = new JLabel();
        j2.setText("Nhập tên player 2:");
        j2.setBounds(20,100, 110,30);
        t3=new JTextField("Player 3");
        t3.setBounds(130,150, 250,30);
        JLabel j3 = new JLabel();
        j3.setText("Nhập tên player 3:");
        j3.setBounds(20,150, 110,30);
        t4=new JTextField("Player 4");
        t4.setBounds(130,200, 250,30);
        JLabel j4 = new JLabel();
        j4.setText("Nhập tên player 4:");
        j4.setBounds(20,200, 110,30);
        jFrame.add(j1);
        jFrame.add(t1);
        jFrame.add(j2);
        jFrame.add(t2);
        jFrame.add(t3);
        jFrame.add(j3);
        jFrame.add(j4);
        jFrame.add(t4);

        jButton.addActionListener(e -> {
            while (true){
                PlayerName[0] = t1.getText();
                PlayerName[1] = t2.getText();
                PlayerName[2] = t3.getText();
                PlayerName[3] = t4.getText();
                for (int i = 0; i < 4; i++){
                    if (PlayerName[i].isEmpty())
                        setPlayerName();

                }
                break;
            }
            jFrame.setVisible(false);
            frame.setVisible(true);
        });
        jFrame.setSize(400,400);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while (true){
            if (PlayerName[0] != "Player 1" && !PlayerName[0].isEmpty())
                break;
            setPlayerName();
        }
    }*/

    public void setPlayerName(){
        int i = 0;
        while (i < 4) {
            String name = JOptionPane.showInputDialog(null, "Player "+ (i+1) +": ", JOptionPane.INFORMATION_MESSAGE);

            if (!name.isEmpty()) {
                PlayerName[i] = name;
                i++;
            } else {
                showError("Name cannot NULL!!!");
            }
        }
        while (true) {
            String strTurn = JOptionPane.showInputDialog(null, "Enter player go first (1/ 2/ 3/ 4): ", JOptionPane.INFORMATION_MESSAGE);
            if (strTurn.matches("[1234]")) {
                turn =  Integer.parseInt(strTurn);
                break;
            } else {
                showError("PLease enter from 1 to 4.");
            }
        }
    }

    public void playGame() {
        graphic.removeFrame();
        while (!endGameFlag) {
            int color = turn;
            turnBonus = NO_BONUS;
            graphic.drawNamePlayer(PlayerName[turn - 1]);
            graphic.drawTurnLabel(color);
            try {
                diePhaseSema.acquire();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            horsePhaseFlag = true;

            int steps = dice.getSteps();
            if (steps == 6 || steps == 1) {
                turnBonus = ONE_BONUS;
                graphic.drawXuatQuanButton(map, color);
            }

            map.addPlayerListener(color, steps);
            try {
                horsePhaseSema.acquire();
            } catch (InterruptedException exc) {
                System.out.println(exc);
            }

            map.removePlayerListener(color);
            graphic.removeXuatQuanButton();
            horsePhaseFlag = false;
            graphic.drawMap(map);

            diePhaseFlag = true;
            turn = (turn + turnBonus) % map.getNumberPlayer() + 1;

            if (map.isWin()) {
                endGameFlag = true;
            }
            graphic.removeNamePlayer();
        }
    }
}
