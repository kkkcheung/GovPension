package uk.gov.dwp.uc.pairtest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public class TicketServiceConfig {
	
	public static final String PROP_INFANT_PRICE = "INFANT_PRICE";
	public static final String PROP_CHILD_PRICE = "CHILD_PRICE";
	public static final String PROP_ADULT_PRICE = "ADULT_PRICE";
	
	public static final int DEFAULT_INFANT_TICKET_PRICE = 0;
	public static final int DEFAULT_CHILD_TICKET_PRICE = 15;
	public static final int DEFAULT_ADULT_TICKET_PRICE = 25;
	public static final int DEFAULT_MAX_TICKETS = 25;
	
	/**
	 * 
	 * @return
	 */
	public static TicketServiceConfig getDefaultConfig() {
		TicketServiceConfig defaultConfig = new TicketServiceConfig();
		defaultConfig.setTicketPurchaseLimit(DEFAULT_MAX_TICKETS);
		defaultConfig.setPrice(TicketTypeRequest.Type.INFANT, Integer.valueOf(DEFAULT_INFANT_TICKET_PRICE));
		defaultConfig.setPrice(TicketTypeRequest.Type.INFANT, Integer.valueOf(DEFAULT_INFANT_TICKET_PRICE));
		defaultConfig.setPrice(TicketTypeRequest.Type.INFANT, Integer.valueOf(DEFAULT_INFANT_TICKET_PRICE));
		return defaultConfig;
	};
	

    private Properties configProp = null;
    private int ticketPurchaselimit;
    private final Map<TicketTypeRequest.Type, Integer> ticketPrices;

    protected TicketServiceConfig() {
		this.ticketPurchaselimit = 0;
		this.ticketPrices = new HashMap<>();
    	
	}
    
    /**
     * 
     * @param configFilePath
     * @throws IOException
     */
    public TicketServiceConfig(String configFilePath) throws IOException {
    	ticketPurchaselimit = DEFAULT_MAX_TICKETS;
        ticketPrices = new HashMap<>();
        loadConfig(configFilePath);
    }


	/**
     * 
     * @param configFilePath
     * @throws IOException
     */
    private void loadConfig(String configFilePath) throws IOException {
        this.configProp = new Properties();
        try (FileInputStream input = new FileInputStream(configFilePath)) {
        	configProp.load(input);
        }

        // Load prices from the configuration file
        setPrice(TicketTypeRequest.Type.INFANT, Integer.parseInt(configProp.getProperty(PROP_INFANT_PRICE, String.valueOf(DEFAULT_INFANT_TICKET_PRICE))));
        setPrice(TicketTypeRequest.Type.INFANT, Integer.parseInt(configProp.getProperty(PROP_CHILD_PRICE, String.valueOf(DEFAULT_CHILD_TICKET_PRICE))));
        setPrice(TicketTypeRequest.Type.ADULT, Integer.parseInt(configProp.getProperty(PROP_ADULT_PRICE, String.valueOf(DEFAULT_ADULT_TICKET_PRICE))));
        
        // load max number of tickets from configuration file
        ticketPrices.put(TicketTypeRequest.Type.ADULT, Integer.parseInt(configProp.getProperty(PROP_ADULT_PRICE, String.valueOf(DEFAULT_ADULT_TICKET_PRICE))));
    }

    /**
     * 
     * @param type
     * @return
     */
    public int getPrice(TicketTypeRequest.Type type) {
        return ticketPrices.getOrDefault(type, 0);
    }
    
    /**
     * 
     * @param type
     * @param price
     */
    protected void setPrice(TicketTypeRequest.Type type, Integer price) {
    	this.ticketPrices.put(type, price);
    }
    
    /**
     * 
     * @return
     */
    public int getTicketPurchaseLimit() {
    	return ticketPurchaselimit;
    }
    
    protected void setTicketPurchaseLimit(int maxNumOfTickets) {
    	this.ticketPurchaselimit = maxNumOfTickets;
    }
    
    /**
     * 
     * @param configKey
     * @return
     */
    public String getConfigValue(String configKey) {
    	if (configKey == null) return null;
    	if (this.configProp == null) return null;
    	return this.configProp.getProperty(configKey);
    	
    }
}