
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.BGSProtocol;
import bgu.spl.net.api.MessageEncoderDecoderImp;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.dataBase;

import java.io.IOException;

import static java.lang.Integer.parseInt;

public class TPCMain {

    public static void main(String[] args) throws IOException {

        dataBase dataBase = new dataBase();

        Server server = Server.threadPerClient(parseInt(args[0]), () -> new BGSProtocol(dataBase), () -> new MessageEncoderDecoderImp());
        server.serve();
    }
}

