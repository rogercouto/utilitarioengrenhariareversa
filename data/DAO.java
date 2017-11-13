package model.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class DAO<T extends Entity> {

	protected String insertSql;
	protected String lastIdSql = "SELECT LAST_INSERT_ID()";
	protected String updateSql;
	protected String deleteSql;
	protected String selectSql;
	protected String getFilter;
	protected List<String> findFilters = new ArrayList<>();

	public DAO(){
		super();
		initialize();
	}

	public void insert(T t) throws ValidationException{
		try {
			Session session = SessionManager.createSession();
			beforeInsert(t);
			PreparedStatement ps = session.prepareStatement(insertSql);
			fillObject(ps, t);
			ps.executeUpdate();
			ps.close();
			int id = 0;
			if (lastIdSql != null){
				Statement s = session.createStatement();
				ResultSet result = s.executeQuery(lastIdSql);
				if (result.next())
					id = result.getInt(1);
				result.close();
			}
			afterInsert(id, t);
			session.commit();
			session.close();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public void update(T t) throws ValidationException{
		try {
			Session session = SessionManager.createSession();
			beforeUpdate(t);
			PreparedStatement ps = session.prepareStatement(updateSql);
			fillPrimaryKey(fillObject(ps, t), ps, t);
			ps.executeUpdate();
			ps.close();
			afterUpdate(t);
			session.commit();
			session.close();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public void delete(T t) throws ValidationException{
		try {
			Session session = SessionManager.createSession();
			beforeDelete(t);
			PreparedStatement ps = session.prepareStatement(deleteSql);
			fillPrimaryKey(1, ps, t);
			ps.executeUpdate();
			ps.close();
			session.commit();
			session.close();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public T get(Object key){
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(selectSql);
			builder.append(" WHERE ");
			builder.append(getFilter);
			Session session = SessionManager.createSession();
			PreparedStatement ps = session.prepareStatement(builder.toString());
			ps.setObject(1, key);
			ResultSet result = ps.executeQuery();
			T t = null;
			if (result.next())
				t = getEntity(result);
			result.close();
			ps.close();
			session.close();
			return t;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public DataList<T> getList(){
		try {
			Session session = SessionManager.createSession();
			Statement ps = session.createStatement();
			ResultSet result = ps.executeQuery(selectSql);
			DataList<T> list = new DataList<T>();
			while (result.next())
				list.add(getEntity(result));
			result.close();
			ps.close();
			session.close();
			list.setModifyed(false);
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public DataList<T> findList(String text){
		try {
			if (text == null || text.trim().length() == 0)
				return getList();
			StringBuilder builder = new StringBuilder();
			builder.append(selectSql);
			for (String filter : findFilters) {
				if (findFilters.indexOf(filter) == 0)
					builder.append(" WHERE ");
				else
					builder.append(" OR ");
				builder.append(filter);
			}
			Session session = SessionManager.createSession();
			PreparedStatement ps = session.prepareStatement(builder.toString());
			fillSearch(ps, text);
			ResultSet result = ps.executeQuery();
			DataList<T> list = new DataList<T>();
			while (result.next())
				list.add(getEntity(result));
			result.close();
			ps.close();
			session.close();
			list.setModifyed(false);
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public DataList<T> findList(String filter, Object[] params){
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(selectSql);
			if (!filter.contains("WHERE"))
				builder.append(" WHERE ");
			builder.append(filter);
			Session session = SessionManager.createSession();
			PreparedStatement ps = session.prepareStatement(builder.toString());
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i+1, params[i]);
			}
			ResultSet result = ps.executeQuery();
			DataList<T> list = new DataList<T>();
			while (result.next())
				list.add(getEntity(result));
			result.close();
			ps.close();
			session.close();
			list.setModifyed(false);
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
	
	public DataList<T> findList(String filter, Object param){
		char[] ca = filter.toCharArray();
		int count = 0;
		for (char c : ca) {
			if (c == '?')
				count++;
		}
		Object[] params = new Object[count];
		for (int i = 0; i < count; i++) {
			params[i] = param;
		}
		return findList(filter, params);
	}
	
	
	public String getInsertSql() {
		return insertSql;
	}

	public String getLastIdSql() {
		return lastIdSql;
	}

	public String getUpdateSql() {
		return updateSql;
	}

	public String getDeleteSql() {
		return deleteSql;
	}

	public String getSelectSql() {
		return selectSql;
	}

	public String getGetFilter() {
		return getFilter;
	}

	public List<String> getFindFilters() {
		return findFilters;
	}

	protected abstract void initialize();
	protected abstract int fillObject(PreparedStatement ps, T t) throws SQLException;
	protected abstract void fillPrimaryKey(int parameterIndex, PreparedStatement ps, T t) throws SQLException;
	protected abstract void beforeInsert(T t) throws ValidationException;
	protected abstract void afterInsert(int keyGen, T t) throws ValidationException, SQLException;
	protected abstract void beforeUpdate(T t) throws ValidationException;
	protected abstract void afterUpdate(T t) throws ValidationException, SQLException;
	protected abstract void beforeDelete(T t) throws ValidationException;
	protected abstract T getEntity(ResultSet result) throws SQLException;
	protected abstract void fillSearch(PreparedStatement ps, String text) throws SQLException;

}
