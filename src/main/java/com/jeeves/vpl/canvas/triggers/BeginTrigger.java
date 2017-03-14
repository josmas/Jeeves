package com.jeeves.vpl.canvas.triggers;

import javafx.scene.Node;

import com.jeeves.vpl.firebase.FirebaseTrigger;

public class BeginTrigger extends Trigger{ // NO_UCD (use default)

	public BeginTrigger(FirebaseTrigger data) {
		super(data);
		name.setValue("Begin Trigger");
		description = "Do stuff when the study begins again";
		addListeners();	}

	@Override
	public String getViewPath() {
		return String.format("/BeginTrigger.fxml", this.getClass().getSimpleName());

	}

	@Override
	public Node[] getWidgets() {
		return new Node[]{};
	}
	
	public BeginTrigger() {
		this(new FirebaseTrigger());
	}
}