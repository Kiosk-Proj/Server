package org.millburn.kiosk.tcp;

import org.millburn.kiosk.Server;
import org.millburn.kiosk.util.GGOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;

public class KioskConnection extends Connection{
    public KioskConnection(Socket socket, int type, int id){
        super(socket, type, id);
    }

    @Override
    public void runInternal() throws IOException, SQLException{
        try {
            while (true) {
                var message = Message.read(getSocket().getInputStream());
                int userid = message.getDataStream().readInt();

                System.out.println("Tried " + userid);



                var resultfuture = Server.getCurrent().getDatabase().query("SELECT * FROM allstudents WHERE id=" + userid + ";");
                //Message.sendAcknowledgement(getSocket().getOutputStream());

                var resultset = resultfuture.getResults();
                if (resultset.getRows().size() == 0) {
                    var out = new GGOutputStream();
                    out.write("");

                    var outmessage = new Message(Message.NUMCONFIRM,
                            -1,
                            out.getData());
                    outmessage.write(getSocket().getOutputStream());

                    log.debug("Kiosk " + getId() + " requested ID " + userid + ", no student found");
                } else {
                    var name = resultset.getRows().get(0).getString("personname");
                    var transaction = Server.getCurrent().createTransaction(userid, this.getId(), name);

                    var out = new GGOutputStream();
                    out.write(name);

                    var outmessage = new Message(Message.NUMCONFIRM,
                            transaction.getTransactionId(),
                            out.getData());

                    outmessage.write(getSocket().getOutputStream());

                    log.debug("Kiosk " + getId() + " requested ID " + userid + ", found student " + name);


                }
                Message.read(getSocket().getInputStream());
            }
        }catch(IOException e){
            log.warn("Kiosk " + this.getId() + " closed connection: " + e.getMessage());
            e.printStackTrace();
        }finally{
            getSocket().close();
        }
    }
}
