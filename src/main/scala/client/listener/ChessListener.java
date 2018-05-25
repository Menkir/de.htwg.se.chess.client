package client.listener;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import client.client.Client;
import client.microservice.MicroServiceUtility;
import scala.Tuple2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.concurrent.CompletionStage;


public class ChessListener implements MouseListener {

    private boolean hasSelectedSource = false;
    private Tuple2<Integer, Integer> source;
    private Tuple2<Integer, Integer> target;
    private Client client;
    private LinkedList<JPanel> moeglicheZuege;
    final ActorSystem system = ActorSystem.create();
    final String BASE_URL = "http://localhost:8080/controller/";
    final Materializer materializer = ActorMaterializer.create(system);
    CompletionStage<HttpResponse> responseFuture;
    LinkedList<Tuple2<Integer, Integer>> coordiantes = new LinkedList<>();
    public ChessListener(Client client){

        this.client = client;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        int x,y = 0;
        int size = 50;
        int default_pos = 100;

        if(!hasSelectedSource) {
            Rectangle pos = ((JPanel) e.getSource()).getBounds();
            x = (pos.x - default_pos) / size;
            y = (pos.y - default_pos) / size;

            source = new Tuple2<>(y, x);
            hasSelectedSource = true;

            moeglicheZuege = new LinkedList<>();

            coordiantes = MicroServiceUtility.getPossibleMoves(source._1, source._2);
            for(Tuple2<Integer, Integer>  t: coordiantes){
                int posx = (int) t._2();
                int posy = (int) t._1();
                if(posx < 0 || posy < 0 || posx > 7 || posy > 7)
                    continue;

                Boolean hasOwnFigure = MicroServiceUtility.hasCurrentPlayerOccupied(posx, posy);
                if(hasOwnFigure)
                    continue;
                Boolean enemyHasFigure = MicroServiceUtility.hasEnemyPlayerOccupied(posy, posx);
                if(enemyHasFigure) {
                    client.getReferenceBackup()[posy][posx].setBackground(new Color(255,255,0));
                    client.getReferenceBackup()[posy][posx].updateUI();
                    client.getReferenceBackup()[posy][posx].setOpaque(true);
                    continue;
                }

                JPanel panel = new JPanel();
                panel.setBounds(100+50*posx, 100+50*posy, 50,50);
                panel.setBackground(new Color(255, 255, 0));
                panel.setBorder(BorderFactory.createBevelBorder(1));
                moeglicheZuege.add(panel);
                client.getGamefieldLp().add(panel, 10);

            }

            client.getGamefieldLp().revalidate();
        }else{
            for(JPanel zug: moeglicheZuege){
                client.getGamefieldLp().remove(zug);
            }
            client.getGamefieldLp().revalidate();
            client.getGamefieldLp().repaint();

            Rectangle pos = ((JPanel) e.getSource()).getBounds();
            x = (pos.x - default_pos) / size;
            y = (pos.y - default_pos) / size;

            target=  new Tuple2<>(y, x);
            
            if(coordiantes.contains(target)){
                MicroServiceUtility.move(source, target);
            }
            hasSelectedSource = false;

            client.update();
            source = null;
            target = null;

        }

    }
    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
