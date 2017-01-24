//by Po-Hsun Chen 1/24 2017
import java.util.*;
import java.util.Map.Entry;

public class Router {
	private boolean on = true;
    private int id;
    private String network;
    private int sequence = 0;
    private int tick = 1;
    public HashMap<Integer, Integer> Graph = new HashMap<>();
    public LinkedHashMap<Integer, Integer> record = new LinkedHashMap<>();
    public HashMap<Integer, Integer> tracking = new HashMap<>();
    public HashMap<Integer, RoutingList> routingTable = new HashMap<>();
    public List<Router> neighbors = new ArrayList<Router>();
    //public Integer Tick = 0;
    
    public Router(int id, String network) {
    	this.id = id;
    	this.network = network;
    }
    
    public void AddConnection(int neighborId, int cost){
    	Graph.put(neighborId, cost);
    }
    
    public void AddConnection(int neighborId){
    	Graph.put(neighborId, 1);
    }

    public void ReceivePacket(LinkStatePacket packet) {
        //decrement the LSP's TTL
        packet.timeToLive--;
        tracking.put(Integer.valueOf(packet.originateRouterId), tick);

        // connectivity has changed (a packet received from routers which not in routing table)
        if(tracking.size() > routingTable.size()){
			Main.Dijkstra(this);
			tracking = new HashMap<>();
        }

        //(1) the TTL has reached zero, or
        //(2) the receiving router has already seen an LSP from the same originating router with a sequence number higher than or equal to the sequence number in the received LSP
        if (packet.timeToLive == 0 || (record.containsKey(packet.originateRouterId) && record.get(packet.originateRouterId) >= packet.sequence)) {
            return;
        }

        // put the packet's originateRouterId and sequence into record
        record.put(packet.originateRouterId, packet.sequence);
        
        SendPacketOut(packet);
    }

    public void OriginatePacket() {
    	// give tracking an entry with value = tick-1 if the router id cannot be found in tracking's key
    	// so when this function run again, the router which has not sent LSP will be found
    	for(Integer id : routingTable.keySet()){
    		if(!tracking.containsKey(id))
    			tracking.put(id, tick-1);
    	}

    	// find out if the newest packet from all connected routers are received in 2 ticks
    	List<Integer> removeList = new ArrayList<>();
        for(Entry<Integer, Integer> entry  : tracking.entrySet()){
        	if(entry.getValue() + 1 < tick){
        		removeList.add(entry.getKey());
        		break;
        	}
        }

    	// if package has not received in 2 ticks, then run Dijkstra
        if(removeList.size() > 0){
        	Main.Dijkstra(this);
        	tracking.keySet().removeAll(removeList);
        }

        //increment a tick
        tick++;
        
        LinkStatePacket packet = new LinkStatePacket(id, sequence);
        //generate an LSP packet based on the current state of the network as it understands it, and call SendPacketOut();
        for(Entry<Integer, RoutingList> entry : routingTable.entrySet()){
        	packet.paths.put(entry.getKey(), new LinkedList<Integer>(entry.getValue().outgoingLink));
        }

        SendPacketOut(packet);
    }

    public void SendPacketOut(LinkStatePacket packet) {
    	// copy a receivers list from neighbors list
    	List<Router> receivers = new ArrayList<>(neighbors);
    	
    	for (Entry<Integer, Queue<Integer>> entry : packet.paths.entrySet()) {
    		if(entry.getValue().isEmpty())
    			continue;
    		
			if(entry.getValue().peek() == this.id){
				entry.getValue().remove();
				
				// to check any potential receivers that haven't been sent
				for(Router receiver : receivers){
					// sent package if receiver's id is recorded on package's paths
					if(receiver.isActive() && !entry.getValue().isEmpty() && receiver.getId() == entry.getValue().peek()){
						receiver.ReceivePacket(packet);
						receivers.remove(receiver);
						break;
					}
				}
			}
    	}
    }
    
    public void TurnOn(){
    	this.on = true;
    }
    
    public void TurnOff(){
    	this.on = false;
    }
    
    public boolean isActive(){
    	return on;
    }
    
    public int getId(){
    	return id;
    }
    
    public String getNetwork(){
    	return network;
    }
}
