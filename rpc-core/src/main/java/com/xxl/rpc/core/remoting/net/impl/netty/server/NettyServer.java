package com.xxl.rpc.core.remoting.net.impl.netty.server;

import com.xxl.rpc.core.remoting.net.Server;
import com.xxl.rpc.core.remoting.net.impl.netty.codec.NettyDecoder;
import com.xxl.rpc.core.remoting.net.impl.netty.codec.NettyEncoder;
import com.xxl.rpc.core.remoting.net.params.Beat;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import com.xxl.rpc.core.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty rpc server
 *
 * @author mzj 2015-10-29 18:17:14
 */
@Slf4j
public class NettyServer extends Server {

    private Thread thread;

    @Override
    public void start(final RpcProviderFactory xxlRpcProviderFactory) throws Exception {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // param
                final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                        NettyServer.class.getSimpleName(),
                        xxlRpcProviderFactory.getCorePoolSize(),
                        xxlRpcProviderFactory.getMaxPoolSize()
                );
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    // start server
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL * 3, TimeUnit.SECONDS))     // beat 3N, close if idle
                                            .addLast(new NettyDecoder(RpcRequest.class, xxlRpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyEncoder(RpcResponse.class, xxlRpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyServerHandler(xxlRpcProviderFactory, serverHandlerPool));
                                }
                            })
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    // bind
                    ChannelFuture future = bootstrap.bind(xxlRpcProviderFactory.getPort()).sync();

                    log.info(">>>>>>>>>>> rpc remoting server start success, nettype = {}, port = {}", NettyServer.class.getName(), xxlRpcProviderFactory.getPort());
                    onStarted();

                    // wait util stop
                    future.channel().closeFuture().sync();

                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        log.info(">>>>>>>>>>> rpc remoting server stop.");
                    } else {
                        log.error(">>>>>>>>>>> rpc remoting server error.", e);
                    }
                } finally {
                    // stop
                    try {
                        serverHandlerPool.shutdown();    // shutdownNow
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                }
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void stop() throws Exception {

        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        // on stop
        onStoped();
        log.info(">>>>>>>>>>> rpc remoting server destroy success.");
    }

}
