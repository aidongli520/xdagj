package io.xdag.net;

import io.netty.channel.socket.nio.NioSocketChannel;
import io.xdag.core.BlockWrapper;
import io.xdag.net.handler.Xdag;
import io.xdag.net.node.Node;

import java.net.InetSocketAddress;

public abstract class Channel {
    protected NioSocketChannel socket;
    protected InetSocketAddress inetSocketAddress;
    protected boolean isActive;
    protected boolean isDisconnected = false;
    /** 该channel对应的节点 */
    protected Node node;

    public abstract InetSocketAddress getInetSocketAddress();

    public abstract void setActive(boolean b);

    public abstract boolean isActive();

    public abstract Node getNode();

    public abstract String getIp();

    public abstract void sendNewBlock(BlockWrapper blockWrapper);

    public abstract void onDisconnect();

    public abstract int getPort();

    public abstract void dropConnection();

    public abstract Xdag getXdag();

    public abstract boolean isDisconnected();
}
