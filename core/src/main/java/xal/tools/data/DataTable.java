/*
 * RecordSet.java
 *
 * Created on May 10, 2002, 2:19 PM
 */
package xal.tools.data;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;

/**
 * ****************************************************************************
 * DataTable is internal storage resembling a database table. An instance of
 * DataTable consists of an associated schema and a number of records. Each
 * record must be of the same class and have the same keys. Note that for
 * performance reasons this class is not thread safe. Users who need thread
 * safety must provide explicit synchronization on the table.
 *
 * @author tap
 */
public class DataTable {

    private static final Logger LOGGER = Logger.getLogger(DataTable.class.getName());

    /**
     * table label inside of a data adaptor
     */
    public static final String DATA_LABEL = "table";

    public static final String NODE_KEY = "nodeId";
    private static final Class<GenericRecord> DEFAULT_RECORD_CLASS = GenericRecord.class;

    /**
     * message center for dispatching data table notices to registered listeners
     */
    private final MessageCenter messageCenter;

    /**
     * proxy which forwards events to registered listeners
     */
    private final DataTableListener noticeProxy;

    /**
     * name of this table
     */
    private String name;

    /**
     * class of this table's records
     */
    private Class<? extends GenericRecord> recordClass;

    /**
     * table of hashed records
     */
    private KeyTable keyTable;

    /**
     * table schema of attributes
     */
    private Schema schema;

    /**
     * Constructor
     */
    public DataTable(final String aName, final Collection<DataAttribute> attributes) {
        this(aName, attributes, GenericRecord.class);
    }

    /**
     * Primary constructor
     */
    public DataTable(final String aName, final Collection<DataAttribute> attributes, final Class<? extends GenericRecord> aRecordClass) {
        messageCenter = new MessageCenter("Data Table");
        noticeProxy = messageCenter.registerSource(this, DataTableListener.class);

        name = aName;
        schema = new Schema(attributes);
        this.recordClass = aRecordClass;
        keyTable = new KeyTable();
    }

    /**
     * Constructor
     *
     * @param adaptor the adaptor from which to construct the data table
     */
    public DataTable(final DataAdaptor adaptor) {
        messageCenter = new MessageCenter("Data Table");
        noticeProxy = messageCenter.registerSource(this, DataTableListener.class);

        DataListener importer = dataHandler();
        importer.update(adaptor);
    }

    /**
     * Add the specified listener as a receiver of events from this data table.
     *
     * @param listener the listener to receive events
     */
    public void addDataTableListener(final DataTableListener listener) {
        MessageCenter defaultMessageCenter = MessageCenter.defaultCenter();
        defaultMessageCenter.registerTarget(listener, this, DataTableListener.class);
    }

    /**
     * Remove the specified listener from receiving events from this data table.
     *
     * @param listener the listener to remove from receiving events
     */
    public void removeDataTableListener(final DataTableListener listener) {
        MessageCenter defaultMessageCenter = MessageCenter.defaultCenter();
        defaultMessageCenter.removeTarget(listener, this, DataTableListener.class);
    }

    /**
     * convenience method for user to detect record class
     */
    public Class<? extends GenericRecord> getRecordClass() {
        return recordClass;
    }

