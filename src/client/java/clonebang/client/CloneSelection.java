package clonebang.client;

import net.minecraft.core.BlockPos;

public class CloneSelection {
	private BlockPos first;
	private BlockPos second;

	public void setFirst(BlockPos first) {
		this.first = first.immutable();
	}

	public void setSecond(BlockPos second) {
		this.second = second.immutable();
	}

	public BlockPos first() {
		return first;
	}

	public BlockPos second() {
		return second;
	}

	public boolean hasFirst() {
		return first != null;
	}

	public boolean isComplete() {
		return first != null && second != null;
	}

	public void clear() {
		first = null;
		second = null;
	}

	public BlockPos min() {
		return new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
	}

	public BlockPos max() {
		return new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
	}

	public int width() {
		return max().getX() - min().getX() + 1;
	}

	public int height() {
		return max().getY() - min().getY() + 1;
	}

	public int depth() {
		return max().getZ() - min().getZ() + 1;
	}

	public String cloneCommand(BlockPos target) {
		BlockPos min = min();
		BlockPos max = max();
		return "clone " + min.getX() + " " + min.getY() + " " + min.getZ() + " "
				+ max.getX() + " " + max.getY() + " " + max.getZ() + " "
				+ target.getX() + " " + target.getY() + " " + target.getZ();
	}
}
