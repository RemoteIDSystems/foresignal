package ai.foresignal.source.aggregator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A loopback TCP server standing in for a local decoder in the aggregator tests (prompt R2-021): it writes the same
 * bytes to every client that connects, every 200 ms, until the client or the server closes.
 */
final class LoopbackFeed implements AutoCloseable {

    private final ServerSocket server;
    private final List<Socket> clients = new CopyOnWriteArrayList<>();

    /**
     * Starts the server on an ephemeral loopback port.
     *
     * @param payload what each client receives, repeatedly
     * @throws IOException when the port cannot be bound
     */
    LoopbackFeed(byte[] payload) throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    Socket client = server.accept();
                    clients.add(client);
                    Thread.ofVirtual().start(() -> feed(client, payload));
                } catch (IOException e) {
                    return;
                }
            }
        });
    }

    private static void feed(Socket client, byte[] payload) {
        try (client) {
            OutputStream out = client.getOutputStream();
            while (!client.isClosed()) {
                out.write(payload);
                out.flush();
                Thread.sleep(200);
            }
        } catch (IOException e) {
            // The reader closed its connection.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** The bound port. */
    int port() {
        return server.getLocalPort();
    }

    /** Closes the server and every client. */
    @Override
    public void close() throws IOException {
        server.close();
        for (Socket client : clients) {
            client.close();
        }
    }
}