    /**
     * Handle reading and writing from a data adaptor
     */
    @SuppressWarnings("unchecked")    // need to cast Class forName() call
    public DataListener dataHandler() throws MissingPrimaryKeyException {
        /* Anonymous class responsible for reading and writing an instance of DataTable with the data store */
        return new DataListener() {
            private static final String NAME_ATTRIBUTE = "name";
            private static final String RECORD_CLASS_ATTRIBUTE = "recordClass";

            /**
             * Get the data label
             */
            @Override
            public String dataLabel() {
                return DATA_LABEL;
            }

            /**
             * Update the table from the data adaptor
             */
            @SuppressWarnings("unchecked")
            @Override
            public void update(final DataAdaptor adaptor) {
                name = adaptor.stringValue(NAME_ATTRIBUTE);

                if (adaptor.hasAttribute(RECORD_CLASS_ATTRIBUTE)) {
                    String recordClassName = "";
                    try {
                        recordClassName = adaptor.stringValue(RECORD_CLASS_ATTRIBUTE);
                        recordClass = (Class<? extends GenericRecord>) Class.forName(recordClassName);
                    } catch (ClassNotFoundException exception) {
                        final String message = "Warning, the specified record class, \""
                                + recordClassName + "\" was not found, will substitute "
                                + DEFAULT_RECORD_CLASS.getName();
                        LOGGER.log(Level.WARNING, message, exception);
                        recordClass = DEFAULT_RECORD_CLASS;
                    }
                } else {
                    recordClass = DEFAULT_RECORD_CLASS;
                }

                // There can only be one schema
                final List<DataAdaptor> schemaList = adaptor.childAdaptors("schema");
                final DataAdaptor schemaAdaptor = schemaList.get(0);
                schema = new Schema();
                schema.update(schemaAdaptor);

                // Now that we have a schema, we can instantiate the keyTable
                keyTable = new KeyTable();

                // read generic records
                final List<DataAdaptor> recordAdaptors = adaptor.childAdaptors("record");
                for (final DataAdaptor recordAdaptor : recordAdaptors) {
                    try {
                        final Constructor<GenericRecord> constructor = (Constructor<GenericRecord>) recordClass.getConstructor(DataTable.class);

                        GenericRecord genericRecord = constructor.newInstance(new Object[]{DataTable.this});
                        genericRecord.update(recordAdaptor);
                        add(genericRecord);
                    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | AddRecordException | GenericRecord.ParseException exception) {
                        LOGGER.log(Level.SEVERE, "Error reading record.", exception);
                    }
                }
            }

            /**
             * Archive this instance to the data adaptor.
             */
            @Override
            public void write(final DataAdaptor adaptor) {
                adaptor.setValue("name", name);
                adaptor.setValue(RECORD_CLASS_ATTRIBUTE, recordClass.getName());

                adaptor.writeNode(schema);

                final Collection<GenericRecord> records = records();
                adaptor.writeNodes(records);
            }
        };
    }

    /**
     * Get the name of this table.
     */
    public String name() {
        return name;
    }

    /**
     * Get all keys for this table.
     */
    public Collection<String> keys() {
        return schema.keys();
    }

    /**
     * Get the primary keys for the table.
     */
    public Collection<String> primaryKeys() {
        return schema.primaryKeys;
    }

    /**
     * Determine if this table contains the specified record
     *
     * @param genericRecord The record to test for membership in this table
     * @return true if this table contains the record and false if not
     */
    public boolean hasRecord(final GenericRecord genericRecord) {
        return records().contains(genericRecord);
    }

    /**
     * Add the record to the table.
     */
    public void add(final GenericRecord genericRecord) throws AddRecordException {
        keyTable.add(genericRecord);
        noticeProxy.recordAdded(this, genericRecord);
    }

    /**
     * Remove the specified record from this table.
     */
    public void remove(final GenericRecord genericRecord) {
        keyTable.remove(genericRecord);
        noticeProxy.recordRemoved(this, genericRecord);
    }

    /**
     * Return the attributes of this table from the table's schema.
     */
    public Collection<DataAttribute> attributes() {
        return schema.attributes();
    }

    /**
     * Order the records according to the specified sort ordering.
     *
     * @param records the records to sort.
     * @param ordering the sort ordering used to sort the records
     * @return The records sorted by the sort ordering.
     */
    public List<GenericRecord> orderRecords(final Collection<GenericRecord> records, final SortOrdering ordering) {
        final List<GenericRecord> results = new ArrayList<>(records);
        Collections.sort(results, ordering);
        return results;
    }

    /**
     * Fetch all records held in the table
     */
    public Collection<GenericRecord> records() {
        return keyTable.records();
    }

    /**
     * Fetch all records held in the table and sort them according to the sort
     * ordering.
     *
     * @param ordering The sort ordering used to sort the records.
     * @return All of the records ordered according to the sort ordering.
     */
    public List<GenericRecord> getRecords(final SortOrdering ordering) {
        return orderRecords(keyTable.records(), ordering);
    }

    /**
     * Fetch the record with matching key/value pair bindings. The keys must be
     * one or more of the primary keys. If the record is not unique, an
     * exception will be thrown.
     */
    public <T extends Object> GenericRecord genericRecord(final Map<String, T> bindings) throws NonUniqueRecordException {
        return keyTable.genericRecord(bindings);
    }

