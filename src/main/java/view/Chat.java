package view;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import control.ControllerFetch;
import control.ControllerFetchCard;
import control.ControllerFetchUpcomingSetDetails;
import control.ControllerFetchUpcomingSets;
import control.ControllerSearchCard;

public class Chat {
	
	private long id;
	private ControllerFetch controllerFetch;
	private boolean fetchActivated;
	
	public Chat(long id) {
		this.id = id;
		fetchActivated = false;
	}
	
	public void callController(Update userInput) {
			controllerFetch.fetch(userInput);
	}
	
	public void processInput(Update userInput, View view) {
			
		// Checking whether it should call its
		//controller passing user's input
		if (fetchActivated) {
			callController(userInput);
		}
		
		else {
			//checking  whether it is a pressed button or a message
			if (userInput.callbackQuery() != null){
				
				if (userInput.callbackQuery().data().contains("getCardInfo")) {				
					controllerFetch = new ControllerFetchCard(view.model, view);
					callController(userInput);		
				}
				
				if (userInput.callbackQuery().data().contains("upcomingSetDetails")) {
					controllerFetch = new ControllerFetchUpcomingSetDetails(view.model, view);
					callController(userInput);
				}
			}
			
			else {			
				if(userInput.message().text().equals("/search_card")) {
					view.sendTypingAction(id);
					view.sendResponse = view.bot.execute(new SendMessage(this.id,
							"Tell me the card's name.\n If you are too lazy or aren't sure of its name, "
							+ "say at least part of it and I'll try to find all name-related cards"
							+ " so that you can pick one out."));
				
					controllerFetch = new ControllerSearchCard(view.model, view);
					fetchActivated = true;
				
				}
			
				else if(userInput.message().text().equals("/coming_soon")){	
					controllerFetch = new ControllerFetchUpcomingSets(view.model, view);
					callController(userInput);	
				
				}
			
				else if(userInput.message().text().equals("/picture"));
			
				else {
					view.sendTypingAction(id);
					view.sendResponse = view.bot.execute(new SendMessage(this.id,
							"What would you like to do now? Enter one of the following commands:\n"
							+ "/search_card to search for a card.\n"
							+ "/coming_soon to check the upcoming sets out." ));
				}
			}		
		}	
	}

	public boolean isFetchActivated() {
		return fetchActivated;
	}

	public void setFetchActivated(boolean fetchActivated) {
		this.fetchActivated = fetchActivated;
	}
	
}
