package org.millburn.kiosk.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.millburn.kiosk.http.StudentLogPair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void sendLogin(StudentLogPair pair){
        for(WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(pair)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMessage emp = objectMapper.readValue(message.getPayload(), ResponseMessage.class);


    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}