    /**
     * Fetch the record with a matching key/value pair binding. The key must be
     * a primary key. If the record is not unique, an exception will be thrown.
     */
    public GenericRecord genericRecord(final String key, final Object value) throws NonUniqueRecordException {
        return keyTable.genericRecord(key, value);
    }

    /**
     * Fetch the records with matching key/value pair bindings. The keys must be
     * one or more of the primary keys.
     *
     * @param bindings The map of key/value pairs where the keys correspond to a
     * subset of primary keys and the values are the ones we want to match.
     * @return The matching records.
     */
    public <T extends Object> Collection<GenericRecord> records(final Map<String, T> bindings) {
        return keyTable.records(bindings);
    }

    /**
     * Fetch the records with matching key/value pair bindings and sort them
     * according to the sort ordering. The keys must be one or more of the
     * primary keys.
     *
     * @param bindings The map of key/value pairs where the keys correspond to a
     * subset of primary keys and the values are the ones we want to match.
     * @param ordering The sort ordering used to sort the records.
     * @return The matching records sorted according to the ordering.
     */
    public <T extends Object> List<GenericRecord> getRecords(final Map<String, T> bindings, final SortOrdering ordering) {
        return orderRecords(records(bindings), ordering);
    }

    /**
     * Fetch the records with a matching key/value pair binding. The key must be
     * a primary key.
     *
     * @param key A primary key to fetch against.
     * @param value The value of the primary key to match.
     * @return the matching records.
     */
    public Collection<GenericRecord> records(final String key, final Object value) {
        return keyTable.records(key, value);
    }

    /**
     * Fetch the records with a matching key/value pair binding and sort them
     * according to the sort ordering. The key must be a primary key.
     *
     * @param key A primary key to fetch against.
     * @param value The value of the primary key to match.
     * @param ordering The sort ordering used to sort the records.
     * @return The matching records sorted according to the ordering.
     */
    public List<GenericRecord> getRecords(final String key, final Object value, final SortOrdering ordering) {
        return orderRecords(records(key, value), ordering);
    }

    /**
     * Fetch the record with a matching nodeId. One of the primary keys must be
     * "nodeId". If the record is not unique, an exception will be thrown.
     */
    public GenericRecord recordForNode(final String nodeId) throws NonUniqueRecordException {
        return genericRecord(NODE_KEY, nodeId);
    }

    /**
     * Fetch the records with a matching nodeId. One of the primary keys must be
     * "nodeId".
     */
    public Collection<GenericRecord> recordsForNode(final String nodeId) {
        return records(NODE_KEY, nodeId);
    }

    /**
     * Get the unique values of the specified primary key column.
     *
     * @param key The primary key column whose unique values we want to fetch.
     * @return the unique values of the specified column.
     */
    public final Collection<Object> getUniquePrimaryKeyValues(final String key) {
        return keyTable.getUniquePrimaryKeyValues(key);
    }

    /**
     * Re-index the record based on new primary key values (if any).
     */
    final synchronized void reIndex(final GenericRecord genericRecord, final String key, final Object oldValue) {
        if (schema.isPrimaryKey(key) && this.hasRecord(genericRecord)) {
            keyTable.reIndex(genericRecord, key, oldValue);
        }
    }

    Schema getSchema() {
        return schema;
    }

    /**
     * ***********************************************************************
     * KeyTable holds a map (valueTable) whose keys are the primary keys and
     * whose values are ValueHash tables. Each ValueHash table corresponds to a
     * single primary key.
     */
    private final class KeyTable {

        /**
         * value hashes keyed by the primary key name
         */
        private final Map<String, ValueHash> valueTable;

        /**
         * Constructor
         */
        public KeyTable() {
            valueTable = new LinkedHashMap<>();

            for (final String key : schema.primaryKeys()) {
                final ValueHash valueHash = new ValueHash(key);
                valueTable.put(key, valueHash);
            }
        }

