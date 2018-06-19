package client.listener;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import client.client.Client;
import client.microservice.MicroServiceUtility;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletionStage;

/**
 * Created by kmg on 04.10.17.
 */
public class ButtonListener implements ActionListener {
    private Client client;

    public ButtonListener(Client client){
        this.client = client;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        client.getLp().setVisible(false);
        String playerA = client.getTxtFieldContent()[0];
        String playerB = client.getTxtFieldContent()[1];
        final ActorSystem system = ActorSystem.create();
        MicroServiceUtility.start(playerA, playerB);
        client.draw(client.getContentPane(), false);
        client.getContentPane().validate();


    }

}
