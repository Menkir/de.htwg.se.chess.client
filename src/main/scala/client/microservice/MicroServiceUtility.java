package client.microservice;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import scala.Tuple2;
import scala.collection.mutable.ListBuffer;

import java.nio.charset.Charset;
import java.util.LinkedList;

public final class MicroServiceUtility {

    private static final ActorSystem system = ActorSystem.create();
    private static final Materializer materializer = ActorMaterializer.create(system);
    private static final String BASE_URL = "http://localhost:8080/controller/";
    private static String makePrimitiveRequest(String relative){
        return Http.get(system)
                .singleRequest(HttpRequest.create(BASE_URL + relative))
                .toCompletableFuture()
                .join()
                .entity()
                .toStrict(1000, materializer)
                .toCompletableFuture().join()
                .getData()
                .decodeString(Charset.defaultCharset());
    }

    public static int getRound(){
        String  response = makePrimitiveRequest("round");
        return StringUnmarshallers.INTEGER.unmarshal(response, materializer)
                .toCompletableFuture()
                .join();
    }

    public static LinkedList<Tuple2<Integer, Integer>> getPossibleMoves(int x, int y){
        LinkedList<Tuple2<Integer, Integer>> result = new LinkedList<>();
        byte[] b = Http.get(system)
                .singleRequest(HttpRequest.create(BASE_URL + "possibleMoves?x=" + x + "&y=" + y))
                .toCompletableFuture()
                .join()
                .entity()
                .toStrict(1000, materializer)
                .toCompletableFuture()
                .join()
                .getData()
                .toArray();

        StringBuilder sb = new StringBuilder();
        for(byte v : b){
            sb.append((char) v);
        }

        String cords = sb.substring(1, sb.length()-1);
        ListBuffer<Tuple2> lel = new ListBuffer<>();

        for(int i = 0; i < cords.length(); i += 5){
            Integer n1 = -1;
            Integer n2 = -1;
            for (char n: cords.substring(i, i+5).toCharArray()){
                if(n1 == -1 && Character.isDigit(n)){
                    n1 = Character.getNumericValue(n);
                }else if(n2 == -1 && Character.isDigit(n))
                    n2 = Character.getNumericValue(n);
            }
            result.add(new Tuple2<>(n1, n2));
            i++;
        }
        return result;
    }

    public static String getCurrentPlayer(){
        String  response = makePrimitiveRequest("currentPlayer");
        return StringUnmarshallers.STRING.unmarshal(response, materializer)
                .toCompletableFuture()
                .join();
    }

    public static String getEnemyPlayer(){
        String  response = makePrimitiveRequest("enemyPlayer");
        return StringUnmarshallers.STRING.unmarshal(response, materializer)
                .toCompletableFuture()
                .join();
}

    public static String getPlayerA(){
        return StringUnmarshallers.STRING
                .unmarshal(makePrimitiveRequest("playerA"), materializer)
                .toCompletableFuture()
                .join();
    }

    public static String getPlayerB(){
        return StringUnmarshallers.STRING
                .unmarshal(makePrimitiveRequest("playerB"), materializer)
                .toCompletableFuture()
                .join();
    }

    public static boolean hasCurrentPlayerOccupied(int x, int y){
        return StringUnmarshallers.BOOLEAN
                .unmarshal(makePrimitiveRequest("currentPlayer/hasFigure?x=" + x + "&y=" + y), materializer)
                .toCompletableFuture()
                .join();
    }

    public static boolean hasEnemyPlayerOccupied(int x, int y){
        return StringUnmarshallers.BOOLEAN
                .unmarshal(makePrimitiveRequest("enemyPlayer/hasFigure?x=" + x + "&y=" + y), materializer)
                .toCompletableFuture()
                .join();
    }

    public static void move(Tuple2<Integer, Integer> src, Tuple2<Integer, Integer> trg){
        Http.get(system)
                .singleRequest(HttpRequest.create(BASE_URL + "move?v=" + src._1 +"&w=" + src._2 + "&x=" +trg._1+ "&y=" + trg._2));
    }

    public static Tuple2<Integer, Integer> getSource(){
        byte[] result = Http.get(system)
                .singleRequest(HttpRequest.create(BASE_URL + "source"))
                .toCompletableFuture()
                .join()
        .entity()
        .toStrict(1000, materializer)
        .toCompletableFuture()
        .join()
        .getData()
        .toArray();

        return constructTuple(result);
    }

    public static Tuple2<Integer, Integer> getTarget(){
        byte[] result = Http.get(system)
                .singleRequest(HttpRequest.create(BASE_URL + "target"))
                .toCompletableFuture()
                .join()
                .entity()
                .toStrict(1000, materializer)
                .toCompletableFuture()
                .join()
                .getData()
                .toArray();

        return constructTuple(result);
    }

    private static Tuple2<Integer, Integer> constructTuple(byte[] bytes){
        int x = -1;
        int y = -1;
        for(byte b : bytes){
            if(Character.isDigit((char) b))
                if(x == -1)
                    x = Character.getNumericValue((char) b);
                else
                    y = Character.getNumericValue((char) b);
        }
        return new Tuple2<>(x, y);
    }
}