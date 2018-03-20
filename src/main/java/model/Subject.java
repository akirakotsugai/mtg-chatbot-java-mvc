package model;

import view.Observer;

public interface Subject {
	
	public void notifyObservers(long chatId, Object data, String type);
	
	public void registerObserver(Observer observer);
	

}
