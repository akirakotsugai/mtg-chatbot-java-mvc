package control;

import com.pengrad.telegrambot.model.Update;

import model.Model;
import view.View;

public class ControllerSearchCard implements ControllerFetch{
	
	private Model model;
	private View view;
	
	public ControllerSearchCard(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void fetch(Update update) {
		view.sendTypingAction(update.message().chat().id());
		model.searchCard(update);
		
	}

}
