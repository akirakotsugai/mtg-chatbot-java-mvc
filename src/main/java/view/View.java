package view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
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
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import model.Model;

public class View implements Observer{
	
	private static View uniqueInstance;
	
	//Chats have to be stored to avoid conflicts between their messages and the bot's responses.
	//each chat is identified by the chat id and has its own controller.
	private ObjectContainer chats;

	//private HashMap<Long, Chat> chats;
	Model model;
	private int queueIndex;
	//
	//Object that receives messages
	GetUpdatesResponse updatesResponse;
	//Object that sends responses
	SendResponse sendResponse;
	//Object that manages chat actions such as "typing action"
	BaseResponse baseResponse;
	
	TelegramBot bot = TelegramBotAdapter.build("497396638:AAFgKhV7J76-wh6vXlOTA0nkBl26MQxdvAU");
	
	private View(Model model) {
		this.model = model;
		chats = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "bd/chats.db4o");
		//chats = new HashMap<Long, Chat>();
		queueIndex = 0;
	}
	
	public static View getInstance(Model model) {
		if(uniqueInstance == null) {
			uniqueInstance = new View(model);
		}
		return uniqueInstance;
	}
	
	public void sendTypingAction(Long chatId) {
		SendChatAction typingAction = new SendChatAction(chatId, ChatAction.typing.name());

		baseResponse = bot.execute(typingAction);
	}
	
	public void receiveUsersMessages() {
		while(true) {
			//taking the Queue of Messages
			updatesResponse = bot.execute(new GetUpdates().limit(100).offset(queueIndex));
			
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
					
//				if(!chats.containsKey(chatId))
//					chats.put(chatId, new Chat(chatId));
				
				Chat proto = new Chat(chatId);
					ObjectSet result=chats.queryByExample(proto);
				
				if (result.isEmpty()) chats.store(proto);
	
				else {
					Chat chat = (Chat) result.get(0);
					chat.processInput(userInput, this);
				};
//				chats.get(chatId).processInput(userInput, this);
			}
		}	
	}
	

	@Override
	public void update(long chatId, Object data, String type) {
		
		if(type.equals("cardsFound")){
			LinkedHashMap<Integer,String> cards = (LinkedHashMap<Integer, String>) data;
			List<InlineKeyboardButton[]> buttons = new LinkedList<InlineKeyboardButton[]>();
			
			for(Map.Entry<Integer, String> entry : cards.entrySet()) {
				String buttonText = entry.getValue();
				Integer cardId = entry.getKey();
				buttons.add(new InlineKeyboardButton[]{
		                new InlineKeyboardButton(buttonText)
		                .callbackData("getCardInfo#"+cardId)});
			}
			
			InlineKeyboardButton[][] buttonsMatrix = buttons.toArray(new InlineKeyboardButton[0][]);			
			InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(buttonsMatrix);
			
			sendResponse = bot.execute(new SendMessage(chatId,
					"just pick one out")
					.parseMode(ParseMode.HTML).replyMarkup(inlineKeyboard));
			
			Chat chat = (Chat) chats.queryByExample(new Chat(chatId)).get(0);
			chat.setFetchActivated(false);
		}
		
		else if(type.contains("cardInfo")) {
			String[] callbackData = type.split("#");
			String cardName = callbackData[2];
			int cardId = Integer.parseInt(callbackData[1]);
			List<InlineKeyboardButton[]> otherOptions = new LinkedList<InlineKeyboardButton[]>();
			otherOptions.add(new InlineKeyboardButton[]{
	                new InlineKeyboardButton("Picture")
	                .callbackData("cardPic#"+cardId),
	                new InlineKeyboardButton("Rulings")
	                .callbackData("cardRulings#"+cardId),
	                new InlineKeyboardButton("Prices")
	                .callbackData("cardPrices#"+cardName)
			});
						
			InlineKeyboardButton[][] buttonsMatrix = otherOptions.toArray(new InlineKeyboardButton[0][]);
			InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(buttonsMatrix);

			
			sendResponse = bot.execute(new SendMessage(chatId,
					(String) data)
					.parseMode(ParseMode.HTML).replyMarkup(inlineKeyboard));
		}
		
		else if(type.equals("cardPic")){	
			sendResponse = bot.execute(new SendPhoto(chatId, (String) data));
		}
		
		else if(type.equals("cardRulings")) {
			sendResponse = bot.execute(new SendMessage(chatId,
					(String) data).parseMode(ParseMode.HTML));
		}
		
		else if(type.equals("cardPrices")) {
			System.out.println((String) data); 
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
			
			Chat chat = (Chat) chats.queryByExample(new Chat(chatId)).get(0);
			chat.setFetchActivated(false);
		}
		
		else if(type.equals("comingsoonFailure")) {
			sendResponse = bot.execute(new SendMessage(chatId, (String) data));
		}
		
		else if(type.equals("upcomingSetDetails")){	
			sendResponse = bot.execute(new SendMessage(chatId, (String) data));		
		}
		
		else if(type.equals("nocardfound")) {
			sendResponse = bot.execute(new SendMessage(chatId, (String) data));
			
			Chat chat = (Chat) chats.queryByExample(new Chat(chatId)).get(0);
			chat.setFetchActivated(false);
		}
	}
}