package io.xdag.net.libp2p;

import io.libp2p.core.multistream.ProtocolBinding;
import io.xdag.net.libp2p.manager.PeerManager;

public class NonHandler implements ProtocolBinding<?> {
    public NonHandler(PeerManager peerManager) {
    }
}
