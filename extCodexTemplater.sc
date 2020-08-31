+ CodexTemplater {
	tippyTaps_synthDef { | templateName("synthDef") |
		this.makeTemplate(
			templateName,
			Main.packages.asDict.at(\TippyTaps)+/+"tippyTaps_synthDef.scd"
		)
	}
}
