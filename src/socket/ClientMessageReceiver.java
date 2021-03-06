package socket;

import logic.GameProcessor;
import messages.BaseMessage.Action;
import messages.PlayerStatusMessage;
import utils.PlayerCard;
import utils.PlayerManager;
import utils.RoomCard;
import utils.WeaponCard;



/**
 *
 * This class is responsible for receiving Messages from the Queue.
 * Messages are added to a Queue which is will then be processed on a separate thread.
 *
 * This class acts as an interface between the Client socket and main Game logic through the receipt of messages.
 * @author Trae
 *
 * @version 2.1
 *
 */
public class ClientMessageReceiver extends MessageReceiver
{
    private static volatile ClientMessageReceiver msgReceiver = null;

	private ClientMessageReceiver()
	{
        /**
         *  This checks to ensure no other instance is created using Reflection API
         */
        if (msgReceiver != null)
        {
        	System.out.println("Exception Handled: Attempt to create new Instance using Reflection API");
        }
        else
        {
        	if(initialize())
        	{
        		System.out.println("[INFO]: Client Message Receiver Initialized.");
        		process();
        	}
        }
	}

	/**
	 * Returns the Singleton instance of ClientMessageReceiver class
	 * @return ClientMessageReceiver
	 */
    public static ClientMessageReceiver getInstance()
    {
        if (msgReceiver == null)
        {
        	/**
        	 *  This is a thread-safe check to ensure another thread can't initialize another MessageReceiver class.
        	 */
            synchronized (ClientMessageReceiver.class)
            {
                if (msgReceiver == null)
                {
                	msgReceiver = new ClientMessageReceiver();
                }

            }
        }

        return msgReceiver;
    }


    /**
     * Threaded function that processes messages from Message Queue as they are received by client socket.
     */
	protected void process()
	{

    	Runnable listener = new Runnable()
    	{
            @Override
        	public void run()
        	{
            	while(running)
            	{
            		try
         		   	{
            			Thread.sleep(10);
         		   	}
         		   	catch (InterruptedException e)
         		   	{
         		   		// TODO Auto-generated catch block
         		   		e.printStackTrace();
         		   	}
            		if(!queue.isEmpty())
            		{

            			Object tmp = pop();
            			if(tmp != null)
            			{
            				processIncomingMessage(tmp);
            			}
            			else
            			{
            				System.out.println("Exception Handled: Received NULL Message");
            			}
            		}
            		else
            		{
        				//System.out.println("Queue Empty");
            		}

            	}

        	}
        };
        processThread = new Thread(listener);
        processThread.setDaemon(true);
        processThread.start();
	}

    /**
     * Sends message object to the server.
     * @param msg
     */
    public static void sendMessage(Object msg)
    {
    	Client.send(msg);
    }


    /**
     * TODO: This function routes incoming messages to for main processing.
     * TODO: Game logic function should be called here. Function that receives Message and processes it.
     * @param object
     */
    protected void processIncomingMessage(Object object)
    {
    	System.out.println("About to receive message");
		
    	if(object instanceof PlayerStatusMessage )
    	{
        	PlayerStatusMessage msg = (PlayerStatusMessage)object;

    		if(msg.getType() == Action.PLAYER_INIT)
    		{
    			PlayerManager.getPlayer().setPlayerId(msg.getPlayerId());
    			Object[] arr = new Object[3];
    			arr = msg.getVarField3();
    					
    			PlayerManager.getPlayer().setRoomCard((RoomCard)arr[0]);
    			PlayerManager.getPlayer().setPlayerCard((PlayerCard)arr[1]);
    			PlayerManager.getPlayer().setWeaponCard((WeaponCard)arr[2]);
    			
    			System.out.println("TXL: " + PlayerManager.getPlayer().getPlayerCard() + " " + PlayerManager.getPlayer().getWeaponCard() + " " + PlayerManager.getPlayer().getRoomCard());

    		}
    	}

    	GameProcessor.processMessage(object);
    }

}
