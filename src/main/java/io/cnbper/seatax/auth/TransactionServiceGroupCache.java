package io.cnbper.seatax.auth;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.ConcurrentSet;

public class TransactionServiceGroupCache {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionServiceGroupCache.class);

    private static ConcurrentSet<String> serviceGroupSet = new ConcurrentSet<>();
    private static String serviceGroupSetMD5 = "";

    public static boolean contains(String serviceGroup) {
        return serviceGroupSet.contains(serviceGroup);
    }

    private Thread refreshThread = null;
    private Refresh refresh;

    public void refresh() {
        // 调用分布式事务控制台API，获取分组信息
        Set<String> newSet = null;
        // 序列化json，计算MD5
        String newServiceGroupSetMD5 = "";
        
        refreshServiceGroupSet(newServiceGroupSetMD5, newSet);
    }

    private void refreshServiceGroupSet(String newServiceGroupSetMD5, Set<String> newSet) {
        if (serviceGroupSetMD5.equals(newServiceGroupSetMD5)) {
            return;
        }
        synchronized (serviceGroupSetMD5) {
            if (newSet == null || newSet.isEmpty()) {
                serviceGroupSet.clear();
            } else {
                Set<String> waitdelSet = new HashSet<String>(); // 待删除数据
                serviceGroupSet.forEach(serviceGroup -> {
                    if (newSet.contains(serviceGroup)) {
                        newSet.remove(serviceGroup);
                    } else {
                        waitdelSet.add(serviceGroup);
                    }
                });

                waitdelSet.forEach(serviceGroup -> {
                    serviceGroupSet.remove(serviceGroup);
                });
            }
            serviceGroupSetMD5 = newServiceGroupSetMD5;
        }
    }

    public void start() {
        synchronized (this) {
            shutdown();
            refresh = new Refresh(this);
            refreshThread = new Thread(refresh);
            refreshThread.setDaemon(true);
            refreshThread.setName("TransactionServiceGroupCacheRefresh");
            refreshThread.start();
        }
    }

    public void shutdown() {
        synchronized (this) {
            if (refresh != null) {
                refresh.shutdown();
                refresh = null;
            }
            if (refreshThread != null) {
                refreshThread.interrupt();
                refreshThread = null;
            }
        }
    }
}

class Refresh implements Runnable {

    volatile boolean shutdown = false;
    TransactionServiceGroupCache serviceGroupCache;

    public Refresh(TransactionServiceGroupCache serviceGroupCache) {
        this.serviceGroupCache = serviceGroupCache;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void run() {
        while (true) {
            if (shutdown) {
                return;
            }
            try {
                this.serviceGroupCache.refresh();
            } catch (Throwable e) {
                TransactionServiceGroupCache.logger.error("failed to sync transaction-servicegroup, reason: {}",
                                e.getMessage(), e);
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }
        }
    }
}