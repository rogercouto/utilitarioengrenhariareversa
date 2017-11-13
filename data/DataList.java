package model.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DataList<T extends Entity> implements List<T> {

	private ArrayList<T> list = new ArrayList<>();
	private boolean modifyed = false;

	public DataList(){
		super();
	}

	public boolean isModifyed(){
		return modifyed;
	}

	public void setModifyed(boolean modifyed){
		this.modifyed = modifyed;
	}

	@Override
	public boolean add(T element) {
		modifyed = true;
		return list.add(element);
	}

	@Override
	public void add(int index, T element) {
		modifyed = true;
		list.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		modifyed = true;
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		modifyed = true;
		return list.addAll(index, c);
	}

	@Override
	public void clear() {
		modifyed = true;
		list.clear();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return containsAll(c);
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		modifyed = true;
		return list.remove(o);
	}

	@Override
	public T remove(int index) {
		modifyed = true;
		return list.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		modifyed = true;
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public T set(int index, T t) {
		modifyed = true;
		return list.set(index, t);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for (T t : list) {
			if (builder.length() > 0)
				builder.append("; ");
			builder.append(t.toString());
		}
		return builder.toString();
	}

}
