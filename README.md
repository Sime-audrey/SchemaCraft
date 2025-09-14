# SchemaCraft (Java/Maven)

Lightweight, in‑memory relational toolkit that lets you **define schemas**, **create typed tables**, **insert tuples/rows**, and perform **lookups, projection, and natural joins**. Types are pluggable via a small `IType` interface; the project ships with `INT` and `VARCHAR(n)` implementations that support fixed‑width binary serialization using `ByteBuffer`.

## At a Glance
- **Project Name:** `SchemaCraft`
- **Modules:** single‑module Maven project (root)
- **Maven:** wrapper present (`mvnw`, `.mvn/wrapper`)
- **Coordinates:** `schemacraft:schemacraft:1.0-SNAPSHOT`
- **Java Toolchain:** 17+ (set via `maven.compiler.release` or compatible)
- **Testing:** JUnit Jupiter **5.11.0**
- **Entry Point:** `schemacraft.MainApplication` (demo); library API is under `schemacraft.*`
- **Key Package:** `schemacraft`

## Project Structure
```
SchemaCraft/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       ├── maven-wrapper.properties
│       └── MavenWrapperDownloader.java
├── src/
│   ├── main/
│   │   └── java/
│   │       └── schemacraft/
│   │           ├── Constants.java
│   │           ├── ITable.java
│   │           ├── IType.java
│   │           ├── MainApplication.java
│   │           ├── Schema.java
│   │           ├── Table.java
│   │           ├── Tuple.java
│   │           ├── TypeInt.java
│   │           └── TypeVarchar.java
│   └── test/
│       └── java/
│           └── schemacraft/
│               ├── TableTest.java
│               └── TupleTest.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Build & Test

### Maven (wrapper)
```bash
# from the project root (folder containing pom.xml)
./mvnw -q clean package
./mvnw -q test
```

This produces `target/schemacraft-1.0-SNAPSHOT.jar` (no external runtime deps).

## Features

- **Schemas & Types**
  - `Schema` defines an ordered set of columns (optionally a **key** column).
  - Built‑in types:
    - `TypeInt` = `INT`
    - `TypeVarchar(max)` = `VARCHAR(n)` with fixed maximum length and padded binary layout.
  - Add columns with helpers: `addIntType(name)`, `addVarCharType(name, max)`, and `addKey*` variants.
  - Utilities: `size()`, `getTupleSizeInBytes()`, `getColumnIndex(name)`, `getType(i)`, `getName(i)`.

- **Tables & Tuples**
  - `Table` holds a collection of `Tuple`s for a `Schema`.
  - Core ops: `insert(Tuple)`, `delete(key)`, `lookup(key)` (by key), `lookup(colName, value)` (by predicate), `iterator()`, `size()`.
  - `Tuple` gives field‑level accessors: `get(i)`, `get(String)`, `getInt(i)`, `getString(i)`, `set(i, value)`, `getKey()`.

- **Relational Operations**
  - `Schema.project(String... attrs)` → new projected `Schema`.
  - `Schema.naturaljoin(Schema other)` → combined `Schema` (no key) for natural join.
  - `Tuple.project(Schema projected)` and `Tuple.joinTuple(Schema joinSchema, Tuple t1, Tuple t2)` offer row‑wise transforms.
  - `Table.lookup(col, value)` returns a new `Table` with matching tuples.

- **Binary Serialization**
  - `IType.readValue(ByteBuffer)` / `writeValue(Object, ByteBuffer)` permit **fixed‑width** row serialization.
  - `Schema.serialize(ByteBuffer)` and `Tuple.serialize(ByteBuffer)` support compact storage.
  - `Constants.BLOCK_SIZE = 4096` suggests page/block alignment for future storage engines.

- **Developer Ergonomics**
  - `Constants.MAX_COLUMN_NAME_LENGTH = 24` and `Constants.DEBUG` flag.
  - Clean, minimal API — easy to extend with custom `IType` implementations.

## Code Map

### `schemacraft.Schema`
- **Add columns:** `addIntType`, `addVarCharType`, `addKeyIntType`, `addKeyVarCharType`
- **Inspect:** `getKey`, `getColumnIndex`, `getType`, `getName`, `getMaxSQLSize`, `size`, `getTupleSizeInBytes`
- **Algebra:** `project(String[])`, `naturaljoin(Schema)`
- **IO:** `serialize(ByteBuffer)`
- **toString():** human‑readable schema listing

### `schemacraft.Table`
- **Lifecycle:** `Table(Schema)`, `close()`
- **Mutation:** `insert(Tuple)`, `delete(key)`
- **Query:** `lookup(key)`, `lookup(colName, value)`, `iterator()`, `size()`
- **toString():** pretty prints rows

### `schemacraft.Tuple`
- **Construct:** `Tuple(Schema, Object... values)`; validates arity and types
- **Access:** `get(int)`, `get(String)`, `getInt(int)`, `getString(int)`, `set(int, Object)`, `getKey()`
- **Transform:** `project(Schema)`, `joinTuple(Schema, Tuple, Tuple)`
- **IO:** `serialize(ByteBuffer)`, `deserialize(Schema, ByteBuffer)`

### `schemacraft.IType` (SPI)
```java
String getColumnName();
int getMaxSizeBytes();
int getMaxSQLLength();
String getExternalName();
int getInternalType();
Object readValue(ByteBuffer buf);
void writeValue(Object value, ByteBuffer buf);
```

### `schemacraft.TypeInt` / `schemacraft.TypeVarchar`
- Fixed‑size `ByteBuffer` encoding; `VARCHAR` pads/truncates to `max` bytes.

### `schemacraft.Constants`
- `BLOCK_SIZE = 4096`, `MAX_COLUMN_NAME_LENGTH = 24`, `DEBUG`

### `schemacraft.MainApplication`
- Small demo showing schema creation, inserts, and `lookup` by column/key.

## Usage

Below is a minimal example using the public API:

```java
import schemacraft.*;

public class Demo {
    public static void main(String[] args) {
        // 1) Define a schema with a key column and two attributes
        Schema schema = new Schema();
        schema.addKeyIntType("ID");
        schema.addVarCharType("dept_name", 20);
        schema.addVarCharType("building", 16);

        // 2) Create a table and insert tuples
        Table table = new Table(schema);
        table.insert(new Tuple(schema, 19803, "Comp. Sci.", "SIG"));
        table.insert(new Tuple(schema, 19901, "Comp. Sci.", "GHC"));
        table.insert(new Tuple(schema, 20123, "Math",      "Wean"));

        // 3) Lookup by key and by non-key column
        Table row = table.lookup(19803);
        Table cs  = table.lookup("dept_name", "Comp. Sci.");

        // 4) Project columns
        Schema proj = schema.project(new String[] {"dept_name"});

        System.out.println(row);
        System.out.println(cs);
        System.out.println(proj);
    }
}
```

Compile & run your own demo (outside Maven build):
```bash
# compile (point to target jar if you also want to reuse build outputs)
javac -cp target/schemacraft-1.0-SNAPSHOT.jar Demo.java
java  Demo
```

## Testing

JUnit 5 tests (e.g., tuple construction & schema validation):
```bash
./mvnw -q test
```