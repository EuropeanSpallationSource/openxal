package eu.ess.lt.parser.openxal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import eu.ess.bled.Subsystem;

/**
 * Compares all brothers in the tree according to leaf ordering
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class OnLeafComparator implements Comparator<Subsystem> {

	private Map<Integer, Integer> systemPos = new HashMap<Integer, Integer>();

	public OnLeafComparator() {
	}

	@PostConstruct
	public void init() {
		
		List<Subsystem> leafs = null; 

		leafs = sortSubsystems(leafs);

		// traverses all parents also
		for (int i = 0; i < leafs.size(); i++) {
			Subsystem node = leafs.get(i);
			while (node != null && !systemPos.containsKey(node.getId())) {
				systemPos.put(node.getId(), i);
				node = node.getParentSubsystem();
			}
		}

	}

	@Override
	public int compare(Subsystem s1, Subsystem s2) {
		Integer s1pos = systemPos.get(s1.getId());
		Integer s2pos = systemPos.get(s2.getId());
		if (s1pos == null)
			s1pos = -1; // this code catches the first leaf also
		if (s2pos == null)
			s2pos = -1;
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

		Collections.sort(sortedList, new Comparator<Subsystem>() {

			@Override
			public int compare(Subsystem o1, Subsystem o2) {
				if (o2.getPreviousSubsystem() == null && o1.getPreviousSubsystem() == null)
					return 0;
				else if (o2.getPreviousSubsystem() == null)
					return 1;
				else if (o1.getPreviousSubsystem() == null)
					return -1;
				else
					return o1.getPreviousSubsystem().compareTo(o2.getPreviousSubsystem());
			}
		});
		return sortedList;
	}
}
