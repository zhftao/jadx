import jadx.api.metadata.ICodeNodeRef
import jadx.core.dex.nodes.MethodNode

val jadx = getJadxInstance()
var savedBookmark: ICodeNodeRef? = null

jadx.gui.ifAvailable {
	addPopupMenuAction(
		"Set bookmark",
		enabled = { true },
		keyBinding = "B",
		action = ::setBookmark,
	)

	addMenuAction(
		"Jump to bookmark",
		action = ::jumpToBookmark,
	)
}

fun setBookmark(node: ICodeNodeRef) {
	val enclosing = jadx.gui.enclosingNodeUnderCaret ?: run {
		log.info { "No enclosing node" }
		return
	}
	// You can bookmark a field, method or a class
	val target = if (enclosing is MethodNode) enclosing else node

	log.info { "Setting bookmark to: $target" }
	savedBookmark = target
}

fun jumpToBookmark() {
	savedBookmark?.let {
		if (!jadx.gui.open(it)) {
			log.warn { "Failed to jump to bookmark: $it" }
		}
	} ?: run {
		log.info { "No bookmark" }
	}
}
