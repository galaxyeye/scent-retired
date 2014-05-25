package org.qiwur.scent.jsoup.block;

public enum BlockPattern {
	NONE,
	Table,
	N2,      	// N sibling tags and each tag has two non-empty child tags
	I_I,		// dense separators
	L,			// dense links
	LI,			// dense links and images
	SIGMA_L, 	// very dense links
	SIGMA_I, 	// very dense images
	SIGMA_LI  	// very dense link, image pairs
}
