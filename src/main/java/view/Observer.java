package view;

public interface Observer {
	
	public void update(long chatId, Object data, String type);

}
