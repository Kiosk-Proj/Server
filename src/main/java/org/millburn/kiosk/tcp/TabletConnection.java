package org.millburn.kiosk.tcp;

import org.millburn.kiosk.Executor;
import org.millburn.kiosk.Server;
import org.millburn.kiosk.Transaction;
import org.millburn.kiosk.util.GGOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;

public class TabletConnection extends Connection{
    private final Object lock = new Object();
    private Transaction next = null;

    public TabletConnection(Socket socket, int type, int id){
        super(socket, type, id);
    }

    @Override
    public void runInternal(){
        Thread t1 = new Thread(this::send);
        Thread t2 = new Thread(this::receive);

        t1.setDaemon(true);
        t2.setDaemon(true);

        t1.start();
        t2.start();
    }

    private void send(){
        while(true){
            synchronized(lock){
                try{
                    lock.wait();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }

            try{
                var transaction = this.next;
                var out = new GGOutputStream();
                out.write(transaction.getUserId());
                out.write(transaction.getName());

                var message = new Message(Message.TABLETUPDATE, transaction.getTransactionId(), out.getData());
                message.write(getSocket().getOutputStream());

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void receive(){
        while(true){
            try{
                var message = Message.read(getSocket().getInputStream());
                var transactionid = message.transationId;
                var accept = message.getDataStream().readInt();

                var transaction = Server.getCurrent().getTransaction(transactionid);

                if(transaction.getState().equals(Transaction.State.EXISTS)){
                    transaction.setState(Transaction.State.CONFIRMED);
                    Executor.in(60 * 1000, () -> Server.getCurrent().processTransaction(transaction));
                }

                boolean accepted = accept != 0;
                transaction.setValidated(accepted);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
