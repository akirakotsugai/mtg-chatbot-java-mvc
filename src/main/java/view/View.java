package view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import control.ControllerFetch;
import control.ControllerFetchCard;
import control.ControllerSearchCard;
import model.Model;

public class View implements Observer{

	private HashMap<Long, Chat> chats;
	Model model;
	private int queueIndex;
	
	//Object that receives messages
	GetUpdatesResponse updatesResponse;
	//Object that sends responses
	SendResponse sendResponse;
	//Object that manages chat actions such as "typing action"
	BaseResponse baseResponse;
	
	TelegramBot bot = TelegramBotAdapter.build("465527379:AAH__TKK53YBun2KMdC3Ja1wM8SO7tkYlUU");
	
	public View(Model model) {
		this.model = model;
		chats = new HashMap<Long, Chat>();
		queueIndex = 0;
	}
	

	
	public void sendTypingAction(Long chatId) {
		baseResponse = bot.execute(
				new SendChatAction(
						chatId, ChatAction.typing.name()
				)
		);	
	}
	
	public void receiveUsersMessages() {
		
		while(true) {
			
			//taking the Queue of Messages
			updatesResponse =  bot.execute(new GetUpdates().limit(100).offset(queueIndex));
			
			//Queue of messages
			List<Update> usersInputQueue = updatesResponse.updates();
			
			for (Update userInput : usersInputQueue) {
				
				queueIndex = userInput.updateId()+1;
				
				// Checking whether the chat has already been stored
				Long chatId;
				if (userInput.callbackQuery() != null)
					chatId = userInput.callbackQuery().message().chat().id();

				else
					chatId = userInput.message().chat().id();
					
				if(!chats.containsKey(chatId))		
					chats.put(chatId, new Chat(chatId));
				
				chats.get(chatId).processInput(userInput, this);			
	
			}
		}	
	}
	
	@Override
	public void update(long chatId, Object data, String type) {
		
		if(type.equals("cardsFound")){
			
		}
		
		else if(type.equals("cardPic")){
		
		}
		
		else if(type.equals("allUpcomingSets")) {
			
			LinkedList<HashMap<String, String>> sets = (LinkedList<HashMap<String, String>>) data;
			List<InlineKeyboardButton[]> buttons = new LinkedList<InlineKeyboardButton[]>();	

			for (HashMap<String, String> set : sets) {
				String buttonText = set.get("releaseDate") + " - " + set.get("nameSet");
				buttons.add(new InlineKeyboardButton[]{
		                new InlineKeyboardButton(buttonText)
		                .callbackData("upcomingSetDetails#"+set.get("link"))});
				
			}
			
			InlineKeyboardButton[][] buttonsMatrix = buttons.toArray(new InlineKeyboardButton[0][]);			
			InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(buttonsMatrix);
			
			sendResponse = bot.execute(new SendMessage(chatId, "Which upcoming set would you like to take a look at?")
					.parseMode(ParseMode.HTML).replyMarkup(inlineKeyboard));
			chats.get(chatId).setFetchActivated(false);
		}
		
		else if(type.equals("comingsoonFailure")) {
			sendResponse = bot.execute(new SendMessage(chatId, (String) data));
		}
		
		else if(type.equals("upcomingSetDetails")){	
			sendResponse = bot.execute(new SendMessage(chatId, (String) data));		
		}
		
		
		
	}

}
