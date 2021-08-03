package xal.extension.tracewinimporter.openxalexporter;

import eu.ess.bled.Subsystem;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compares all brothers in the tree according to leaf ordering
 *
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class OnLeafComparator implements Comparator<Subsystem> {

    private static final Logger LOGGER = Logger.getLogger(OnLeafComparator.class.getName());

    private Map<Integer, Integer> systemPos = new HashMap<>();

    public OnLeafComparator() {
    }

    public void init(Collection<Subsystem> systems) {
        LOGGER.log(Level.INFO, "Collecting all leafs");

        LOGGER.log(Level.INFO, "Sorting all leafs");

        // traverses all parents also
        LOGGER.log(Level.INFO, "Collecting parents info");
        int i = 0;
        for (Subsystem node : systems) {
            if (node.getPreviousSubsystem() == null) {
                continue;
            }
            while (node != null && !systemPos.containsKey(node.getId())) {
                systemPos.put(node.getId(), i);
                node = node.getParentSubsystem();
            }
            i++;
        }
    }

    @Override
    public int compare(Subsystem s1, Subsystem s2) {
        Integer s1pos = systemPos.get(s1.getId());
        Integer s2pos = systemPos.get(s2.getId());
        if (s1pos == null) {
            // this code catches the first leaf also
            s1pos = -1;
        }
        if (s2pos == null) {
            s2pos = -1;
        }
        return s1pos - s2pos;
    }

    /**
     * Sorts the subsystems into links according to the
     * {@link Subsystem#getPreviousSubsystem()} field.
     *
     * @param subsystems {@link Collection}<code>&lt;</code>{@link Subsystem}
     * <code>&gt;</code> to be sorted.
     * @return {@link List}<code>&lt;</code>{@link Subsystem}<code>&gt;</code>
     * of subsystems, sorted by {@link Subsystem#getPreviousSubsystem()} .
     */
    public List<Subsystem> sortSubsystems(Collection<Subsystem> subsystems) {
        List<Subsystem> sortedList = new ArrayList<>();
        sortedList.addAll(subsystems);

        Collections.sort(sortedList, new Comparator<Subsystem>() {

            @Override
            public int compare(Subsystem o1, Subsystem o2) {
                if (o2.getPreviousSubsystem() == null && o1.getPreviousSubsystem() == null) {
                    return 0;
                } else if (o2.getPreviousSubsystem() == null) {
                    return 1;
                } else if (o1.getPreviousSubsystem() == null) {
                    return -1;
                } else {
                    return o1.getPreviousSubsystem().compareTo(o2.getPreviousSubsystem());
                }
            }
        });
        return sortedList;
    }
}
