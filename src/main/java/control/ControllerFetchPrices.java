package control;

import com.pengrad.telegrambot.model.Update;

import model.Model;
import view.View;

public class ControllerFetchPrices implements ControllerFetch {
	
	private Model model;
	private View view;
	
	public ControllerFetchPrices(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void fetch(Update update) {
		view.sendTypingAction(update.callbackQuery().message().chat().id());
		model.scrapPrices(update);
		
	}

}
