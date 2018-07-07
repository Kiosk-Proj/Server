package org.millburn.kiosk.tcp;

import org.millburn.kiosk.Server;
import org.millburn.kiosk.util.GGOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class KioskConnection extends Connection{
    public KioskConnection(Socket socket, int type, int id){
        super(socket, type, id);
    }

    @Override
    public void runInternal() throws IOException, SQLException{
        while(true){
            var message = Message.read(getSocket().getInputStream());
            int userid = message.getDataStream().readInt();
            getSocket().getOutputStream().write(1);

            var resultfuture = Server.getCurrent().getDatabase().query("");
            var resultset = resultfuture.getResults();

            if(resultset.getRows().size() == 0){
                var out = new GGOutputStream();
                out.write("");

                var outmessage = new Message(Message.NUMCONFIRM,
                            -1,
                            out.getData());
                outmessage.write(getSocket().getOutputStream());
            }else{
                var name = resultset.getRows().get(0).getString("name");
                var transaction = Server.getCurrent().createTransaction(userid, this.getId(), name);

                var out = new GGOutputStream();
                out.write(name);

                var outmessage = new Message(Message.NUMCONFIRM,
                        transaction.getTransactionId(),
                        out.getData());
                outmessage.write(getSocket().getOutputStream());
            }
        }
    }
}
