//by Po-Hsun Chen 1/24 2017
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class Main {
	static HashMap<Integer, Router> routerList = new HashMap<>();
    public static void main(String[] args){
    	routerList = readInputCreateRouters(); // initiate routers from infile.dat
    	// create neighbors for each router from existing link
    	for(Entry<Integer, Router> r : routerList.entrySet()){
    		for(Entry<Integer, Integer> g : r.getValue().Graph.entrySet()){
    			r.getValue().neighbors.add(routerList.get(g.getKey()));
    		}
    	}
    	for(Router r : routerList.values())
    		Dijkstra(r);
		
        // prompt the user for input in the command line
    	System.out.println("C: continue");
    	System.out.println("P n: print routing table which router id = n");
    	System.out.println("S n: shut down a router which id = n");
    	System.out.println("T n: start a router which id = n");
    	System.out.println("Q: quit");
    	System.out.print("Please input your next step:");
    	@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
    	String action = s.nextLine();
    	String[] items = action.split("\\s");
    	int id = -1;
    	while(!items[0].equals("Q")){
    		switch(items[0]){
	    		case "C":
	    			for(Router router : routerList.values()){
	    				router.OriginatePacket();
	    			}
	    			break;
	    		case "P": // print routing table
	    			if(!routerList.get(Integer.parseInt(items[1])).isActive()){
	    				System.out.println("Router " + items[1] + " is turned off.");
	    				break;
	    			}
	    			// print the header of the routing table
    				System.out.println("Network            Cost        Outgoing Link");
    				System.out.println("============================================");
	    			
    				for(RoutingList rli : routerList.get(Integer.parseInt(items[1])).routingTable.values()){
	    				System.out.printf("%-18s ", rli.network);	
	    				//print cost
	    				if(rli.cost == Integer.MAX_VALUE)
		    				System.out.print("Infinity      ");
	    				else
	    					System.out.printf("%-5d         ", rli.cost);
	    				// print outgoing link
	    				Queue<Integer> outgoingLink = new LinkedList<>(rli.outgoingLink);
	    				while(!outgoingLink.isEmpty()){
	    					System.out.printf(outgoingLink.poll() + " ");
	    				}
	    				System.out.println();
	    			}
	    			break;
	    		case "T": // start a router
	    			id = Integer.parseInt(items[1]);
	    			routerList.get(id).TurnOn();
	        		Dijkstra(routerList.get(id));
	    			break;
	    		case "S": // shut down a router
	    			id = Integer.parseInt(items[1]);
	    			routerList.get(id).TurnOff();
	    			routerList.get(id).routingTable = new HashMap<>(); // make routing table empty
	    			break;
    		}

        	System.out.print("Please input your next step:");
        	action = s.nextLine();
        	items = action.split("\\s");
    	}
    }
    
    public static void Dijkstra(Router router){
		if(!router.isActive())
			return;
		
		List<Router> V = new ArrayList<Router>();
		for(Router r : routerList.values()){
			if(r.isActive())
				V.add(r);
		}
		V.remove(router); // V = V-S
		
		//initiate D[] for distance, P[] for paths
		HashMap<Integer, Integer> D = new HashMap<Integer, Integer>(); // to store C[S, i]
		HashMap<Integer, Queue<Integer>> P = new HashMap<Integer, Queue<Integer>>(); // to store shortest Path[S, i]
		for(Router r : V){
			D.put(r.getId(), Integer.MAX_VALUE); // assign MAX_VALUE (infinity) to each C[S,V-S];
			P.put(r.getId(), new LinkedList<Integer>());
		}
		// Use the cost of each directed connection from S to replace infinity value
		for(Map.Entry<Integer, Integer> link : router.Graph.entrySet()){
			// if it is not active
			if(!D.containsKey(link.getKey()))
				continue;
			D.put(link.getKey(), link.getValue());
			Queue<Integer> tmpQ = P.get(link.getKey());
			tmpQ.add(router.getId());
			tmpQ.add(link.getKey());
			P.put(link.getKey(), tmpQ);
		}
		// keep finding the minimum path C[S, V-S] until V-S is empty
		while(V.size() > 0){
			Router cur = null;
			int curCost = 0; // base cost for calculating paths which pass through more than 1 edges
			for(Entry<Integer, Integer> d : D.entrySet()){
				boolean isMatch = false;
				for(Router r : V){ // to get the matched router and update its paths
					if(r.getId() == d.getKey() && d.getValue() < Integer.MAX_VALUE){
						cur = r;
						isMatch = true;
						break;
					}
				}
				if(isMatch) break;
			}
			if(cur != null)
				V.remove(cur);
			else break;
			curCost = D.get(cur.getId());
			
			for(Map.Entry<Integer, Integer> link : cur.Graph.entrySet()){
				//if the cur Graph has the C[s, cur] smaller than existing D[cur]
    			if(D.containsKey(link.getKey()) && D.get(link.getKey()) > curCost + link.getValue() && link.getKey() != router.getId()){
    				// if D[i] is updated, put the router i back to V
    				D.put(link.getKey(), curCost + link.getValue());
    				// update shortest path
    				Queue<Integer> path = new LinkedList<Integer>(P.get(cur.getId()));
    				path.add(link.getKey());
    				P.put(link.getKey(), path);
    				for(Entry<Integer, Router> tmpR : routerList.entrySet()){
    					if(tmpR.getKey() == link.getKey()){
        					V.remove(tmpR.getValue()); // to make sure it will not exist 2 same routers in V
    						V.add(tmpR.getValue());
    					}
    				}
    			}
    		}
		}
		// assign calculated routing table into the router
		router.routingTable = new HashMap<>();
		for (Map.Entry<Integer, Integer> d : D.entrySet()){
			RoutingList rl = new RoutingList();
			rl.network = routerList.get(d.getKey()).getNetwork();
			rl.cost = d.getValue();
			rl.outgoingLink = new LinkedList<Integer>(P.get(d.getKey()));
			router.routingTable.put(d.getKey(), rl);
		}
    }

    public static HashMap<Integer, Router> readInputCreateRouters() {
    	HashMap<Integer, Router> routers = new HashMap<>();
        Path path = Paths.get(System.getProperty("user.dir")+"\\infile.dat");
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new HashMap<>();
        }
        Router router = null;
        for (String line : lines) {
            String[] items = line.split("\\s"); // \s=space
            if (!line.startsWith(" ")) {
                int id = Integer.valueOf(items[0]);
                String network = items[1];
                router = new Router(id, network);
                routers.put(id, router);
            }
            else { //connectivity graph
                router.Graph.put(Integer.valueOf(items[1]), items.length < 3 ? 1 : Integer.valueOf(items[2]));
            }
        }
        return routers;
    }
}
