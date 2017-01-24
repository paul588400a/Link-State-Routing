//by Po-Hsun Chen 1/24 2017
import java.util.HashMap;
import java.util.Queue;

public class LinkStatePacket {
    public int originateRouterId;
    public Integer sequence;
    public Integer timeToLive = 10;
    public HashMap<Integer, Queue<Integer>> paths = new HashMap<Integer, Queue<Integer>>();
    
    public LinkStatePacket(int originateRouterId, int sequence){
        this.originateRouterId = originateRouterId;
        this.sequence = sequence;
    }
}
