/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyproc;

/**
 *
 * @author Justin Swanson
 */
public enum Axis {
    X,
    Y,
    Z;

    public static Axis get(String s) {
	switch (s) {
	    case "X":
		return X;
	    case "Y":
		return Y;
	    default:
		return Z;
	}
    }
}
