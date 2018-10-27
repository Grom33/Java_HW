package ru.otus.gromov.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import ru.otus.gromov.base.dataSets.UserDataSet;
import ru.otus.gromov.dao.UserDataSetDAO;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class DBServiceHibernateImpl implements DBService{
	private final SessionFactory sessionFactory;

	public DBServiceHibernateImpl() {
		Configuration configuration = new Configuration()
				.configure(new File("config/hibernate.cfg.xml"));
				//.addFile(new File("config/UserDataSet.hbm.xml"));

		sessionFactory = createSessionFactory(configuration);
	}

	private static SessionFactory createSessionFactory(Configuration configuration) {
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
		builder.applySettings(configuration.getProperties());
		ServiceRegistry serviceRegistry = builder.build();
		return configuration.buildSessionFactory(serviceRegistry);
	}

	public String getLocalStatus() {
		return runInSession(session -> {
			return session.getTransaction().getStatus().name();
		});
	}

	public void save(UserDataSet dataSet) {
		try (Session session = sessionFactory.openSession()) {
			UserDataSetDAO dao = new UserDataSetDAO(session);
			dao.save(dataSet);
		}
	}

	public UserDataSet read(long id) {
		return runInSession(session -> {
			UserDataSetDAO dao = new UserDataSetDAO(session);
			return dao.read(id);
		});
	}

	public UserDataSet readByName(String name) {
		return runInSession(session -> {
			UserDataSetDAO dao = new UserDataSetDAO(session);
			return dao.readByName(name);
		});
	}

	public List<UserDataSet> readAll() {
		return runInSession(session -> {
			UserDataSetDAO dao = new UserDataSetDAO(session);
			return dao.readAll();
		});
	}

	public void shutdown() {
		sessionFactory.close();
	}

	private <R> R runInSession(Function<Session, R> function) {
		try (Session session = sessionFactory.openSession()) {
			Transaction transaction = session.beginTransaction();
			R result = function.apply(session);
			transaction.commit();
			return result;
		}
	}
}
