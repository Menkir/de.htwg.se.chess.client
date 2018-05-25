package client.listener;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import client.client.Client;

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
        final String BASE_URL = "http://localhost:8080/controller/";
        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.create(BASE_URL + "start?playerOne=" + playerA + "&playerTwo=" + playerB));


        client.draw(client.getContentPane());
        client.getContentPane().validate();


    }

}
