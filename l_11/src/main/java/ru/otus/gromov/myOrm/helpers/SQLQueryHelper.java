package ru.otus.gromov.myOrm.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.gromov.base.dataSets.DataSet;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class SQLQueryHelper {
	private static final Logger log = LoggerFactory.getLogger(SQLQueryHelper.class);

	public static String getValueQuery(Object object, Collection<Field> fields) {
		String result = String.format("INSERT INTO %s (%s) VALUES (%s);",
				object.getClass().getSimpleName(),
				fields.stream()
						.map(Field::getName)
						.collect(Collectors.joining(", ")),
				fields.stream()
						.map(field -> wrapString(ReflectionHelper.getFieldValueByField(object, field)))
						.collect(Collectors.joining(", "))
		);
		log.info("getValueQuery({}): {}", object, result);
		return result;
	}

	public static String getInitQuery(Object object, Collection<Field> fields) {
		StringBuilder initQuery = new StringBuilder();
		initQuery.append(String.format("CREATE TABLE IF NOT EXISTS %s (", object.getClass().getSimpleName()));
		initQuery.append("id bigint generated by default as identity, ");
		fields.forEach((f) -> {
			if (!"id".equals(f.getName())) {
				initQuery.append(String.format(" %s %s,", f.getName(), getSqlType(f.getType())));
			}
		});
		initQuery.append(" primary key (id));");
		log.info("getInitQuery({}): {}", object, initQuery);
		return initQuery.toString();
	}

	private static String getSqlType(Class clazz) {
		if (int.class.isAssignableFrom(clazz)) return "int";
		if (long.class.isAssignableFrom(clazz)) return "bigint";
		if (float.class.isAssignableFrom(clazz)) return "float";
		if (double.class.isAssignableFrom(clazz)) return "float";
		if (String.class.isAssignableFrom(clazz)) return "varchar(255)";
		if (boolean.class.isAssignableFrom(clazz)) return "boolean";
		return "varchar(255)";
	}

	private static String wrapString(Object object) {
		if (object instanceof String) return String.format("'%s'", object);
		return object.toString();
	}

	public static String getInitJoinTable(Object first, Object second) {
		StringBuilder initQuery = new StringBuilder();
		String tableName = first.getClass().getSimpleName() + "_" + second.getClass().getSimpleName();
		initQuery.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));
		initQuery.append("id bigint generated by default as identity, ");
		initQuery.append(String.format(" %s %s,", first.getClass().getSimpleName() + "_id", "bigint"));
		initQuery.append(String.format(" %s %s,", second.getClass().getSimpleName() + "_id", "bigint"));
		initQuery.append(" primary key (id));");
		log.info("getInitJoinTable({}, {}): {}", first, second, initQuery);
		return initQuery.toString();
	}

	public static String getValuesJoinTable(Object first, Object second) {
		String tableName = first.getClass().getSimpleName() + "_" + second.getClass().getSimpleName();
		String initQuery = String.format("INSERT INTO %s (%s) VALUES (%s);",
				tableName,
				String.format(" %s,", first.getClass().getSimpleName() + "_id")
						+ String.format(" %s", second.getClass().getSimpleName() + "_id"),
				String.format(" %s,", ((DataSet) first).getId())
						+ String.format(" %s", ((DataSet) second).getId())
		);
		log.info("getValuesJoinTable({}, {}): {}", first, second, initQuery);
		return initQuery;
	}
}
