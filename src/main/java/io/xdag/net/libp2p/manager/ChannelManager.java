package io.xdag.net.libp2p.manager;///*
// * The MIT License (MIT)
// *
// * Copyright (c) 2020-2030 The XdagJ Developers
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// */
//package io.xdag.net.libp2p.manager;
//
//import io.xdag.core.BlockWrapper;
//import io.xdag.net.Channel;
//import io.xdag.net.node.Node;
//import lombok.extern.slf4j.Slf4j;
//
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
//
//@Slf4j
//public class ChannelManager {
//    protected ConcurrentHashMap<InetSocketAddress, Channel> channels = new ConcurrentHashMap<>();
//    protected ConcurrentHashMap<String, Channel> activeChannels = new ConcurrentHashMap<>();
//    /** Queue with new blocks from other peers */
//    private BlockingQueue<BlockWrapper> newForeignBlocks = new LinkedBlockingQueue<>();
//    // 广播区块
//    private Thread blockDistributeThread;
//
//    public ChannelManager( ) {
//        this.blockDistributeThread = new Thread(this::newBlocksDistributeLoop, "NewSyncThreadBlocks");
//        blockDistributeThread.start();
//    }
//
//    private void newBlocksDistributeLoop() {
//        while (!Thread.currentThread().isInterrupted()) {
//            BlockWrapper wrapper = null;
//            try {
//                wrapper = newForeignBlocks.take();
//                sendNewBlock(wrapper);
//            } catch (InterruptedException e) {
//                break;
//            } catch (Throwable e) {
//                if (wrapper != null) {
//                    log.error("Block dump: {}", wrapper.getBlock());
//                } else {
//                    log.error("Error broadcasting unknown block", e);
//                }
//            }
//        }
//    }
//
//    public void sendNewBlock(BlockWrapper blockWrapper) {
//
//        Node receive = null;
//        // 说明是自己产生的
//        if (blockWrapper.getRemoteNode() != null) {
//            Channel receiveChannel = activeChannels.get(blockWrapper.getRemoteNode().getHexId());
//            receive = receiveChannel != null ? receiveChannel.getNode() : null;
//        }
//        //广播
//        for (Channel channel : activeChannels.values()) {
//            if (receive != null && channel.getNode().getHexId().equals(receive.getHexId())) {
//                log.debug("不发送给他");
//                continue;
//            }
//            log.debug("发送给除receive的节点");
//            channel.sendNewBlock(blockWrapper);
//        }
//    }
//    public void onChannelActive(Channel channel, Node node){
//        channel.setActive(true);
//        activeChannels.put(node.getHexId(), channel);
//        log.info("activeChannel size:"+ activeChannels.size());
//    }
//    public void add(Channel ch){
//        channels.put(ch.getNode().getAddress(), ch);
//    }
//
//    public void remove(Channel ch) {
//        log.debug("Channel removed: remoteAddress = {}", ch.getIp());
//        channels.remove(ch.getIp());
//        if (ch.isActive()) {
//            activeChannels.remove(ch.getIp());
//            ch.setActive(false);
//        }
//    }
//
//    public void onNewForeignBlock(BlockWrapper blockWrapper) {
//        newForeignBlocks.add(blockWrapper);
//    }
//
//    public List<Channel> getactiveChannel(){
//        Collection<Channel> channels =activeChannels.values();
//        log.debug("Active Channels {}", channels.size());
//        return new ArrayList<>(activeChannels.values());
//    }
//    public void stop() {
//        log.debug("Channel Manager stop...");
//        if (blockDistributeThread != null) {
//            // 中断
//            blockDistributeThread.interrupt();
//        }
//        // 关闭所有连接
//        for (Channel channel : activeChannels.values()) {
//            channel.onDisconnect();
//        }
//    }
//}
