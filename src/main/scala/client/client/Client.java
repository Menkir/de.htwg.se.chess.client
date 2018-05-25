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
import java.util.concurrent.CompletionStage;

import static javax.swing.SwingConstants.CENTER;
import client.view.*;
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

    private int WHITE = 0;
    private int BLACK = 1;

    private  JTextField txtPlayerA, txtPlayerB;

    private Client(){

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
        menu.add(new JMenu("Menu"));

        this.add(lp);
        this.setJMenuBar(menu);
        this.setResizable(false);
        this.setVisible(true);
    }

    public JLayeredPane getLp(){return lp;}
    public JLayeredPane getGamefieldLp(){return gamefield;}

    public String[] getTxtFieldContent(){
        return new String[]{txtPlayerA.getText(), txtPlayerB.getText()};
    }


    public  void draw(Container c){
        gamefield = new JLayeredPane();
        //ROUND = new JLabel("ROUND " + controller.round());
        ROUND = new JLabel("ROUND " + 1);
        ROUND.setBounds(20, 40, 150, 30);
        ROUND.setFont(new Font("ComicScans", Font.BOLD, 30));
        playerA = new JLabel("Player: " + MicroServiceUtility.getPlayerA(), CENTER);
        playerA.setBounds(200, 40, 200, 30);
        playerA.setBorder(BorderFactory.createBevelBorder(1));
        playerA.setBackground(new Color(0, 255, 68));
        playerB = new JLabel("Player: " + MicroServiceUtility.getPlayerB(), CENTER);
        playerB.setBounds(200, 530, 200, 30);


        gamefield.add(ROUND);
        gamefield.add(playerA);
        gamefield.add(playerB);
        chesslistener = new ChessListener(this);
        gamefield.setLayout(null);
        gamefield.setPreferredSize(new Dimension(600,600));
        build();
        c.add(gamefield);
    }

    private  void build(){
        int ct = 0;
        int size = 50;
        int pos = 100;
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){

                Tile beige = new Tile(255, 235, 205);
                beige.setBounds(pos+ i*size,pos+j*size,size,size);
                beige.addMouseListener(chesslistener);

                Tile brown = new Tile(139, 69, 19);
                brown.setBounds(pos+i*size,pos+j*size,size,size);
                brown.addMouseListener(chesslistener);


                if(ct % 2 == 0) {
                    gamefield.add(beige, JLayeredPane.DEFAULT_LAYER);
                }
                else{
                    gamefield.add(brown, JLayeredPane.DEFAULT_LAYER);
                }
                BauerTile bauer;
                if(j == 1 || j == 6){
                    if(j == 1)
                        bauer = new BauerTile(WHITE);
                    else
                        bauer = new BauerTile(BLACK);
                    bauer.setBounds(pos+i*size,pos+j*size,size,size);
                    bauer.addMouseListener(chesslistener);
                    gamefield.add(bauer, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = bauer;
                }

                TurmTile turm;
                if((i == 0 || i == 7) && j == 0){
                    turm = new TurmTile(WHITE);
                    turm.setBounds(pos+i*size,pos+j*size,size,size);
                    turm.addMouseListener(chesslistener);
                    gamefield.add(turm, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = turm;
                }else if((i == 0 || i == 7) && j == 7){
                    turm = new TurmTile(BLACK);
                    turm.setBounds(pos+i*size,pos+j*size,size,size);
                    turm.addMouseListener(chesslistener);
                    gamefield.add(turm, JLayeredPane.DEFAULT_LAYER.intValue());
                }

                LaeuferTile laeufer;
                if((i == 1 || i == 6) && j == 0){
                    laeufer = new LaeuferTile(WHITE);
                    laeufer.setBounds(pos+i*size,pos+j*size,size,size);
                    laeufer.addMouseListener(chesslistener);
                    gamefield.add(laeufer, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = laeufer;
                }else if((i == 1 || i == 6) && j == 7){
                    laeufer = new LaeuferTile(BLACK);
                    laeufer.setBounds(pos+i*size,pos+j*size,size,size);
                    laeufer.addMouseListener(chesslistener);
                    gamefield.add(laeufer, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = laeufer;
                }

                OffizierTile offizier;
                if((i == 2 || i == 5) && j == 0){
                    offizier= new OffizierTile(WHITE);
                    offizier.setBounds(pos+i*size,pos+j*size,size,size);
                    offizier.addMouseListener(chesslistener);
                    gamefield.add(offizier, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = offizier;
                }else if((i == 2 || i == 5) && j == 7){
                    offizier= new OffizierTile(BLACK);
                    offizier.setBounds(pos+i*size,pos+j*size,size,size);
                    offizier.addMouseListener(chesslistener);
                    gamefield.add(offizier, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = offizier;
                }

                KoenigTile koenig;
                if(i == 3 && j == 0){
                    koenig = new KoenigTile(WHITE);
                    koenig.setBounds(pos+i*size,pos+j*size,size,size);
                    koenig.addMouseListener(chesslistener);
                    gamefield.add(koenig, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = koenig;
                } else if(i == 3 && j == 7){
                    koenig = new KoenigTile(BLACK);
                    koenig.setBounds(pos+i*size,pos+j*size,size,size);
                    koenig.addMouseListener(chesslistener);
                    gamefield.add(koenig, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = koenig;
                }

                DameTile dame;
                if(i == 4 && j == 0){
                    dame = new DameTile(WHITE);
                    dame.setBounds(pos+i*size,pos+j*size,size,size);
                    dame.addMouseListener(chesslistener);
                    gamefield.add(dame, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = dame;
                } else if(i == 4 && j == 7){
                    dame = new DameTile(BLACK);
                    dame.setBounds(pos+i*size,pos+j*size,size,size);
                    dame.addMouseListener(chesslistener);
                    gamefield.add(dame, JLayeredPane.DEFAULT_LAYER.intValue());
                    referenceBackup[j][i] = dame;
                }

                if(j == 7)
                    ct += 2;
                else ++ct;

            }
        }
    }

    private void updateFigures(){
        System.out.println("[updateFigures]");
        int size = 50;
        Tuple2<Integer, Integer> source = MicroServiceUtility.getSource();
        Tuple2<Integer, Integer> target = MicroServiceUtility.getTarget();
        System.out.println("Source " + source);
        System.out.println("Target " + target);
        try{
            referenceBackup[source._1][source._2].setBounds(100+target._2*size, 100+target._1*size, size, size);
        }catch(NullPointerException e){
            System.out.println("dough! there is no JPanel at [" + source._1 + ", " + source._2 + "]");
        }

        //update reference backup table
        if(referenceBackup[target._1][target._2] != null){
            gamefield.remove(referenceBackup[target._1][target._2]); //remove figure jpanel
        }
        referenceBackup[target._1][target._2] =  referenceBackup[source._1][source._2];
        referenceBackup[source._1][source._2] = null;

    }

    public JPanel[][] getReferenceBackup() {
        return referenceBackup;
    }

    public static void main(String[] args){
        new Client();
    }

    public void print(){
        for(int i = 0; i< 8; ++i){
            for(int j = 0; j< 8; ++j){
                try{
                    System.out.print("full ");
                }catch(NullPointerException e){
                    System.out.print("null ");
                }
            }
            System.out.println();
        }
    }
}
