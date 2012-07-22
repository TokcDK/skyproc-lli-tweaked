/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lev.gui;

import java.util.*;
import javax.swing.AbstractListModel;

/**
 *
 * @author Justin Swanson
 */
public class SortedListModel<T extends Object> extends AbstractListModel {

    // Define a SortedSet
    SortedSet<T> model;

    public SortedListModel() {
	// Create a TreeSet
	// Store it in SortedSet variable
	model = new TreeSet<>();
    }

    // ListModel methods
    @Override
    public int getSize() {
	// Return the model size
	return model.size();
    }

    @Override
    public T getElementAt(int index) {
	// Return the appropriate element
	return (T) model.toArray()[index];
    }

    // Other methods
    public void add(T element) {
	if (model.add(element)) {
	    fireContentsChanged(this, 0, getSize());
	}
    }

    public void addAll(T elements[]) {
	Collection c = Arrays.asList(elements);
	model.addAll(c);
	fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
	model.clear();
	fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(T element) {
	return model.contains(element);
    }

    public T firstElement() {
	// Return the appropriate element
	return model.first();
    }

    public Iterator iterator() {
	return model.iterator();
    }

    public T lastElement() {
	// Return the appropriate element
	return model.last();
    }

    public boolean removeElement(T element) {
	boolean removed = model.remove(element);
	if (removed) {
	    fireContentsChanged(this, 0, getSize());
	}
	return removed;
    }
}