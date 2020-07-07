package bgu.spl.net.api;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer,ConnectionHandler<T>> idPerClients;
    private int index=0;

    public ConnectionsImpl() {
        idPerClients=new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(idPerClients.get((Integer)connectionId) != null) {
            idPerClients.get((Integer)connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for(Integer I:idPerClients.keySet())
            idPerClients.get(I).send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
            idPerClients.remove(connectionId);
    }

    public void addClient(ConnectionHandler<T> connectionHandler,int index)
    {
        idPerClients.putIfAbsent(index,connectionHandler);
    }

}
