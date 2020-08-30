+ CodexTemplater { 
	tippySpecs { | templateName("specs") | 
		this.makeTemplate(
			templateName, 
			Main.packages.asDict.at(\CodexTemplater)+/+"tippySpecs.scd"
		)
	}
}
