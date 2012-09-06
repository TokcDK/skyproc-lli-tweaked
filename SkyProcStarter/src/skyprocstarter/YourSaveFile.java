/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyprocstarter;

import lev.gui.LSaveFile;

/**
 *
 * @author Justin Swanson
 */
public class YourSaveFile extends LSaveFile {

    @Override
    protected void initSettings() {
	//  The Setting,	    The default value,	    Whether or not it changing means a new patch should be made
	Add(Settings.IMPORT_AT_START,		true,	    false);
	Add(Settings.LANGUAGE,			0,	    true); // 0 is the default value because English is index 0 on the language enum.

    }

    @Override
    protected void initHelp() {

	helpInfo.put(Settings.IMPORT_AT_START,
		"If enabled, the program will begin importing your mods when the program starts.\n\n"
		+ "If turned off, the program will wait until it is necessary before importing.\n\n"
		+ "NOTE: This setting will not take effect until the next time the program is run.\n\n"
		+ "Benefits:\n"
		+ "- Faster patching when you close the program.\n"
		+ "- More information displayed in GUI, as it will have access to the records from your mods."
		+ "\n\n"
		+ "Downsides:\n"
		+ "- Having this on might make the GUI respond sluggishly while it processes in the "
		+ "background.");

	helpInfo.put(Settings.LANGUAGE,
		"You can set your language here.  This will make SkyProc import strings files of that language.\n\n"
		+ "NOTE:  You must restart the program for this to take effect.");

	helpInfo.put(Settings.OTHER_SETTINGS,
		"These are other settings related to this patcher program.");
    }

    // Note that some settings just have help info, and no actual values in
    // initSettings().
    public enum Settings {
	IMPORT_AT_START,
	LANGUAGE,
	OTHER_SETTINGS;
    }
}