        /**
         * Return all records in the table
         *
         * @return all records in the table
         */
        public Collection<GenericRecord> records() {
            final Collection<ValueHash> valueHashes = valueTable.values();

            // each value hash has a copy of all of the records, so we only need one
            final Iterator<ValueHash> valueHashIter = valueHashes.iterator();

            // every value hash contains all records, so we only need one
            return valueHashes.isEmpty() ? Collections.<GenericRecord>emptySet() : valueHashIter.next().records();
        }

        /**
         * Get a record matching all of the primary key bindings. Bindings
         * should include all primary keys to ensure a unique record.
         */
        public <V extends Object> GenericRecord genericRecord(final Map<String, V> bindings) throws NonUniqueRecordException {
            final Collection<GenericRecord> records = records(bindings);

            if (records.size() > 1) {
                throw new NonUniqueRecordException(bindings);
            }

            final Iterator<GenericRecord> recordIter = records.iterator();
            return recordIter.hasNext() ? recordIter.next() : null;
        }

        /**
         * Get a record matching the specified primary key value. The key should
         * be the sole primary key to assure a unique record.
         */
        public GenericRecord genericRecord(final String key, final Object value) throws NonUniqueRecordException {
            final Map<String, Object> bindings = new HashMap<>(1);
            bindings.put(key, value);

            return genericRecord(bindings);
        }

        /**
         * Fetch all records matching the primary key bindings. You may use a
         * subset of primary keys since multiple records may be returned.
         */
        public <V extends Object> Collection<GenericRecord> records(final Map<String, V> bindings) {
            final Collection<GenericRecord> records = new HashSet<>();
            final Set<Map.Entry<String, V>> entries = bindings.entrySet();

            if (entries.isEmpty()) {
                return Collections.<GenericRecord>emptySet();
            }

            final Iterator<Map.Entry<String, V>> entryIter = entries.iterator();
            Map.Entry<String, V> entry = entryIter.next();
            Collection<GenericRecord> entryRecords = records(entry);
            records.addAll(entryRecords);
            while (entryIter.hasNext() && !records.isEmpty()) {
                entry = entryIter.next();
                entryRecords = records(entry);
                records.retainAll(entryRecords);
            }

            return records;
        }

        /**
         * Fetch the records matching the key/value pair specified in the entry.
         */
        private <V extends Object> Collection<GenericRecord> records(final Map.Entry<String, V> entry) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            return records(key, value);
        }

        /**
         * Get all of the records matching the specified primary key/value pair
         */
        public Collection<GenericRecord> records(final String key, final Object value) {
            return valueTable(key).records(value);
        }

        /**
         * Get the value table corresponding to the specified primary key
         */
        private ValueHash valueTable(final String key) {
            return valueTable.get(key);
        }

        /**
         * Get the unique values of the specified column.
         *
         * @param key The column whose unique values we want to fetch.
         * @return the unique values of the specified column.
         */
        protected final Collection<Object> getUniquePrimaryKeyValues(final String key) {
            return valueTable(key).getUniqueKeyValues();
        }

        /**
         * Get the primary key bindings associated with the specified record.
         */
        private Map<String, Object> primaryBindings(final GenericRecord genericRecord) {
            final Map<String, Object> bindings = new HashMap<>();
            for (final String key : schema.primaryKeys()) {
                final Object value = genericRecord.valueForKey(key);
                bindings.put(key, value);
            }

            return bindings;
        }

        /**
         * Determine whether there is an existing record with the same primary
         * bindings as the specified record.
         *
         * @param genericRecord the record whose primary key bindings we wish to
         * test
         * @return true if there is an existing record with the same primary key
         * bindings as the specified record and false otherwise
         */
        private boolean hasConflictingRecord(final GenericRecord genericRecord) {
            final Map<String, Object> bindings = primaryBindings(genericRecord);
            return genericRecord(bindings) != null;
        }

        /**
         * Re-index the hash table for a change in the specified record's value
         * for the specified key
         *
         * @param genericRecord the record whose primary key value has changed
         * @param key the primary key associated with the modified value
         * @param oldValue old value associated with the specified primary key
         */
        public final void reIndex(final GenericRecord genericRecord, final String key, final Object oldValue) {
            valueTable(key).reIndex(genericRecord, oldValue);
        }

