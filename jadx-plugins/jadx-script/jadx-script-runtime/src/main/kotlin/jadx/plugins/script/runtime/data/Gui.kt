package jadx.plugins.script.runtime.data

import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
import jadx.plugins.script.runtime.JadxScriptInstance

class Gui(
	private val jadx: JadxScriptInstance,
	private val guiContext: JadxGuiContext?,
) {

	fun isAvailable() = guiContext != null

	fun ifAvailable(block: Gui.() -> Unit) {
		guiContext?.let { this.apply(block) }
	}

	fun ui(block: () -> Unit) {
		context().uiRun(block)
	}

	fun addMenuAction(name: String, action: () -> Unit) {
		context().addMenuAction(name, action)
	}

	fun addPopupMenuAction(
		name: String,
		enabled: (ICodeNodeRef) -> Boolean = { _ -> true },
		keyBinding: String? = null,
		action: (ICodeNodeRef) -> Unit,
	) {
		context().addPopupMenuAction(name, enabled, keyBinding, action)
	}

	fun registerGlobalKeyBinding(id: String, keyBinding: String, action: () -> Unit): Boolean {
		return context().registerGlobalKeyBinding(id, keyBinding, action)
	}

	fun copyToClipboard(str: String) {
		context().copyToClipboard(str)
	}

	fun open(ref: ICodeNodeRef): Boolean = context().open(ref)

	fun reloadActiveTab() = context().reloadActiveTab()

	fun reloadAllTabs() = context().reloadAllTabs()

	val nodeUnderCaret: ICodeNodeRef?
		get() = context().nodeUnderCaret
	val nodeUnderMouse: ICodeNodeRef?
		get() = context().nodeUnderMouse
	val enclosingNodeUnderCaret: ICodeNodeRef?
		get() = context().enclosingNodeUnderCaret
	val enclosingNodeUnderMouse: ICodeNodeRef?
		get() = context().enclosingNodeUnderMouse

	/**
	 * Save node rename in a project and run all needed UI updates
	 */
	fun applyNodeRename(node: ICodeNodeRef) = context().applyNodeRename(node)

	private fun context(): JadxGuiContext =
		guiContext ?: throw IllegalStateException("GUI plugins context not available!")
}
