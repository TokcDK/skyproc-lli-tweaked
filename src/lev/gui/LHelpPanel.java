/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lev.gui;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import lev.gui.resources.LImages;

/**
 * A panel that displays help information.
 * @author Justin Swanson
 */
public class LHelpPanel extends LPanel {

    LLabel setting;
    LTextPane help;
    Image arrow;
    int arrowx;
    int y;
    LPanel bottomArea;
    Boolean textVisible = false;
    Boolean hideArrow = false;
    static int spacing = 75;

    /**
     *
     * @param bounds
     * @param titleFont
     * @param titleC
     * @param contentC
     * @param leftArrow
     * @param arrowX
     */
    public LHelpPanel(Rectangle bounds, Font titleFont, Color titleC, Color contentC, boolean leftArrow, int arrowX) {
	y = - 100;
	arrowx = arrowX;
	arrow = (new ImageIcon(LImages.arrow(leftArrow))).getImage();

	setting = new LLabel("  Setting Name", titleFont, titleC);
	setBounds(bounds);

	help = new LTextPane(new Dimension(getWidth() - 35, getHeight()), contentC);
	help.setVisible(true);
	help.addScroll();
	help.setEditable(false);
	add(setting);
	add(help);
	setting.setVisible(textVisible);
	help.setVisible(textVisible);

	bottomArea = new LPanel();
	bottomArea.setSize((int) bounds.getWidth(), 190);
	bottomArea.setLocation(0, (int) bounds.getHeight() - bottomArea.getHeight());
	bottomArea.setVisible(false);
	add(bottomArea);

	setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
	if (textVisible && !hideArrow) {
	    g.drawImage(arrow, arrowx, y - arrow.getHeight(null) / 2, null);
	}
    }

    /**
     *
     * @param c
     */
    public void setHeaderColor (Color c) {
	setting.setForeground(c);
    }

    public void setTitle(String title_) {
	setting.setText("   " + title_);
	setting.setSize(setting.getPreferredSize());
    }

    public void setTitleHeight(int y_) {
	if (y_ == -1) {
	    y = spacing;
	} else {
	    y = y_;
	}
	setting.setLocation(17, y - setting.getHeight() / 2);
	help.setLocation(35, y + setting.getHeight() / 2);
	evalPositioning();

    }

    /**
     *
     * @param text
     */
    public void setContent(String text) {
	hideArrow = false;
	help.setText(text);
	evalPositioning();
    }

    private void evalPositioning() {
	// Divide by two as setting + help are shifted up that much in positioning
	int min = getLimit() - spacing - setting.getHeight() / 2;
	if (min > help.getPreferredSize().height) {
	    min = help.getPreferredSize().height;
	}

	help.setSize(help.getWidth(), min);
	int helpReach = setting.getY() + setting.getHeight() + help.getHeight();
	if (helpReach > getLimit()) {
	    int move = helpReach - getLimit();
	    help.setLocation(help.getX(), help.getY() - move);
	    setting.setLocation(setting.getX(), setting.getY() - move);
	}
    }

    private int getLimit() {
	if (bottomArea != null && bottomArea.isVisible()) {
	    return getHeight() - bottomArea.getHeight();
	} else {
	    return getHeight();
	}
    }

    /**
     * Appends text to the content string.
     * @param text
     */
    public void addContent(String text) {
	help.append(text);
	evalPositioning();
    }

    /**
     * Clears the bottom panel of the help area.
     */
    public void clearBottomArea() {
	bottomArea.removeAll();
	bottomArea.setVisible(false);
    }

    /**
     * Adds a component to the separate bottom area panel.
     * @param c
     */
    public void addToBottomArea(Component c) {
	bottomArea.Add(c);
	bottomArea.setVisible(true);
    }

    /**
     *
     * @param y
     */
    public void setBottomAreaHeight(int y) {
	int limit = bottomArea.getY() + bottomArea.getHeight();
	bottomArea.setSize(bottomArea.getWidth(), y);
	bottomArea.setLocation(0, limit - bottomArea.getHeight());
    }

    /**
     *
     * @param on
     */
    public void setBottomAreaVisible(boolean on) {
	bottomArea.setVisible(on);
    }

    /**
     *
     * @param b
     */
    public void textVisible(Boolean b) {
	textVisible = b;
	setting.setVisible(b);
	help.setVisible(b);
	repaint();
    }

    /**
     *
     */
    public void hideArrow() {
	hideArrow = true;
    }

    /**
     *
     * @return
     */
    public Dimension getBottomSize() {
	return bottomArea.getSize();
    }
}
