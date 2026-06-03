package com.futureprograms.clients.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyIkeaCustomerService {

    private static final String SCHEMA = "myikea";
    private static final String TABLE = "customer";

    private final JdbcTemplate jdbcTemplate;
    private volatile Columns columns;

    public record PagedResult<T>(List<T> items, long totalItems, int currentPage, int pageSize) {
        public int totalPages() {
            if (pageSize <= 0) {
                return 0;
            }
            return (int) Math.ceil((double) totalItems / (double) pageSize);
        }

        public boolean hasNext() {
            return currentPage + 1 < totalPages();
        }

        public boolean hasPrevious() {
            return currentPage > 0;
        }
    }

    public PagedResult<Map<String, Object>> listCustomers(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        long total = jdbcTemplate.queryForObject("select count(*) from " + qualifiedTable(), Long.class);

        String sql = """
                select %s
                from %s
                order by %s
                limit ? offset ?
                """.formatted(selectList(), qualifiedTable(), orderByExpr());

        int offset = safePage * safeSize;
        List<Map<String, Object>> customers = jdbcTemplate.queryForList(sql, safeSize, offset);
        return new PagedResult<>(customers, total, safePage, safeSize);
    }

    public Optional<Map<String, Object>> getCustomerById(int customerId) {
        String sql = """
                select %s
                from %s
                where %s = ?
                """.formatted(selectList(), qualifiedTable(), idExpr());

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.getFirst());
    }

    public void deleteCustomerById(int customerId) {
        jdbcTemplate.update("delete from " + qualifiedTable() + " where " + idExpr() + " = ?", customerId);
    }

    public PagedResult<Map<String, Object>> searchByFirstName(String firstName, int page, int size) {
        Columns c = resolveColumns();
        if (c.firstNameColumn == null) {
            return new PagedResult<>(List.of(), 0, Math.max(0, page), Math.min(Math.max(1, size), 100));
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String term = "%" + normalizeLike(firstName) + "%";

        String countSql = "select count(*) from " + qualifiedTable() + " where lower(" + backtick(c.firstNameColumn) + ") like ?";
        long total = jdbcTemplate.queryForObject(countSql, Long.class, term.toLowerCase(Locale.ROOT));

        String sql = """
                select %s
                from %s
                where lower(%s) like ?
                order by %s
                limit ? offset ?
                """.formatted(selectList(), qualifiedTable(), backtick(c.firstNameColumn), orderByExpr());

        int offset = safePage * safeSize;
        List<Map<String, Object>> customers = jdbcTemplate.queryForList(sql, term.toLowerCase(Locale.ROOT), safeSize, offset);
        return new PagedResult<>(customers, total, safePage, safeSize);
    }

    public PagedResult<Map<String, Object>> searchByLastName(String lastName, int page, int size) {
        Columns c = resolveColumns();
        if (c.lastNameColumn == null) {
            return new PagedResult<>(List.of(), 0, Math.max(0, page), Math.min(Math.max(1, size), 100));
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String term = "%" + normalizeLike(lastName) + "%";

        String countSql = "select count(*) from " + qualifiedTable() + " where lower(" + backtick(c.lastNameColumn) + ") like ?";
        long total = jdbcTemplate.queryForObject(countSql, Long.class, term.toLowerCase(Locale.ROOT));

        String sql = """
                select %s
                from %s
                where lower(%s) like ?
                order by %s
                limit ? offset ?
                """.formatted(selectList(), qualifiedTable(), backtick(c.lastNameColumn), orderByExpr());

        int offset = safePage * safeSize;
        List<Map<String, Object>> customers = jdbcTemplate.queryForList(sql, term.toLowerCase(Locale.ROOT), safeSize, offset);
        return new PagedResult<>(customers, total, safePage, safeSize);
    }

    private String qualifiedTable() {
        return backtick(SCHEMA) + "." + backtick(TABLE);
    }

    private String selectList() {
        Columns c = resolveColumns();
        return String.join(", ",
                aliasedOrNull(c.idColumn, "customerId"),
                aliasedOrNull(c.firstNameColumn, "firstName"),
                aliasedOrNull(c.lastNameColumn, "lastName"),
                aliasedOrNull(c.emailColumn, "email"),
                aliasedOrNull(c.telefonoColumn, "telefono"),
                aliasedOrNull(c.fechaDeNacimientoColumn, "fechaDeNacimiento")
        );
    }

    private String orderByExpr() {
        Columns c = resolveColumns();
        if (c.idColumn == null) {
            return "1";
        }
        return backtick(c.idColumn);
    }

    private String idExpr() {
        Columns c = resolveColumns();
        if (c.idColumn == null) {
            throw new IllegalStateException("No se pudo resolver la columna ID de customer en myikea.customer");
        }
        return backtick(c.idColumn);
    }

    private static String aliasedOrNull(String columnName, String alias) {
        if (columnName == null) {
            return "null as " + backtick(alias);
        }
        return backtick(columnName) + " as " + backtick(alias);
    }

    private Columns resolveColumns() {
        Columns cached = this.columns;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            Columns secondCheck = this.columns;
            if (secondCheck != null) {
                return secondCheck;
            }

            Set<String> names = Set.copyOf(jdbcTemplate.queryForList(
                            "select column_name from information_schema.columns where table_schema = ? and table_name = ?",
                            String.class,
                            SCHEMA,
                            TABLE
                    ).stream()
                            .map(s -> s == null ? "" : s.toLowerCase(Locale.ROOT))
                            .toList()
            );

            Columns resolved = new Columns(
                    pick(names, "customerid", "customer_id", "id"),
                    pick(names, "firstname", "first_name", "firstName"),
                    pick(names, "lastname", "last_name", "lastName"),
                    pick(names, "email"),
                    pick(names, "telefono", "phone", "telephone"),
                    pick(names, "fechadenacimiento", "fecha_de_nacimiento", "birthdate", "birth_date", "birthday")
            );

            this.columns = resolved;
            return resolved;
        }
    }

    private static String pick(Set<String> availableLower, String... candidates) {
        for (String raw : candidates) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String lower = raw.toLowerCase(Locale.ROOT);
            if (availableLower.contains(lower)) {
                return lower;
            }
        }
        return null;
    }

    private static String backtick(String name) {
        return "`" + name.replace("`", "") + "`";
    }

    private static String normalizeLike(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }

    private record Columns(
            String idColumn,
            String firstNameColumn,
            String lastNameColumn,
            String emailColumn,
            String telefonoColumn,
            String fechaDeNacimientoColumn
    ) {}
}

