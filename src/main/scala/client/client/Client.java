package client.client;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import client.listener.ButtonListener;
import client.microservice.MicroServiceUtility;
import client.listener.ChessListener;
import scala.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static javax.swing.SwingConstants.CENTER;
import client.view.*;
import scala.Tuple3;

public class Client extends JFrame {
    final ActorSystem system = ActorSystem.create();
    final String BASE_URL = "http://localhost:8080/controller/";
    final Materializer materializer = ActorMaterializer.create(system);
    CompletionStage<HttpResponse> responseFuture;
    public void update(){
        int round = MicroServiceUtility.getRound();
        String playerAName = MicroServiceUtility.getPlayerA();
        String currentPlayer =  MicroServiceUtility.getCurrentPlayer();

        ROUND.setText("ROUND " + round);
        if(currentPlayer.equals(playerAName)){
            playerA.setBorder(BorderFactory.createBevelBorder(1));
            playerA.setBackground(new Color(0, 255, 68));
            playerB.setBorder(null);
            playerB.setBackground(null);
        }
        else {
            playerB.setBorder(BorderFactory.createBevelBorder(1));
            playerB.setBackground(new Color(0, 255, 68));
            playerA.setBorder(null);
            playerA.setBackground(null);
        }
        updateFigures();
    }

    private JLayeredPane gamefield;
    private JLayeredPane lp;
    private ChessListener chesslistener;
    private JPanel[][] referenceBackup = new JPanel[8][8];
    private JLabel ROUND, playerA, playerB;
    private Container c;

    private final int WHITE = 0;
    private final int BLACK = 1;

    private  JTextField txtPlayerA, txtPlayerB;

