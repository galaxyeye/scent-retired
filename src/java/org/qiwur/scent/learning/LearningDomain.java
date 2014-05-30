package org.qiwur.scent.learning;

public enum LearningDomain {
	EntityAttribute("EntityAttribute"),
	EntityCategory("EntityCategory"),
	BlockTitle("BlockTitle"),
	Website("Website");

	private String text;

	private LearningDomain(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}

	public static LearningDomain fromString(String text) {
		if (text != null) {
			for (LearningDomain b : LearningDomain.values()) {
				if (text.equalsIgnoreCase(b.text())) {
					return b;
				}
			}
		}

		return null;
	}
}
