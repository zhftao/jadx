package jadx.api.plugins.gui;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.KeyStroke;

import org.jetbrains.annotations.Nullable;

import jadx.api.metadata.ICodeNodeRef;

public interface JadxGuiContext {

	/**
	 * Run code in UI Thread
	 */
	void uiRun(Runnable runnable);

	/**
	 * Add global menu entry ('Plugins' section)
	 */
	void addMenuAction(String name, Runnable action);

	/**
	 * Add code viewer popup menu entry
	 *
	 * @param name       entry title
	 * @param enabled    check if entry should be enabled, called on popup creation
	 * @param keyBinding optional assigned keybinding {@link KeyStroke#getKeyStroke(String)}
	 */
	void addPopupMenuAction(String name,
			@Nullable Function<ICodeNodeRef, Boolean> enabled,
			@Nullable String keyBinding,
			Consumer<ICodeNodeRef> action);

	/**
	 * Attach new key binding to main window
	 *
	 * @param id         unique ID string
	 * @param keyBinding keybinding string {@link KeyStroke#getKeyStroke(String)}
	 * @param action     runnable action
	 * @return false if already registered
	 */
	boolean registerGlobalKeyBinding(String id, String keyBinding, Runnable action);

	void copyToClipboard(String str);

	/**
	 * Access to GUI settings
	 */
	JadxGuiSettings settings();

	ICodeNodeRef getNodeUnderCaret();

	ICodeNodeRef getNodeUnderMouse();

	ICodeNodeRef getEnclosingNodeUnderCaret();

	ICodeNodeRef getEnclosingNodeUnderMouse();

	/**
	 * Jump to a code ref
	 *
	 * @return if successfully jumped to the code ref
	 */
	boolean open(ICodeNodeRef ref);

	/**
	 * Reload code in active tab
	 */
	void reloadActiveTab();

	/**
	 * Reload code in all open tabs
	 */
	void reloadAllTabs();

	/**
	 * Save node rename in a project and run all needed UI updates
	 */
	void applyNodeRename(ICodeNodeRef node);
}
