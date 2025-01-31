package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest httpRequest = HttpRequest.receive(br);
            HttpResponse httpResponse = new HttpResponse(dos);

            if (!httpRequest.hasSessionId()) {
                httpResponse.addHeader("Set-Cookie", "sessionId=" + UUID.randomUUID() + "; Path=/");
            }

            RequestMapping.runController(httpRequest, httpResponse);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
