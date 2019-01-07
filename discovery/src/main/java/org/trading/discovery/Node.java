package org.trading.discovery;

public class Node {
    public final String id;
    public final String address;
    public final int port;

    public Node(String id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }
}