    private Client(){
        this.c = this.getContentPane();
        this.setSize(590, 620);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        lp = new JLayeredPane();
        Cover cover = new Cover(new ImageIcon(getClass().getResource("../images/chess.jpg")).getImage());
        JLabel header= new JLabel("Chess 0.0.0");
        header.setFont(new Font("ComicSans", Font.PLAIN, 50));
        header.setBounds(200, 120 ,300,50);

        txtPlayerA = new JTextField("PlayerA");
        txtPlayerB = new JTextField("PlayerB");
        txtPlayerA.setBounds(250,170,150,30);
        txtPlayerB.setBounds(250,210,150,30);

        JButton btnStart = new JButton("Start");
        btnStart.setBounds(250,300,100,50);

        btnStart.addActionListener(new ButtonListener(this));

        lp.add(cover, JLayeredPane.DEFAULT_LAYER);
        lp.add(header, new Integer(JLayeredPane.DEFAULT_LAYER+1));
        lp.add(txtPlayerA, new Integer(JLayeredPane.DEFAULT_LAYER+1));
        lp.add(txtPlayerB, new Integer(JLayeredPane.DEFAULT_LAYER+1));
        lp.add(btnStart, new Integer(JLayeredPane.DEFAULT_LAYER+1));

        JMenuBar menu = new JMenuBar();
        JMenu menuentry = new JMenu("Menu");
        JMenuItem load = new JMenuItem(new AbstractAction("load") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String session = javax.swing.JOptionPane.showInputDialog(null, "Session ID: ");
                getLp().setVisible(false);
                if(gamefield != null)
                    gamefield.removeAll();
                draw(c, true);
                MicroServiceUtility.load(session);
                load(MicroServiceUtility.download());
            }
        });
        JMenuItem save = new JMenuItem(new AbstractAction("save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int d = javax.swing.JOptionPane.showConfirmDialog(null, "Wollen sie wirklich speichern?");
                if(d == 0){
                    MicroServiceUtility.save();
                }
            }
        });
        JMenuItem newgame = new JMenuItem(new AbstractAction("new game") {
            @Override
            public void actionPerformed(ActionEvent e) {
                getLp().setVisible(true);
                if(gamefield != null)
                    gamefield.removeAll();
            }
        });


        menuentry.add(load);
        menuentry.addSeparator();
        menuentry.add(save);
        menuentry.addSeparator();
        menuentry.add(newgame);
        menu.add(menuentry);

        this.add(lp);
        this.setJMenuBar(menu);
        this.setResizable(false);
        this.setVisible(true);

        if(MicroServiceUtility.controller_instantiated()){
            lp.setVisible(false);
            draw(this.getContentPane(), true);
            load(MicroServiceUtility.download());
            this.getContentPane().validate();
            this.repaint();
        }
    }

    public JLayeredPane getLp(){return lp;}
    public JLayeredPane getGamefieldLp(){return gamefield;}

    public String[] getTxtFieldContent(){
        return new String[]{txtPlayerA.getText(), txtPlayerB.getText()};
    }


    public  void draw(Container c, boolean session_loaded){
        playerA = new JLabel();
        playerB = new JLabel();
        ROUND = new JLabel();
        gamefield = new JLayeredPane();

        if(session_loaded){
            playerA.setText("Player: " + MicroServiceUtility.getPlayerA());
            playerB.setText("Player: " + MicroServiceUtility.getPlayerB());
            ROUND.setText("ROUND " + MicroServiceUtility.getRound());
        }else{
            playerA.setText("Player: " + txtPlayerA.getText());
            playerB.setText("Player: " + txtPlayerB.getText());
            ROUND.setText("ROUND " + 1);
        }
        ROUND.setBounds(20, 40, 150, 30);
        ROUND.setFont(new Font("ComicScans", Font.BOLD, 30));
        playerA.setBounds(200, 40, 200, 30);
        playerB.setBounds(200, 530, 200, 30);
        playerA.setBorder(BorderFactory.createBevelBorder(1));
        playerB.setBorder(BorderFactory.createBevelBorder(1));

        if(MicroServiceUtility.getPlayerA().equals(MicroServiceUtility.getCurrentPlayer()))
            playerA.setBackground(new Color(0, 255, 68));
        else
            playerB.setBackground(new Color(0, 255, 68));


        gamefield.add(ROUND);
        gamefield.add(playerA);
        gamefield.add(playerB);
        chesslistener = new ChessListener(this);
        gamefield.setLayout(null);
        gamefield.setPreferredSize(new Dimension(600,600));
        build(session_loaded);
        c.add(gamefield);
    }

    private  void build(boolean session_loaded){
        int ct = 0;
        int size = 50;
        int pos = 100;
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){

                Square beige = new Square(255, 235, 205);
                beige.setBounds(pos+ i*size,pos+j*size,size,size);
                beige.addMouseListener(chesslistener);

                Square brown = new Square(139, 69, 19);
                brown.setBounds(pos+i*size,pos+j*size,size,size);
                brown.addMouseListener(chesslistener);


                if(ct % 2 == 0) {
                    gamefield.add(beige, JLayeredPane.DEFAULT_LAYER);
                }
                else{
                    gamefield.add(brown, JLayeredPane.DEFAULT_LAYER);
                }

                if(session_loaded){
                    if(j == 7)
                        ct += 2;
                    else ++ct;
                    continue;
                }

                Pawn bauer;
                if(j == 1 || j == 6){
                    if(j == 1)
                        bauer = new Pawn(WHITE);
                    else
                        bauer = new Pawn(BLACK);
                    bauer.setBounds(pos+i*size,pos+j*size,size,size);
                    bauer.addMouseListener(chesslistener);
                    gamefield.add(bauer, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = bauer;
                }

                Rook turm;
                if((i == 0 || i == 7) && j == 0){
                    turm = new Rook(WHITE);
                    turm.setBounds(pos+i*size,pos+j*size,size,size);
                    turm.addMouseListener(chesslistener);
                    gamefield.add(turm, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = turm;
                }else if((i == 0 || i == 7) && j == 7){
                    turm = new Rook(BLACK);
                    turm.setBounds(pos+i*size,pos+j*size,size,size);
                    turm.addMouseListener(chesslistener);
                    gamefield.add(turm, JLayeredPane.DEFAULT_LAYER.intValue());
                }

                Knight knight;
                if((i == 1 || i == 6) && j == 0){
                    knight = new Knight(WHITE);
                    knight.setBounds(pos+i*size,pos+j*size,size,size);
                    knight.addMouseListener(chesslistener);
                    gamefield.add(knight, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = knight;
                }else if((i == 1 || i == 6) && j == 7){
                    knight = new Knight(BLACK);
                    knight.setBounds(pos+i*size,pos+j*size,size,size);
                    knight.addMouseListener(chesslistener);
                    gamefield.add(knight, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = knight;
                }

                Bishop bishop;
                if((i == 2 || i == 5) && j == 0){
                    bishop= new Bishop(WHITE);
                    bishop.setBounds(pos+i*size,pos+j*size,size,size);
                    bishop.addMouseListener(chesslistener);
                    gamefield.add(bishop, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = bishop;
                }else if((i == 2 || i == 5) && j == 7){
                    bishop= new Bishop(BLACK);
                    bishop.setBounds(pos+i*size,pos+j*size,size,size);
                    bishop.addMouseListener(chesslistener);
                    gamefield.add(bishop, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = bishop;
                }

                King king;
                if(i == 3 && j == 0){
                    king = new King(WHITE);
                    king.setBounds(pos+i*size,pos+j*size,size,size);
                    king.addMouseListener(chesslistener);
                    gamefield.add(king, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = king;
                } else if(i == 3 && j == 7){
                    king = new King(BLACK);
                    king.setBounds(pos+i*size,pos+j*size,size,size);
                    king.addMouseListener(chesslistener);
                    gamefield.add(king, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = king;
                }

                Queen queen;
                if(i == 4 && j == 0){
                    queen = new Queen(WHITE);
                    queen.setBounds(pos+i*size,pos+j*size,size,size);
                    queen.addMouseListener(chesslistener);
                    gamefield.add(queen, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = queen;
                } else if(i == 4 && j == 7){
                    queen = new Queen(BLACK);
                    queen.setBounds(pos+i*size,pos+j*size,size,size);
                    queen.addMouseListener(chesslistener);
                    gamefield.add(queen, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = queen;
                }

                if(j == 7)
                    ct += 2;
                else ++ct;

            }
        }
    }

    private void updateFigures(){
        int size = 50;
        Tuple2<Integer, Integer> source = MicroServiceUtility.getSource();
        Tuple2<Integer, Integer> target = MicroServiceUtility.getTarget();
        if(!isValid(source) ||!isValid(target))
            return;
        referenceBackup[source._2][source._1].setBounds(100+target._2*size, 100+target._1*size, size, size);

        //update reference backup table
        if(referenceBackup[target._2][target._1] != null){
            gamefield.remove(referenceBackup[target._2][target._2]); //remove figure jpanel
        }
        referenceBackup[target._2][target._1] =  referenceBackup[source._1][source._2];
        referenceBackup[source._2][source._1] = null;
        this.c.repaint();
        this.c.revalidate();
    }

    private boolean isValid(Tuple2<Integer, Integer> t){
        return t._1 >= 0 && t._1 <= 7 && t._2 >= 0 && t._2 <= 7;
    }

    public JPanel[][] getReferenceBackup() {
        return referenceBackup;
    }

    private void load(Map<String, ArrayList<Tuple3<String, Integer, Integer>>> m){
        //clean reference backup table
        for(int i = 0; i < referenceBackup.length; ++i){
            for(int j = 0; j < referenceBackup.length; ++j){
                if(referenceBackup[i][j] != null)
                gamefield.remove(referenceBackup[i][j]);
            }
        }
        //set new playerNames
        String pa = MicroServiceUtility.getPlayerA();
        String pb = MicroServiceUtility.getPlayerB();
        this.playerA.setText("Player: " + pa);
        this.playerB.setText("Player: " + pb);
        this.ROUND.setText("ROUND " + MicroServiceUtility.getRound());

        set_figure_jpanel(m, WHITE, pa);
        set_figure_jpanel(m, BLACK, pb);

        this.getContentPane().repaint();
        this.getContentPane().validate();
    }


    private void set_figure_jpanel(Map<String, ArrayList<Tuple3<String, Integer, Integer>>> m, int color, String player){

        for(int i = 0; i < m.get(player).size(); ++i){
            Object e = m.get(player).get(i);
            String designator= (String)((ArrayList) e).get(0);
            Integer y= (Integer)((ArrayList) e).get(1);
            Integer x= (Integer)((ArrayList) e).get(2);
            switch(color){
                case WHITE: referenceBackup[x][y] = getFigure(designator, WHITE);
                    break;
                case BLACK: referenceBackup[x][y] = getFigure(designator, BLACK);
                    break;
            }
            referenceBackup[x][y].setBounds(100+x*50,100+y*50,50,50);
            referenceBackup[x][y].addMouseListener(chesslistener);
            referenceBackup[x][y].validate();
            gamefield.add(referenceBackup[x][y], JLayeredPane.DEFAULT_LAYER.intValue());
        }

    }

    private JPanel getFigure(String designator, int color){
        switch (designator){
            case "B":
                return new Pawn(color);
            case "T":
                return new Rook(color);
            case "K":
                return new King(color);
            case "D":
                return new Queen(color);
            case "L":
                return new Knight(color);
            case "O":
                return new Bishop(color);
        }
        return null;
    }

    public static void main(String[] args){
        new Client();
    }

}
