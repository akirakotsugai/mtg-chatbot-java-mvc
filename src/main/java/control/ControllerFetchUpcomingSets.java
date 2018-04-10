package control;

import com.pengrad.telegrambot.model.Update;

import model.Model;
import view.View;

public class ControllerFetchUpcomingSets implements ControllerFetch{
	
	private Model model;
	private View view;
	
	public ControllerFetchUpcomingSets(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void fetch(Update update) {
		view.sendTypingAction(update.message().chat().id());
		model.scrapUpcomingSets(update);
	}

}
