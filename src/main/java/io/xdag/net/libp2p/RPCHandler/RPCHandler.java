/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.net.libp2p.RPCHandler;

import io.libp2p.core.Connection;
import io.libp2p.core.P2PChannel;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.multistream.ProtocolDescriptor;
import io.netty.channel.ChannelHandlerContext;
import io.xdag.Kernel;
import io.xdag.net.Channel;
import io.xdag.net.handler.MessageCodes;
import io.xdag.net.handler.Xdag03;
import io.xdag.net.libp2p.Libp2pChannel;
import io.xdag.net.libp2p.manager.PeerManager;
import io.xdag.net.manager.XdagChannelManager;
import io.xdag.net.message.MessageQueue;
import io.xdag.net.message.impl.Xdag03MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class RPCHandler implements ProtocolBinding<RPCHandler.Controller> {
    public Controller controller;
    Kernel kernel;
    Libp2pChannel libp2pChannel;
    BlockHandler blockHandler;
    XdagChannelManager channelManager;
    PeerManager peerManager;
    public RPCHandler(Kernel kernel, PeerManager peerManager) {
        this.kernel = kernel;
        this.peerManager = peerManager;
        this.channelManager = kernel.getChannelMgr();
    }
    @NotNull
    @Override
    public ProtocolDescriptor getProtocolDescriptor() {
        return  new ProtocolDescriptor("xdagj");
    }

    @NotNull
    @Override
    public CompletableFuture<Controller> initChannel(@NotNull P2PChannel p2PChannel, @NotNull String s) {
        final Connection connection = ((io.libp2p.core.Stream) p2PChannel).getConnection();
        peerManager.handleConnection(connection);
        libp2pChannel = new Libp2pChannel(connection,this);
        libp2pChannel.init();
        channelManager.add(libp2pChannel);
        blockHandler = new BlockHandler(libp2pChannel);
        blockHandler.setMessageFactory(new Xdag03MessageFactory());
        channelManager.onChannelActive(libp2pChannel,libp2pChannel.getNode());
        blockHandler.setMsgQueue(new MessageQueue(libp2pChannel));
        MessageCodes messageCodes = new MessageCodes();
        this.controller = new Controller(kernel,libp2pChannel);
        p2PChannel.pushHandler(blockHandler);
        p2PChannel.pushHandler(messageCodes);
        p2PChannel.pushHandler(controller);
        return controller.activeFuture;
    }
    public static class Controller extends Xdag03{

        public CompletableFuture<Controller> activeFuture;
        public XdagChannelManager channelManager;
        public Controller(Kernel kernel, Channel channel) {
            super(kernel, channel);
            channelManager = kernel.getChannelMgr();
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx){
            ctx.close().addListener(future -> channelManager.remove(channel));
            activeFuture.complete(this);
        }
    }

//    public static class Controller extends SimpleChannelInboundHandler<Message> implements Xdag{
//        protected final CompletableFuture<Controller> activeFuture = new CompletableFuture<>();
//        protected Blockchain blockchain;
//        protected SyncManager syncMgr;
//        protected Kernel kernel;
//        protected Libp2pChannel channel;
//        protected MessageQueue msgQueue;
//        protected XdagChannelManager channelManager;
//        public Controller(Kernel kernel,Libp2pChannel channel,BlockHandler blockHandler) {
//            this.kernel = kernel;
//            this.channel = channel;
//            this.blockchain = kernel.getBlockchain();
//            this.msgQueue = blockHandler.msgQueue;
//            this.syncMgr = kernel.getSyncMgr();
//            this.channelManager = kernel.getChannelMgr();
//        }
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx){
//            log.info("channelActive and remove channel");
//            ctx.close().addListener(future -> channelManager.remove(channel));
//        }
//        /**进来的最后一道，而且发送不在这处理*/
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
//            switch (msg.getCommand()) {
//                case NEW_BLOCK:
//                    processNewBlock((NewBlockMessage) msg);
//                    break;
//                case BLOCK_REQUEST:
//                    processBlockRequest((BlockRequestMessage) msg);
//                    break;
//                case BLOCKS_REQUEST:
//                    processBlocksRequest((BlocksRequestMessage) msg);
//                    break;
//                case BLOCKS_REPLY:
//                    processBlocksReply((BlocksReplyMessage) msg);
//                    break;
//                case SUMS_REQUEST:
//                    processSumsRequest((SumRequestMessage) msg);
//                    break;
//                case SUMS_REPLY:
//                    processSumsReply((SumReplyMessage) msg);
//                    break;
//                case BLOCKEXT_REQUEST:
//                    processBlockExtRequest((BlockExtRequestMessage) msg);
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        @Override
//        public void handlerAdded(ChannelHandlerContext ctx) {
//            log.info("handlerAdded ");
//            msgQueue.activate(ctx);
//        }
//
//        @Override
//        public void channelInactive(ChannelHandlerContext ctx) {
//            log.debug("channelInactive:[{}] ", ctx.toString());
//            this.killTimers();
//            disconnect();
//        }
//
//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//            log.debug("exceptionCaught:[{}]", cause.getMessage(), cause);
//            ctx.close();
//            killTimers();
//            disconnect();
//        }
//
//        @Override
//        public void dropConnection() {
//            log.info("Peer {}: is a bad one, drop", channel.getNode().getHexId());
//            disconnect();
//        }
//
//        public void killTimers() {
//            log.debug("msgQueue stop");
//            msgQueue.close();
//        }
//
//        /**
//         * ********************** Message Processing * ***********************
//         */
//        protected void processNewBlock(NewBlockMessage msg) {
//            Block block = msg.getBlock();
//            log.info("processNewBlock:{}", Hex.toHexString(block.getHashLow()));
//            BlockWrapper bw = new BlockWrapper(block, msg.getTtl() - 1, channel.getNode());
//            syncMgr.validateAndAddNewBlock(bw);
//        }
//
//        /**
//         * 区块请求响应一个区块 并开启一个线程不断发送一段时间内的区块 *
//         */
//        protected void processBlocksRequest(BlocksRequestMessage msg) {
//            log.debug("processBlocksRequest:" + msg);
//            updateXdagStats(msg);
//            long startTime = msg.getStarttime();
//            long endTime = msg.getEndtime();
//            long random = msg.getRandom();
//
//            List<Block> blocks = blockchain.getBlocksByTime(startTime, endTime);
//            for (Block block : blocks) {
//                sendNewBlock(block, 1);
//            }
//            sendMessage(new BlocksReplyMessage(startTime, endTime, random, kernel.getBlockchain().getXdagStats()));
//        }
//
//        protected void processBlocksReply(BlocksReplyMessage msg) {
////        log.debug("processBlocksReply:" + msg);
//            updateXdagStats(msg);
//            long randomSeq = msg.getRandom();
//            SettableFuture<byte[]> sf = kernel.getSync().getBlocksRequestMap().get(randomSeq);
//            if (sf != null) {
//                sf.set(new byte[]{0});
//            }
//        }
//
//        /**
//         * 将sumRequest的后8个字段填充为自己的sum 修改type类型为reply 发送
//         */
//        protected void processSumsRequest(SumRequestMessage msg) {
//            log.debug("processSumsRequest:" + msg);
//            updateXdagStats(msg);
//            byte[] sums = new byte[256];
//            kernel.getBlockStore().loadSum(msg.getStarttime(), msg.getEndtime(), sums);
//            SumReplyMessage reply = new SumReplyMessage(msg.getEndtime(), msg.getRandom(), kernel.getBlockchain().getXdagStats(), sums);
//            sendMessage(reply);
//            log.debug("processSumsRequest:" + reply);
//        }
//
//        protected void processSumsReply(SumReplyMessage msg) {
//            updateXdagStats(msg);
//            long randomSeq = msg.getRandom();
//            SettableFuture<byte[]> sf = kernel.getSync().getSumsRequestMap().get(randomSeq);
//            if (sf != null) {
//                sf.set(msg.getSum());
//            }
//        }
//
//        protected void processBlockExtRequest(BlockExtRequestMessage msg) {
//        }
//
//        protected void processBlockRequest(BlockRequestMessage msg) {
//            log.debug("processBlockRequest:" + msg);
//            byte[] find = new byte[32];
//            byte[] hash = msg.getHash();
//            hash = Arrays.reverse(hash);
//            System.arraycopy(hash, 8, find, 8, 24);
//            Block block = blockchain.getBlockByHash(find, true);
//            if (block != null) {
//                NewBlockMessage message = new NewBlockMessage(block, kernel.getConfig().getTTL());
//                sendMessage(message);
//            }
//        }
//
//        /**
//         * ********************** Message Sending * ***********************
//         */
//        @Override
//        public void sendNewBlock(Block newBlock, int TTL) {
//            log.debug("sendNewBlock:" + Hex.toHexString(newBlock.getHashLow()));
//            NewBlockMessage msg = new NewBlockMessage(newBlock, TTL);
//            sendMessage(msg);
//        }
//
//        @Override
//        public long sendGetBlocks(long startTime, long endTime) {
////        log.debug("sendGetBlocks:[startTime={} endTime={}]", startTime, endTime);
//            BlocksRequestMessage msg = new BlocksRequestMessage(startTime, endTime, kernel.getBlockchain().getXdagStats());
//            sendMessage(msg);
//            return msg.getRandom();
//        }
//
//        @Override
//        public boolean isIdle() {
//            return false;
//        }
//
//        @Override
//        public long sendGetBlock(byte[] hash) {
//            log.debug("sendGetBlock:[{}]", Hex.toHexString(hash));
//            BlockRequestMessage msg = new BlockRequestMessage(hash, kernel.getBlockchain().getXdagStats());
//            sendMessage(msg);
//            return msg.getRandom();
//        }
//
//        @Override
//        public long sendGetSums(long startTime, long endTime) {
////        log.debug("sendGetSums:startTime=[{}],endTime=[{}]", startTime, endTime);
//            SumRequestMessage msg = new SumRequestMessage(startTime, endTime, kernel.getBlockchain().getXdagStats());
//            sendMessage(msg);
//            return msg.getRandom();
//        }
//
//        @Override
//        public void sendMessage(Message message) {
//            if (msgQueue.isRunning()) {
//                msgQueue.sendMessage(message);
//            } else {
//                log.debug("msgQueue is close");
//            }
//        }
//
//        protected void disconnect() {
//            msgQueue.disconnect();
//        }
//
//        @Override
//        public void activate() {
//            log.debug("Xdag protocol activate");
//        }
//
//        @Override
//        public XdagVersion getVersion() {
//            return null;
//        }
//
//        @Override
//        public void disableBlocks() {
//            // TODO Auto-generated method stub
//        }
//
//        @Override
//        public void enableBlocks() {
//            // TODO Auto-generated method stub
//        }
//
//        @Override
//        public void onSyncDone(boolean done) {
//            // TODO Auto-generated method stub
//        }
//
//        public void updateXdagStats(AbstractMessage message) {
//            XdagStats remoteXdagStats = message.getXdagStats();
//            kernel.getBlockchain().getXdagStats().update(remoteXdagStats);
//            kernel.getNetDBMgr().updateNetDB(message.getNetDB());
//        }
//    }
}
