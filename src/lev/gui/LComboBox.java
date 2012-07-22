/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lev.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.JComboBox;
import lev.gui.LHelpComponent.HelpFocusHandler;

/**
 *
 * @author Justin Swanson
 */
public class LComboBox<T extends Object> extends LUserSetting<Integer> {

    JComboBox<T> box;
    T previous;

    /**
     *
     * @param title_
     */
    public LComboBox(String title_) {
	super(title_);
	init();
    }

    protected LComboBox(String title_, Font font, Color shade) {
	super(title_, font, shade);
	init();
    }

    void init() {
	box = new JComboBox<>();
	add(box);
	box.setVisible(true);
	setVisible(true);
    }

    @Override
    public void setSize(int x, int y) {
	super.setSize(x, y);
	box.setSize(x, y);
    }

    /**
     *
     * @param a
     */
    public void addActionListener(ActionListener a) {
	box.addActionListener(a);
    }

    /**
     *
     * @return
     */
    public T getSelectedItem() {
	return (T) box.getSelectedItem();
    }

    /**
     *
     */
    public void removeAllItems() {
	box.removeAllItems();
    }

    /**
     *
     * @param o
     */
    public void addItem(T o) {
	box.addItem(o);
    }

    /**
     *
     * @param in
     */
    public void setSelectedIndex(int in) {
	if (box.getItemCount() <= in) {
	    box.setSelectedIndex(box.getItemCount() - 1);
	} else if (in < 0) {
	    box.setSelectedIndex(0);
	} else {
	    box.setSelectedIndex(in);
	}
    }

    /**
     *
     * @param o
     */
    public void switchTo(T o) {
	for (int i = 0; i < box.getItemCount(); i++) {
	    if (box.getItemAt(i).equals(o)) {
		setSelectedIndex(i);
	    }
	}
    }

    /**
     *
     */
    public void savePrevious() {
	if (box.getSelectedItem() != null) {
	    previous = getSelectedItem();
	} else {
	    previous = null;
	}
    }

    /**
     *
     */
    public void switchToPrevious() {
	if (previous != null) {
	    switchTo(previous);
	}
    }

    /**
     *
     * @param mouseListener
     */
    @Override
    public void addHelpHandler(boolean mouseListener) {
	addFocusListener(new HelpFocusHandler());
	if (mouseListener) {
	    addMouseListener(new HelpMouseHandler());
	}
    }

    @Override
    public void addFocusListener(FocusListener f) {
	super.addFocusListener(f);
	for (Component c : box.getComponents()) {
	    c.addFocusListener(f);
	}
    }

    @Override
    public void addMouseListener(MouseListener m) {
	super.addMouseListener(m);
	box.addMouseListener(m);
	for (Component c : box.getComponents()) {
	    c.addMouseListener(m);
	}
	if (titleLabel != null) {
	    titleLabel.addMouseListener(m);
	}
    }

    @Override
    protected void addUpdateHandlers() {
	box.addActionListener(new UpdateHandler());
    }

    public boolean isEmpty() {
	return box.getModel().getSize() == 0;
    }

    @Override
    public boolean revertTo(Map<Enum, Setting> m) {
	if (isTied()) {
	    int cur = box.getSelectedIndex();
	    box.setSelectedIndex(m.get(saveTie).getInt());
	    if (cur != box.getSelectedIndex()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public Integer getValue() {
	return box.getSelectedIndex();
    }

    @Override
    public void highlightChanged() {
	box.setBackground(new Color(224, 121, 147));
    }

    @Override
    public void clearHighlight() {
	box.setBackground(null);
    }
}
