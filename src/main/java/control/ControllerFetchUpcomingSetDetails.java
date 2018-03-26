package control;

import com.pengrad.telegrambot.model.Update;

import model.Model;
import view.View;

public class ControllerFetchUpcomingSetDetails implements ControllerFetch {
	
	private Model model;
	private View view;
	
	public ControllerFetchUpcomingSetDetails(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void fetch(Update update) {
		view.sendTypingAction(update.callbackQuery().message().chat().id());
		model.scrapUpcomingSetDetails(update);		
	}

}
