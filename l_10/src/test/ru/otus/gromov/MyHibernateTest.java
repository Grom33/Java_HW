package ru.otus.gromov;

import org.junit.Test;
import ru.otus.gromov.domain.UserDataSet;
import ru.otus.gromov.sql.ConnectionHelper;
import ru.otus.gromov.sql.SQLHelper;
import ru.otus.gromov.sql.SqlExecutor;

public class MyHibernateTest {
	@Test
	public void initDbTest(){
		//Executor executor = new MyHibernate();
		//
		//System.out.println(((MyHibernate) executor).buildQuery(new UserDataSet(1l, "dsafd", 12)));
		System.out.println(SQLHelper.buildQuery(new UserDataSet(1l, "dsafd", 12, true)));
	}

	@Test
	public void save(){
		MyHibernate myHibernate = new MyHibernate(ConnectionHelper.getConnection("sa", ""));
		//myHibernate.saveObject(new UserDataSet(1l, "dsafd", 12, true));

		myHibernate.save(new UserDataSet(1l, "dsafd", 12, true));
		myHibernate.loadObject(1, UserDataSet.class);
		//myHibernate.execute("SELECT * FROM USERDATASET");
	}
}