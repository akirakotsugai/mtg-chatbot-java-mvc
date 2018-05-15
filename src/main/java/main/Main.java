package main;

import model.Model;
import view.View;

public class Main {
	
	private static Model model;

	public static void main(String[] args) {
		try {
			System.out.println("Running bot...");

			model = Model.getInstance();			
			View view = View.getInstance(model);
			model.registerObserver(view); //connection Model -> View
			view.receiveUsersMessages();
		} catch (Exception e) {
			System.out.println("Bot crashed: " + e.getMessage());
		} finally {
			System.out.println("Bot stopped.");
		}
	}

}
