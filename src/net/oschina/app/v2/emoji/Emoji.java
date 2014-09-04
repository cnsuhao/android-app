package net.oschina.app.v2.emoji;

public class Emoji {
	private int resId;
	private String value;

	public Emoji() {
	}

	public Emoji(int resId, String value) {
		this.resId = resId;
		this.value = value;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
