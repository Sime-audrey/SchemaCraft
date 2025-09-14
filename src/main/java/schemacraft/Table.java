package schemacraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Table implements ITable {

	private List<Tuple> tuples;
	private Schema schema;

	public Table(Schema schema) {
		this.schema = schema;
		tuples = new ArrayList<>();
	}

	@Override
	public Schema getSchema() {
		return schema;
	}


	@Override
	public int size() {
		return tuples.size();
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public boolean insert(Tuple rec) {
		if (! rec.getSchema().equals(schema)) {
			throw new IllegalStateException("Error: tuple schema does not match table schema.");
		}

		// if schema has no key, then just add the tuple.
		// if schema has key, see if key already exists in table

		
		if (schema.getKey() != null) {
			// Schema has a primary key, check if the key already exists
			Object key = rec.get(schema.getKey());
			for (Tuple t : tuples) {
				if (t.get(schema.getKey()).equals(key)) {
					// Key already exists, do not insert the tuple
					return false;
				}
			}
		}
		// If no key or key does not exist, add the tuple
		tuples.add(rec);
		return true;
	}

	@Override
	public boolean delete(Object key) {
		if (schema.getKey() == null) {
			throw new IllegalStateException("Error: table does not have a primary key.  Can not delete.");
		}

		
		Iterator<Tuple> it = tuples.iterator();
		while (it.hasNext()) {
			Tuple t = it.next();
			if (t.get(schema.getKey()).equals(key)) {
				it.remove();
				return true;
			}
		}
		// Key not found, return false
		return false;
	}


	@Override
	public Tuple lookup(Object key) {
		if (schema.getKey() == null) {
			throw new IllegalStateException("Error: table does not have a primary key.  Can not lookup by key.");
		}

		
		for (Tuple t : tuples) {
			if (t.get(schema.getKey()).equals(key)) {
				return t;
			}
		}
		// Key not found, return null
		return null;
	}

	@Override
	public ITable lookup(String colname, Object value) {
		if (schema.getColumnIndex(colname) < 0) {
			throw new IllegalStateException("Error: table does not contain column "+colname);
		}
		Table result = new Table(this.getSchema());

		// find all tuples that satisfy the predicate colname=value
		// and insert the tuples to result table.
		// return the result

		
		for(Tuple t : tuples){
			if(t.get(colname).equals(value)) result.insert(t);
		}
		return result;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

	public String toString() {
		if (tuples.isEmpty()) {
			return "Empty Table";
		} else {
			StringBuilder sb = new StringBuilder();
			for (Tuple t : this) {
				sb.append(t.toString());
				sb.append("\n");
			}
			return sb.toString();
		}
	}
}
