package org.trading.discovery;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Nodes {
    private final List<String> nodes = new CopyOnWriteArrayList<>();

    public void setHealthyNodes(List<String> healthyNodes) {
        nodes.clear();
        nodes.addAll(healthyNodes);
    }

    public List<String> getNodes() {
        return nodes;
    }

}
