package schemacraft;

public class MainApplication {
    public static void main(String[] args) {
        // Create a schema with ID as the primary key
        Schema schema = new Schema();
        schema.addKeyIntType("ID");
        schema.addVarCharType("name", 25);
        schema.addVarCharType("dept_name", 25);
        schema.addIntType("salary");

        // Create a table with the defined schema
        Table table = new Table(schema);

        // Insert tuples into the table
        table.insert(new Tuple(schema, 12121, "Kim", "Elect. Engr.", 65000));
        table.insert(new Tuple(schema, 19803, "Wisneski", "Comp. Sci.", 46000));
        table.insert(new Tuple(schema, 24734, "Bruns", "Comp. Sci.", 70000));
        table.insert(new Tuple(schema, 55552, "Scott", "Math", 80000));
        table.insert(new Tuple(schema, 12321, "Tao", "Comp. Sci.", 95000));

        // Print the table
        System.out.println("Initial table:");
        System.out.println(table);

        // Delete a tuple and print the table
        System.out.println("Delete 12121: " + table.delete(12121));
        System.out.println(table);

        // Attempt to delete the same tuple again
        System.out.println("Attempt to delete 12121 again: " + table.delete(12121));

        // Lookup by key
        System.out.println("Lookup 19803: " + table.lookup(19803));
        System.out.println("Lookup 12121: " + table.lookup(12121)); // Should return null

        // Lookup by non-key column with different scenarios
        ITable resultMultiple = table.lookup("dept_name", "Comp. Sci.");
        ITable resultSingle = table.lookup("ID", 19803);
        ITable resultNone = table.lookup("ID", 19802);

        System.out.println("eval dept_name=Comp. Sci.");
        System.out.println(resultMultiple);
        System.out.println("eval ID=19803");
        System.out.println(resultSingle);
        System.out.println("eval ID=19802");
        System.out.println(resultNone);
    }
}
