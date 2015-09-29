package eu.ess.lt.parser.openxal;

import eu.ess.bled.Subsystem;
import javax.annotation.PostConstruct;
import java.util.*;

/**
 *  Compares all brothers in the tree according to leaf ordering
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class OnLeafComparator implements Comparator<Subsystem>{
	
	
	private Map<Integer, Integer> systemPos = new HashMap<Integer, Integer>();
	
	public OnLeafComparator(){
	}
		
	public void init(Collection<Subsystem> systems) {
		/*List<Subsystem> leafs = new ArrayList<Subsystem>(); 
		collectLeafs(leafs, subsystemDao.getById(systemID));*/
		System.out.println("Collecting all leafs");		
		
		System.out.println("Sorting all leafs");		
					
		// traverses all parents also
		System.out.println("Collecting parents info");
		int i = 0;
		for (Subsystem node : systems) {
			if (node.getPreviousSubsystem() == null) continue;
			//LOG.info("pos: "+i+"Leaf id:"+leaf.getId()+" parent:"+leaf.getParentSubsystem().getId()+" previous:"+leaf.getPreviousSubsystem().getId());						
			while (node != null && !systemPos.containsKey(node.getId())) {
				systemPos.put(node.getId(), i);
				node = node.getParentSubsystem();
			}		
			i++;
		}
		
		/*for (Entry<Integer,Integer> map : systemPos.entrySet()) {
			LOG.info("System id: "+map.getKey()+" child pos:"+ map.getValue());
		}*/		
	}
			
	@Override
	public int compare(Subsystem s1, Subsystem s2) {
		Integer s1pos = systemPos.get(s1.getId());
		Integer s2pos = systemPos.get(s2.getId());
		if (s1pos == null) s1pos = -1; // this code catches the first leaf also
		if (s2pos == null) s2pos = -1;
		return s1pos - s2pos;
	}		
	
	/**
	 * Sorts the subsystems into links according to the
	 * {@link Subsystem#getPreviousSubsystem()} field.
	 * 
	 * @param subsystems
	 *            {@link Collection}<code>&lt;</code>{@link Subsystem}
	 *            <code>&gt;</code> to be sorted.
	 * @return {@link List}<code>&lt;</code>{@link Subsystem}<code>&gt;</code>
	 *         of subsystems, sorted by {@link Subsystem#getPreviousSubsystem()}
	 *         .
	 */
	public List<Subsystem> sortSubsystems(Collection<Subsystem> subsystems) {
		List<Subsystem> sortedList = new ArrayList<Subsystem>();
		sortedList.addAll(subsystems);

		//HashSet<Subsystem> addedTracker = new HashSet<>();

		//int notFound = 0, addedSystems = 0;
		
		Collections.sort(sortedList, new Comparator<Subsystem>() {

	        public int compare(Subsystem o1, Subsystem o2) {
	        	if(o2.getPreviousSubsystem() == null && o1.getPreviousSubsystem() == null)
	        		return 0;
	        	else if(o2.getPreviousSubsystem() == null)
	        		return 1;
	        	else if(o1.getPreviousSubsystem() == null)
	        		return -1;
	        	else
	        		return o1.getPreviousSubsystem().compareTo(o2.getPreviousSubsystem());
	        }
	    });

		// @formatter:off
		// Go through all the subsystems.
		// 1) Subsystem is already in the sorted systems ==> do nothing
		// 2) Subsystem is not in the sorted systems ==>
		//      - Get the last element of the sorted systems (or null)
		//      - insert the current element at the end of the list
		//      - insert previous systems before this new element, until the 
		//        previous system of the inserted element equals the "last". 
		//        If "last" == null, this means insert all systems until the 
		//        first one.
		// @formatter:on
		/*for (Subsystem element : subsystems) {
			if ((sortedList.indexOf(element) < 0) && !addedTracker.contains(element)) {
				notFound++;
				Subsystem insertElement = element;
				int insertIndex = sortedList.size();
				Subsystem last = insertIndex > 0 ? sortedList.getLast() : null;
				do {
					addedSystems++;
					addedTracker.add(insertElement);
					sortedList.add(insertIndex, insertElement);
					insertElement = insertElement.getPreviousSubsystem();
				} while (insertElement != null && insertElement != last && !addedTracker.contains(insertElement));
			}
		}*/

		//LOG.finer("Not found: " + notFound + " ; All added: " + addedSystems);

		return sortedList;
	}
}
