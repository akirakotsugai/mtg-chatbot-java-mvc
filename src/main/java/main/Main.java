package main;

import model.Model;
import view.View;

public class Main {
	
	private static Model model;

	public static void main(String[] args) {
		model = Model.getInstance();
		View view = new View(model);
		model.registerObserver(view); //connection Model -> View
		view.receiveUsersMessages();

	}

}
