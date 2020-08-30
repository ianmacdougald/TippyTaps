+ CodexTemplater { 
	tippyTaps_synthDef { | templateName("specs") | 
		this.makeTemplate(
			templateName, 
			Main.packages.asDict.at(\CodexTemplater)+/+"tippyTaps_synthDef.scd"
		)
	}
}
