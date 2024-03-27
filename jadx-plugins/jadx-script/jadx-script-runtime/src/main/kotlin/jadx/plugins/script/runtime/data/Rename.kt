package jadx.plugins.script.runtime.data

import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.IAttributeNode
import jadx.core.dex.nodes.IDexNode
import jadx.core.dex.nodes.RootNode
import jadx.plugins.script.runtime.JadxScriptInstance

class Rename(private val jadx: JadxScriptInstance) {

	fun all(makeNewName: (String) -> String?) {
		all { name, _ -> makeNewName.invoke(name) }
	}

	fun all(makeNewName: (String, IDexNode) -> String?) {
		jadx.addPass(object : ScriptOrderedPreparePass(
			jadx,
			"RenameAll",
			runBefore = listOf("RenameVisitor"),
		) {
			override fun init(root: RootNode) {
				for (pkgNode in root.packages) {
					rename(makeNewName, pkgNode, pkgNode.pkgInfo.name)
				}
				for (cls in root.classes) {
					rename(makeNewName, cls, cls.name)
					for (mth in cls.methods) {
						if (!mth.isConstructor) {
							rename(makeNewName, mth, mth.name)
						}
					}
					for (fld in cls.fields) {
						rename(makeNewName, fld, fld.name)
					}
				}
			}

			private inline fun <T : IDexNode> rename(
				makeNewName: (String, IDexNode) -> String?,
				node: T,
				name: String,
			) {
				if (node is IAttributeNode && node.contains(AFlag.DONT_RENAME)) {
					return
				}
				makeNewName.invoke(name, node)?.let {
					node.rename(it)
				}
			}
		})
	}
}
