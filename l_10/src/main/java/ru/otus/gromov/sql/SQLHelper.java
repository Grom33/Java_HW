package ru.otus.gromov.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.gromov.reflection.ReflectionHelper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SQLHelper {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ConnectionHelper connectionHelper;

	public SQLHelper(ConnectionHelper connectionHelper) {
		this.connectionHelper = connectionHelper;
	}

	public <T> T transactionalExecute(SqlTransaction<T> executor) {
		log.info("Begin execute transactional SQL operation.");
		try (Connection conn = connectionHelper.getConnection()) {
			try {
				conn.setAutoCommit(false);
				log.info("Open transaction.");
				T res = executor.execute(conn);
				conn.commit();
				log.info("Commit transaction.");
				return res;
			} catch (SQLException e) {
				log.info("Rollback transaction.");
				conn.rollback();
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String buildQuery(Object object) {
		String initQuery = getInitQuery(object);
		String valuesQuery = getValueQuery(object);
		StringBuilder resultQuery = new StringBuilder();
		resultQuery.append(initQuery);
		resultQuery.append("\n");
		resultQuery.append(valuesQuery);
		return resultQuery.toString();
	}

	private String getValueQuery(Object object) {
		List<Field> fields = ReflectionHelper.getFields(object);
		return String.format("INSERT INTO %s (%s) VALUES (%s);",
				object.getClass().getSimpleName(),
				fields.stream()
						.map(Field::getName)
						.collect(Collectors.joining(", ")),
				fields.stream()
						.map(field -> wrapString(ReflectionHelper.getFieldValueByField(object, field)))
						.collect(Collectors.joining(", "))

		);
	}

	private String getInitQuery(Object object) {
		List<Field> fields = ReflectionHelper.getFields(object);
		StringBuilder initQuery = new StringBuilder();
		initQuery.append(String.format("CREATE TABLE IF NOT EXISTS %s (", object.getClass().getSimpleName()));
		initQuery.append("id bigint generated by default as identity, ");
		fields.forEach((f) -> {
			if (!"id".equals(f.getName())) {
				initQuery.append(String.format(" %s %s,", f.getName(), getSqlType(f.getType())));
			}
		});
		initQuery.append(" primary key (id));");
		return initQuery.toString();
	}

	private String getSqlType(Class clazz) {
		if (int.class.isAssignableFrom(clazz)) return "int";
		if (long.class.isAssignableFrom(clazz)) return "bigint";
		if (float.class.isAssignableFrom(clazz)) return "float";
		if (double.class.isAssignableFrom(clazz)) return "float";
		if (String.class.isAssignableFrom(clazz)) return "varchar(255)";
		if (boolean.class.isAssignableFrom(clazz)) return "boolean";
		return "varchar(255)";
	}

	private String wrapString(Object object) {
		if (object instanceof String) return String.format("'%s'", object);
		return object.toString();
	}

}
