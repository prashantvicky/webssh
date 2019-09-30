package com.cisco.webshell.ssh;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matchers;

public class WebSSH extends TextWebSocketHandler{

    private final SSHClient ssh = new SSHClient();
    private WebSocketSession session;
    private Session.Shell shell;
    private Expect expect;

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        if (!sshConnected()) {
            checkSshString(message);
            return;
        }

        executorService.submit(() -> {
            try {
                expect.sendLine(message.getPayload());
                session.sendMessage(new TextMessage("> OK.\n"));
                Result result = expect.expect(Matchers.anyString());
                if (result.isSuccessful())
                    session.sendMessage(new TextMessage(result.getInput()));
                else
                    session.sendMessage(new TextMessage(message.getPayload() + " was unsuccessful.."));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        exception.printStackTrace();
    }
    
    private void checkSshString(TextMessage message) throws Exception {
        String trim = message.getPayload();
        if (trim.startsWith("connect::")) {

            String replace = trim.replace("connect::", "");
            String[] split = replace.split("\\|");
            String user = split[0];
            String host = split[1];
            String port = split[2];
            String password = split[3];
            try {
            connectToSshServer(user, host, port, password);
            }catch(UnknownHostException u) {
            	session.sendMessage(new TextMessage("UnknownHostException"));	
            }

        }
    }

    private void connectToSshServer(String user, String host, String port, String password) throws Exception {

        ssh.addHostKeyVerifier((hostname, p, key) -> true);
        ssh.connect(host, Integer.parseInt(port));
        ssh.authPassword(user, password);
        shell = ssh.startSession().startShell();
        expect = new ExpectBuilder()
                .withOutput(shell.getOutputStream())
                .withInputs(shell.getInputStream(), shell.getErrorStream())
                .withTimeout(5, TimeUnit.SECONDS)
                .build();
        session.sendMessage(new TextMessage("connected::true"));

    }
    
    private boolean sshConnected() {
        return ssh.isConnected() && ssh.isAuthenticated();
    }

	
	
}