        /**
         * Add the specified record to the hash table
         *
         * @param genericRecord the record to add
         */
        public void add(final GenericRecord genericRecord) throws AddRecordException {
            if (hasConflictingRecord(genericRecord)) {
                throw new AddRecordException(genericRecord);
            }

            for (final String key : schema.primaryKeys()) {
                final ValueHash valueHash = valueTable(key);
                valueHash.add(genericRecord);
            }
        }

        /**
         * Remove the specified record from the hash table
         *
         * @param genericRecord the record to remove
         */
        public void remove(final GenericRecord genericRecord) {
            for (final String key : schema.primaryKeys()) {
                final ValueHash valueHash = valueTable(key);
                valueHash.remove(genericRecord);
            }
        }
    }

    /**
     * ************************************************************************
     * ValueHash is a class used to index values associated with primary keys.
     * An instance is associated with a single primary key. The contained map
     * holds all values associated with the primary key. The value acts as the
     * key in the map and the set of all records sharing that value is the value
     * in the map. Each ValueHash contains exactly one reference to each record
     * in the table.
     */
    private static final class ValueHash {

        /**
         * The primary key for which to maintain a hash of values.
         */
        private final String primaryKey;

        /**
         * Map of record sets associated with a value for the primary key.
         */
        private final Map<Object, Set<GenericRecord>> recordSetTable;

        /**
         * Constructor
         *
         * @param primaryKey the primary key for which to hash values.
         */
        public ValueHash(final String primaryKey) {
            this.primaryKey = primaryKey;
            recordSetTable = new LinkedHashMap<>();
        }

        /**
         * Get the unique values of the primary key.
         *
         * @return The unique values of the primary key.
         */
        public final Collection<Object> getUniqueKeyValues() {
            return recordSetTable.keySet();
        }

        /**
         * Get all records in this value hash
         */
        public final Collection<GenericRecord> records() {
            final Collection<GenericRecord> unionRecordSet = new LinkedHashSet<>();
            final Collection<Set<GenericRecord>> recordSets = recordSetTable.values();

            for (final Set<GenericRecord> recordSet : recordSets) {
                unionRecordSet.addAll(recordSet);
            }

            return unionRecordSet;
        }

        /**
         * Get all records whose primary key matches the specified value
         */
        public final Set<GenericRecord> records(final Object value) {
            final Set<GenericRecord> records = recordSetTable.get(value);
            return records != null ? records : Collections.<GenericRecord>emptySet();
        }

        /**
         * add a record keyed by its primary key
         */
        public final void add(final GenericRecord genericRecord) {
            final Object value = genericRecord.valueForKey(primaryKey);
            Set<GenericRecord> recordSet = recordSetTable.get(value);

            if (recordSet == null) {
                recordSet = new LinkedHashSet<>();
                recordSetTable.put(value, recordSet);
            }
            recordSet.add(genericRecord);
        }

        /**
         * remove the specified record from the hash
         */
        public final void remove(final GenericRecord genericRecord) {
            Object value = genericRecord.valueForKey(primaryKey);
            Set<GenericRecord> recordSet = recordSetTable.get(value);

            if (recordSet == null) {
                return;
            }

            recordSet.remove(genericRecord);
        }

        /**
         * re-index this hash for the specified record replacing the record's
         * old value with the new one
         */
        public final void reIndex(final GenericRecord genericRecord, final Object oldValue) {
            records(oldValue).remove(genericRecord);
            add(genericRecord);
        }

        /**
         * Get a string representation of this ValueHash
         */
        @Override
        public String toString() {
            return primaryKey + ": " + recordSetTable.toString();
        }
    }

    /**
     * ************************************************************************
     * This exception is thrown when attempting to fetch a single record with
     * bindings and more than one record matches the criteria.
     */
    public static class NonUniqueRecordException extends RuntimeException {

        /**
         * serialization ID
         */
        private static final long serialVersionUID = 1L;

        private final transient Map<String, Object> bindings;

        /**
         * Constructor
         */
        // exception classes don't support generics so we have no choice but to cast
        @SuppressWarnings("unchecked")
        public <T> NonUniqueRecordException(final Map<String, T> theBindings) {
            bindings = (Map<String, Object>) theBindings;
        }

        /**
         * Get the exception message.
         */
        @Override
        public String getMessage() {
            return "Attempt to get a unique record for the bindings: " + bindings;
        }
    }

    /**
     * **********************************************************************
     * Exception thrown when attempting to add a record which causes an
     * inconsistency. Most likely this happens when a record with the same
     * primary key(s) already exist in the table.
     */
    public static class AddRecordException extends RuntimeException {

        /**
         * serialization ID
         */
        private static final long serialVersionUID = 1L;

        private final GenericRecord genericRecord;

        /**
         * Constructor
         */
        public AddRecordException(final GenericRecord aRecord) {
            genericRecord = aRecord;
        }

        /**
         * Get the exception message.
         */
        @Override
        public String getMessage() {
            return "Failed attempt to add the record: " + genericRecord;
        }
    }

    /**
     * This class represents the schema of the table which specifies the
     * attributes belonging to the table.
     */
    public class Schema implements DataListener {

        /**
         * map of attributes keyed by name
         */
        protected final Map<String, DataAttribute> attributeTable;

        /**
         * collection of primary keys
         */
        protected final Collection<String> primaryKeys;

        /**
         * Empty Constructor
         */
        public Schema() {
            this(Collections.<DataAttribute>emptySet());
        }

        /**
         * Primary constructor
         */
        public Schema(final Collection<DataAttribute> attributes) throws MissingPrimaryKeyException {
            attributeTable = new LinkedHashMap<>();
            primaryKeys = new LinkedHashSet<>();
            addAttributes(attributes);
        }

        /**
         * Get all of the keys for the schema.
         */
        public Set<String> keys() {
            return attributeTable.keySet();
        }

        /**
         * Validate that the primary keys are defined for this schema.
         */
        protected void validatePrimaryKeys() throws MissingPrimaryKeyException {
            if (primaryKeys.isEmpty()) {
                throw new MissingPrimaryKeyException(DataTable.this.name());
            }
        }

        /**
         * Get the collection of all attributes in this schema
         *
         * @return the collection of all attributes in this schema
         */
        public Collection<DataAttribute> attributes() {
            return attributeTable.values();
        }

        /**
         * Add to the schema each attribute in the specified collection
         *
         * @param attributes The collection of attributes to add to the schema
         */
        private void addAttributes(final Collection<DataAttribute> attributes) {
            for (final DataAttribute attribute : attributes) {
                addAttribute(attribute);
            }
        }

        /**
         * Add a new attribute to the schema
         *
         * @param attribute The attribute to add to the schema
         */
        public void addAttribute(final DataAttribute attribute) {
            final String attributeName = attribute.name();

            attributeTable.put(attributeName, attribute);

            if (attribute.isPrimaryKey()) {
                primaryKeys.add(attributeName);
            }
        }

        /**
         * Get the collection of primary keys
         *
         * @return The collection of primary keys
         */
        public Collection<String> primaryKeys() {
            return primaryKeys;
        }

        /**
         * Determine if the specified key is a primary key
         *
         * @param key The key to test for being a primary key
         * @return true if the key is a primary key and false if it isn't a
         * primary key
         */
        public boolean isPrimaryKey(final String key) {
            return primaryKeys.contains(key);
        }

        /**
         * Get the data label.
         */
        @Override
        public String dataLabel() {
            return "schema";
        }

        /**
         * Update the schema from the data adaptor.
         *
         * @param schemaAdaptor
         */
        @Override
        public void update(final DataAdaptor schemaAdaptor) throws MissingPrimaryKeyException {
            final List<DataAdaptor> attributeAdaptors = schemaAdaptor.childAdaptors("attribute");
            for (final DataAdaptor attributeAdaptor : attributeAdaptors) {
                final DataAttribute attribute = new DataAttribute(attributeAdaptor);
                addAttribute(attribute);
            }
            validatePrimaryKeys();
        }

        /**
         * Write the schema out to the data adaptor.
         */
        @Override
        public void write(final DataAdaptor schemaAdaptor) throws MissingPrimaryKeyException {
            validatePrimaryKeys();

            final Collection<DataAttribute> attributes = attributes();
            for (final DataAttribute attribute : attributes) {
                final DataListener attributeWriter = attribute.readerWriter();
                schemaAdaptor.writeNode(attributeWriter);
            }
        }
    }
}
