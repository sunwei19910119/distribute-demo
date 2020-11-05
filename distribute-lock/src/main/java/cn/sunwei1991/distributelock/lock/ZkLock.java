package cn.sunwei1991.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ZkLock implements Watcher,AutoCloseable {

    private ZooKeeper zooKeeper;
    private String businessName;
    private String znode;

    public ZkLock(String connectString,String businessName) throws IOException {
        this.zooKeeper = new ZooKeeper(connectString,30000,this);
        this.businessName = businessName;
    }

    public boolean getLock() throws KeeperException, InterruptedException {
        Stat existsNode = zooKeeper.exists("/" + businessName, false);
        //创建业务根节点
        if (existsNode == null){
            zooKeeper.create("/" + businessName,businessName.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        //创建瞬时有序节点
        znode = zooKeeper.create("/" + businessName + "/" + businessName + "_", businessName.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        znode = znode.substring(znode.lastIndexOf("/")+1);
        //获取业务节点下所有的子节点
        List<String> childrenNodes = zooKeeper.getChildren("/" + businessName, false);
        //子节点排序
        Collections.sort(childrenNodes);
        //获取序号最小的子节点
        String firstNode = childrenNodes.get(0);
        //如果创建的节点是第一个子节点，则获得锁
        if (!firstNode.equals(znode)){
            //监听前一个节点
            String lastNode = firstNode;
            for (String node:childrenNodes){
                if (!znode.equals(node)){
                    lastNode = node;
                }else {
                    zooKeeper.exists("/"+businessName+"/"+lastNode,true);
                    break;
                }
            }
            synchronized (this){
                wait();
            }
        }
        return true;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeDeleted){
            synchronized (this){
                notify();
            }
        }
    }


    @Override
    public void close() throws Exception {
        zooKeeper.delete("/"+businessName+"/"+znode,-1);
        zooKeeper.close();
        log.info("我释放了锁");
    }
}
