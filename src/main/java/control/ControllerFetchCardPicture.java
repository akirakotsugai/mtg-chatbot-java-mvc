package control;

import com.pengrad.telegrambot.model.Update;

import model.Model;
import view.View;

public class ControllerFetchCardPicture implements ControllerFetch {
	
	private Model model;
	private View view;
	
	public ControllerFetchCardPicture(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void fetch(Update update) {
		model.fetchCardPicture(update);
		
	}

}
