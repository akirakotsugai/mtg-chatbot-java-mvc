package model;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pengrad.telegrambot.model.Update;

import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.api.MTGAPI;
import io.magicthegathering.javasdk.resource.Card;
import io.magicthegathering.javasdk.resource.Ruling;
import view.Observer;

public class Model implements Subject {
	
	private List<Observer> observers;
	private static Model uniqueInstance;
	
	private Model(){
		observers = new LinkedList<Observer>();		
	}
	
	public static Model getInstance(){
		if(uniqueInstance == null){
			uniqueInstance = new Model();
		}
		return uniqueInstance;
	}

	@Override
	public void notifyObservers(long chatId, Object data, String type) {
		for(Observer observer : observers) {
			observer.update(chatId, data, type);
		}
		
	}

	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);
	}
	
	public void searchCard(Update update) {
		
		List<Card> cardsFound = CardAPI.getAllCards(
				new LinkedList<>(Arrays.asList("name=" + update.message().text())));
		
		if(!cardsFound.isEmpty()) {
			LinkedHashMap<Integer, String> reply = new LinkedHashMap<Integer, String>();
			
			for(Card card : cardsFound) {
				reply.put(card.getMultiverseid(), card.getName() + " - " + card.getSetName());
			}
			notifyObservers(update.message().chat().id(), reply, "cardsFound");			
		}
		
		else notifyObservers(
				update.message().chat().id(),
				"I am sorry "+update.message().chat().firstName() +
				". I haven't found any name-related card or even similar to "+update.message().text(),
				"nocardfound");
	
	}
	
	public void fetchCardInfo(Update update) {
		int cardId = Integer.parseInt(update.callbackQuery().data().split("#")[1]);
		String reply;
		Card card = CardAPI.getCard(cardId);		
		
		try {
			String name = "<b>"+card.getName()+"</b>";
			String type = card.getType();
			String cmc = Double.toString(card.getCmc());
			String[] coloursArray = card.getColors();
			String colours = "";
			String power;
			String toughness;
			String flavor;
			String text;
			
			if (coloursArray != null) {
				for (String colour : coloursArray) {
					colours = "";
					colours += colour + " ";
				}
			}
			else colours = "None";
			
			power = card.getPower() != null ? card.getPower() : "None";
			toughness = card.getToughness() != null ? card.getToughness() : "None";
			flavor = card.getFlavor() != null ? "<i>"+card.getFlavor()+"</i>" : "";
			text = card.getText() != null ? "<i>"+card.getText()+"</i>" : "";
			
			reply = name + "\n" + "Power: " + power + " Toughness: " + toughness + "\n"
					+ "Cmc: " + cmc + " Colours: " + colours + "\n" + "Type: " + type + "\n"
					+ text + "\n" + flavor + "\n\n Anything Else?" ;
		
		} catch (Exception e) {
			reply = "I'm so sorry " + update.callbackQuery().message().chat().firstName() 
					+ ". I wasn't able to bring the info you asked for.";
		}
		
		notifyObservers(update.callbackQuery().message().chat().id(), reply, "cardInfo"+"#"+cardId);

	}
	
	public void fetchCardPicture(Update update) {

		Card card = CardAPI.getCard(
				Integer.parseInt(update.callbackQuery().data().split("#")[1]));

		String reply = new String();
		
		String userName = update.callbackQuery().message().chat().firstName();
		reply = card.getImageUrl() != null ?
				card.getImageUrl() : "I'm sorry "+userName+". I haven't got its picture yet";
		
		notifyObservers(update.callbackQuery().message().chat().id(),
				reply, "cardPic");
	}
	
	public void fetchCardRulings(Update update) {
		Card card = CardAPI.getCard(
				Integer.parseInt(update.callbackQuery().data().split("#")[1]));
		
		Ruling[] rulings = card.getRulings();
		String reply = new String();
		
		if (rulings != null) {
			for (Ruling ruling : rulings) {
				reply += "<b>"+ruling.getDate()+"</b>\n";
				reply += ruling.getText()+"\n\n";
			}		
		}	
		else reply = "I'm sorry. I haven't got its rulings yet.";		
	
		notifyObservers(update.callbackQuery().message().chat().id(),
				reply, "cardRulings");
	}
	
	public void scrapPrices(Update update) {
		
		String card = update.message().text();
		card = card.replace(" ", "+");
		String url = "https://ligamagic.com.br/?view=cards%2Fsearch&card="+card;
		Document doc;
		String reply = new String();
		
		try {
			doc = Jsoup.connect(url).get();
			Elements tables = doc.getElementsByClass("tabela-card txt-centro");
			Element table = tables.first(); // there is only one table under this class
			Elements lines = table.getElementsByTag("tr");		
						
			for (Element line : lines) {
				if (line.text().contains("R$")) {
					Element img = line.getElementsByTag("img").first();
					String cardEdition = img.attr("title");
					if (cardEdition.contains("/")) {
						String [] splitEdition = cardEdition.split("/");
						cardEdition = splitEdition[0];
					}
					cardEdition = "<b>" + cardEdition + "</b>";
					
					String cheapest = line.getElementsByClass("menor-preco").first().text();
					String average = line.getElementsByClass("preco-medio").first().text();
					String mostExpensive = line.getElementsByClass("maior-preco").first().text();
					
					reply = reply +"\nLowest Price:"+cheapest+"\nAverage Price:"+
					average+"\nHighest Price:"+mostExpensive+"\n\n";
				}
			}
		
		} catch (IOException e) {
			reply = "I'm sorry"+update.message().chat().firstName()
					+"I wasn't able to connect to the prices' provider due to a connection problem.";
		}
		
		notifyObservers(update.message().chat().id(), reply, "cardprices");
	}
	
	public void scrapUpcomingSets(Update update) {
			
		Document doc;
		try {
			List<HashMap<String, String>> reply = 
					new LinkedList<HashMap<String, String>>();	
			
			doc = Jsoup.connect("https://magic.wizards.com/en/products/coming-soon").get();
			Elements block = doc.getElementsByClass("card-set-archive-table");
			Elements data = block.get(0).getElementsByTag("li");		
			
			for(Element li : data) {
				if(li.className() == ""){
					HashMap<String, String> item = new HashMap<String, String>();
					String nameSet = li.getElementsByClass("nameSet").get(0).text();
					String releaseDate = li.getElementsByClass("releaseDate").get(0).text();
					String link = li.getElementsByTag("a").get(0).attr("href");
					item.put("nameSet", nameSet);
					item.put("releaseDate", releaseDate);
					item.put("link", link);
					reply.add(item);
				}
			
			}
			notifyObservers(update.message().chat().id(), reply, "allUpcomingSets");			
		
		} catch (IOException e) {
			notifyObservers(update.message().chat().id(),
					"I'm afraid I couldn't get connected to our comingsoon's provider.", "comingsoonFailure");
			e.printStackTrace();
		}

	}
	
	public void scrapUpcomingSetDetails(Update update) {
		
		Document doc;
		String detailsLink = update.callbackQuery().data().split("#")[1];
		String reply = "";
		
		try {
			
			doc = Jsoup.connect("https://magic.wizards.com"+detailsLink).get();
			Element info = doc.getElementById("-tabs-1");
			
			if (info == null)
				reply = "You'd be better off taking a look at Magic:"
						+ " The Gathering website for further information.";
			
			else {
				Elements ps = info.getElementsByTag("p");
				for (Element p : ps) {
					reply += p.text() + "\n";
				}		
			}
			
			notifyObservers(update.callbackQuery().message().chat().id(), reply, "upcomingSetDetails");
			
			
		} catch (IOException e) {
			notifyObservers(update.callbackQuery().message().chat().id(), 
					"The details concerning the upcoming set you chose"
					+ " is unfortunately unavailable at the moment.", "upcomingSetDetails");
		}	
	}
	
}
