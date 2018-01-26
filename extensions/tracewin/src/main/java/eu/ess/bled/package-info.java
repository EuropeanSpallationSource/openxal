/**
 * <code>eu.ess.bled</code> is the main package containing the java object model of BLED
 * (Best and Leanest Ever Database). The database is used to describe the complete accelerator
 * system, including the facilities (infrastructure), users, machine lattice and devices and 
 * complete configuration for the machine to operate. The database is configured to store 
 * all current settings as well as the history of all configurations in order to revert
 * the machine setting to a previous setting at any time.
 * <p>
 * The database is structured around the base class {@link Subsystem}, which represents
 * a single physical or logical component in the system. Other components are organized
 * in a tree structure around the root {@link Subsystem}. All nodes in the tree are
 * also {@link Subsystem}s. 
 * </p>
 * <p>
 * All classes that form BLED, form a relational model, which is annotated with hibernate
 * annotations and can be mapped to/from a database.
 * </p> 
 */
package eu.ess.bled;