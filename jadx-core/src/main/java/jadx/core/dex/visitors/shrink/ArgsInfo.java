package jadx.core.dex.visitors.shrink;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.utils.EmptyBitSet;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;

final class ArgsInfo {
	private final InsnNode insn;
	private final List<ArgsInfo> argsList;
	private final List<RegisterArg> args;
	private final int pos;
	private int inlineBorder;
	private ArgsInfo inlinedInsn;
	private @Nullable List<ArgsInfo> wrappedInsns;

	public ArgsInfo(InsnNode insn, List<ArgsInfo> argsList, int pos) {
		this.insn = insn;
		this.argsList = argsList;
		this.pos = pos;
		this.inlineBorder = pos;
		this.args = getArgs(insn);
	}

	public static List<RegisterArg> getArgs(InsnNode insn) {
		List<RegisterArg> args = new ArrayList<>();
		addArgs(insn, args);
		return args;
	}

	private static void addArgs(InsnNode insn, List<RegisterArg> args) {
		if (insn.getType() == InsnType.TERNARY) {
			args.addAll(((TernaryInsn) insn).getCondition().getRegisterArgs());
		}
		for (InsnArg arg : insn.getArguments()) {
			if (arg.isRegister()) {
				args.add((RegisterArg) arg);
			}
		}
		for (InsnArg arg : insn.getArguments()) {
			if (arg.isInsnWrap()) {
				addArgs(((InsnWrapArg) arg).getWrapInsn(), args);
			}
		}
	}

	public InsnNode getInsn() {
		return insn;
	}

	List<RegisterArg> getArgs() {
		return args;
	}

	public BitSet getArgsSet() {
		if (args.isEmpty() && Utils.isEmpty(wrappedInsns)) {
			return EmptyBitSet.EMPTY;
		}
		BitSet set = new BitSet();
		fillArgsSet(set);
		return set;
	}

	private void fillArgsSet(BitSet set) {
		for (RegisterArg arg : args) {
			set.set(arg.getRegNum());
		}
		List<ArgsInfo> wrapList = wrappedInsns;
		if (wrapList != null) {
			for (ArgsInfo wrappedInsn : wrapList) {
				wrappedInsn.fillArgsSet(set);
			}
		}
	}

	public WrapInfo checkInline(int assignPos, RegisterArg arg) {
		if (assignPos >= inlineBorder || !canMove(assignPos, inlineBorder)) {
			return null;
		}
		inlineBorder = assignPos;
		return inline(assignPos, arg);
	}

	private boolean canMove(int from, int to) {
		ArgsInfo startInfo = argsList.get(from);
		int start = from + 1;
		if (start == to) {
			// previous instruction or on edge of inline border
			return true;
		}
		if (start > to) {
			throw new JadxRuntimeException("Invalid inline insn positions: " + start + " - " + to);
		}
		BitSet movedSet = startInfo.getArgsSet();
		if (movedSet == EmptyBitSet.EMPTY && startInfo.insn.isConstInsn()) {
			return true;
		}
		boolean canReorder = startInfo.canReorder();
		for (int i = start; i < to; i++) {
			ArgsInfo argsInfo = argsList.get(i);
			if (argsInfo.getInlinedInsn() == this) {
				continue;
			}
			InsnNode curInsn = argsInfo.insn;
			if (canReorder) {
				if (usedArgAssign(curInsn, movedSet)) {
					return false;
				}
			} else {
				if (!curInsn.canReorder() || usedArgAssign(curInsn, movedSet)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean canReorder() {
		if (!insn.canReorder()) {
			return false;
		}
		List<ArgsInfo> wrapList = wrappedInsns;
		if (wrapList != null) {
			for (ArgsInfo wrapInsn : wrapList) {
				if (!wrapInsn.canReorder()) {
					return false;
				}
			}
		}
		return true;
	}

	static boolean usedArgAssign(InsnNode insn, BitSet args) {
		if (args.isEmpty()) {
			return false;
		}
		RegisterArg result = insn.getResult();
		if (result == null) {
			return false;
		}
		return args.get(result.getRegNum());
	}

	WrapInfo inline(int assignInsnPos, RegisterArg arg) {
		ArgsInfo argsInfo = argsList.get(assignInsnPos);
		argsInfo.inlinedInsn = this;
		if (wrappedInsns == null) {
			wrappedInsns = new ArrayList<>(args.size());
		}
		wrappedInsns.add(argsInfo);
		return new WrapInfo(argsInfo.insn, arg);
	}

	ArgsInfo getInlinedInsn() {
		if (inlinedInsn != null) {
			ArgsInfo parent = inlinedInsn.getInlinedInsn();
			if (parent != null) {
				inlinedInsn = parent;
			}
		}
		return inlinedInsn;
	}

	@Override
	public String toString() {
		return "ArgsInfo: |" + inlineBorder
				+ " ->" + (inlinedInsn == null ? "-" : inlinedInsn.pos)
				+ ' ' + args + " : " + insn;
	}
}
