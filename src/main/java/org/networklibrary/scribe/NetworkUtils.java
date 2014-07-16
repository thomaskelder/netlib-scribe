package org.networklibrary.scribe;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NetworkUtils {

	static public Set<Node> getUniqueNeighbours(Node n){
		Set<Node> uniqueNeighbours = new HashSet<Node>();

		// that really correct? more testing needed
		for(Relationship rel : n.getRelationships()){
			if(!rel.getOtherNode(n).equals(n))
				uniqueNeighbours.add(rel.getOtherNode(n));
		}

		return uniqueNeighbours;
	}

	static public Set<Node> dfs(Set<Node> nodes, int maxDepth){
		Set<Node> result = new HashSet<Node>();

		for(Node n : nodes){
			doDFS(n,result,maxDepth,0);
		}
		
		return result;
	}

	static protected void doDFS(Node n, Set<Node> marked, int maxDepth, int depth){

		if(depth <= maxDepth){
			marked.add(n);
			for(Node neighbour : getUniqueNeighbours(n)){
				if(!marked.contains(neighbour)){
					doDFS(neighbour,marked,maxDepth,depth+1);
				}
			}
		}

	}

}
