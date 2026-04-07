# dao/jdbc

Implementaciones concretas de DAOs usando JDBC (MariaDB) y el pool HikariCP.

Convencion sugerida:
- `PacienteDaoJdbc`, `CitaDaoJdbc`, etc.
- Usar `DataSourceProvider.getRequired()` para obtener conexiones.

