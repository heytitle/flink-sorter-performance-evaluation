package org.evaluation.utils;

/**
 * copied from package org.apache.flink.runtime.operators.testutils.types;
 */

public class IntPair {
	private int key;
	private int value;


	public IntPair()
	{}

	public IntPair(int key, int value)
	{
		this.key = key;
		this.value = value;
	}


	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "(" + this.key + "," + this.value + ")";
	}
}

