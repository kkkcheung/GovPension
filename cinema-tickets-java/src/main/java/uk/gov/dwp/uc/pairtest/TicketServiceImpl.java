package uk.gov.dwp.uc.pairtest;

import java.util.HashMap;
import java.util.Map;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

// Implementation of the TicketService
public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;
    private final TicketServiceConfig serviceConfig;
    
    /**
     * 
     * @param paymentService
     * @param reservationService
     */
    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.serviceConfig = TicketServiceConfig.getDefaultConfig();
    }
    
    /**
     * 
     * @param paymentService
     * @param reservationService
     * @param serviceConfig
     */
    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService, TicketServiceConfig serviceConfig) {
    	this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.serviceConfig = serviceConfig;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("Invalid account ID");
        }

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        int totalTickets = 0;
        int totalCost = 0;
        int totalSeats = 0;
        
        int ticketPriceAdult = serviceConfig.getPrice(TicketTypeRequest.Type.ADULT);
        int ticketPriceChild = serviceConfig.getPrice(TicketTypeRequest.Type.CHILD);
        int ticketPriceInfant = serviceConfig.getPrice(TicketTypeRequest.Type.INFANT);

        // Calculate total tickets, cost, and seats
        for (TicketTypeRequest request : ticketTypeRequests) {
            int quantity = request.getNoOfTickets();
            TicketTypeRequest.Type type = request.getTicketType();

            ticketCounts.put(type, ticketCounts.getOrDefault(type, 0) + quantity);
            totalTickets += quantity;
            
            switch (type) {
                case ADULT:
                    totalCost += ticketPriceAdult * quantity;
                    totalSeats += quantity;  // Adults need seats
                    break;
                case CHILD:
                    totalCost += ticketPriceChild * quantity;
                    totalSeats += quantity;  // Children need seats
                    break;
                case INFANT:
                    // Infants do not need a seat or pay for a ticket
                    // No additional cost or seats needed for infants
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ticket type: " + type);
            }
            
        }

        validateTicketPurchase(ticketCounts, totalTickets);

        // Make payment and reserve seats
        paymentService.makePayment(accountId, totalCost);
        reservationService.reserveSeat(accountId, totalSeats);
    }

    /**
     * Validate the purchase of tickets
     * @param ticketCounts
     * @param totalTickets
     */
    private void validateTicketPurchase(Map<TicketTypeRequest.Type, Integer> ticketCounts, int totalTickets) {
    	
    	if (totalTickets < 0) {
    		throw new InvalidPurchaseException("Purchase tickets ("+ totalTickets +") cannot less than  0  tickets");
    	}
    		
    	int maxTickets = this.serviceConfig.getTicketPurchaseLimit();
        if (totalTickets > maxTickets) {
            throw new InvalidPurchaseException("Purchase tickets ("+ totalTickets +") cannot more than (" + maxTickets + ") tickets");
        }

        int adultTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
    	if (adultTickets < 0) {
    		throw new InvalidPurchaseException("Purchase adult tickets ("+ adultTickets +") cannot less than  0  tickets");
    	}
    	
        int childTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
    	if (childTickets < 0) {
    		throw new InvalidPurchaseException("Purchase child tickets ("+ childTickets +") cannot less than  0  tickets");
    	}
    	
        int infantTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
    	if (infantTickets < 0) {
    		throw new InvalidPurchaseException("Purchase infant tickets ("+ infantTickets +") cannot less than  0  tickets");
    	}

        if ((childTickets > 0 || infantTickets > 0) && adultTickets == 0) {
            throw new InvalidPurchaseException("Child tickets ( "+ childTickets +") or Infant  tickets ("+infantTickets+ ") cannot be purchased without any Adult ticket ("+adultTickets +").");
        }
    }
    
    /**
     * Print out the details for a purchase of tickets
     * @param accountId
     * @param ticketTypeRequests
     */
    public void printTicketsPurchase(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("Invalid account ID");
        }

        int totalTickets = 0;
        int totalCost = 0;
        int totalSeats = 0;

        int ticketPriceAdult = serviceConfig.getPrice(TicketTypeRequest.Type.ADULT);
        int ticketPriceChild = serviceConfig.getPrice(TicketTypeRequest.Type.CHILD);
        int ticketPriceInfant = serviceConfig.getPrice(TicketTypeRequest.Type.INFANT);

        int ticketAdult = 0;
        int ticketChild = 0;
        int ticketInfant = 0;

        System.out.println("Ticket Purchase Summary:");
        System.out.println("Account ID: [" + accountId + "]");

        // Loop through the ticket type requests
        for (TicketTypeRequest request : ticketTypeRequests) {
            int quantity = request.getNoOfTickets();
            TicketTypeRequest.Type type = request.getTicketType();

            switch (type) {
                case ADULT:
                    ticketAdult += quantity;
                    totalCost += ticketPriceAdult * quantity;
                    totalSeats += quantity;  // Adults require seats
                    break;
                case CHILD:
                    ticketChild += quantity;
                    totalCost += ticketPriceChild * quantity;
                    totalSeats += quantity;  // Children require seats
                    break;
                case INFANT:
                    ticketInfant += quantity;
                    // Infants do not require seats and do not add to the cost
                    break;
            }

            totalTickets += quantity;
        }

        System.out.println("Total Tickets: " + totalTickets);
        System.out.println("Ticket Type: ADULT, Quantity: " + ticketAdult + ", Price per ticket: £" + ticketPriceAdult);
        System.out.println("Ticket Type: CHILD, Quantity: " + ticketChild + ", Price per ticket: £" + ticketPriceChild);
        System.out.println("Ticket Type: INFANT, Quantity: " + ticketInfant + ", Price per ticket: £" + ticketPriceInfant);
        System.out.println("Total Seats Reserved: " + totalSeats);
        System.out.println("Total Cost: £" + totalCost);
    }

}