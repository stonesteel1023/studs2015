package com.cinnober.exercise.ordermatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.*;
import java.util.Collections;
import java.util.Comparator;

/**
 * Order book with continuous matching of limit orders with time priority.
 *
 * <p>In an electronic exchange an order book is kept: All
 * buy and sell orders are entered into this order book and the prices are
 * set according to specific rules. Bids and asks are matched and trades
 * occur.
 * <p>This class keeps an order book, that can determine in real-time the
 * current market price and combine matching orders to trades. Each order
 * has a quantity and a price.
 *
 * <p><b>The trading rules:</b>
 * It is a match if a buy order exist at a higher price or equal to a sell
 * order in the order book. The quantity of both orders is reduced as much as
 * possible. When an order has a quantity of zero it is removed. An order can
 * match several other orders if the quantity is large enough and the price is
 * correct. The price of the trade is computed as the order that was in the
 * order book first (the passive party).
 *
 * <p>The priority of the orders to match is based on the following:
 * <ol>
 * <li> On the price that is best for the active order (the one just entered)
 * <li> On the time the order was entered (first come first served)
 * </ol>
 *
 * <p><b>Note:</b> some methods are not yet implemented. This is your job!
 * See {@link #addOrder(Order)} and {@link #getOrders(Side)}.
 */
public class OrderMatcher {

    private List<Trade> tradeList = new ArrayList<Trade>();
    private List<Order> orderList = new ArrayList<Order>();

    /**
     * Create a new order matcher.
     */
    public OrderMatcher() {
    }
    
    /**
     * Add the specified order to the order book.
     *
     * @param order the order to be added, not null. The order will not be modified by the caller after this call.
     * @return any trades that were created by this order, not null.
     */
    public List<Trade> addOrder(Order order) {
        List<Order> newOrderList = new ArrayList<Order>();
        if(order.getQuantity()!=0){
            orderList.add(order);
            for(Order passiveOrder:orderList){
                if(passiveOrder.getQuantity()!=0 && order.getQuantity()!=0){
                    switch(order.getSide()){
                        case BUY:
                            if(passiveOrder.getSide().equals(Side.SELL) && order.getPrice() >= passiveOrder.getPrice()){
                                createTrade(passiveOrder,order,newOrderList);
                            }else{
                               newOrderList.add(passiveOrder);
                            }
                            break;
                        case SELL:
                            if(passiveOrder.getSide().equals(Side.BUY) && order.getPrice() <= passiveOrder.getPrice()){
                                createTrade(passiveOrder,order,newOrderList);
                            }else{
                                newOrderList.add(passiveOrder);
                            }
                            break;
                    }
                }else if(order.getQuantity()==0 && passiveOrder.getQuantity()!=0){
                        newOrderList.add(passiveOrder);
                }
            }
        }
        orderList = newOrderList;
        System.out.println("trade: ");
        tradeList.stream().map(Trade::toString).forEach(System.out::println);
        return tradeList;
        //throw new UnsupportedOperationException("addOrder is not implemented yet"); // FIXME
    }

    /**
     * Generate trade data and modify old order information
     *
     * @param passiveOrder the order that was already added in the list, not null.
     * @param order the order that was recently added, not null.
     * @param newOrderList the new order list to store modified orders other orders which are not modified.
     */
    private void createTrade(Order passiveOrder,Order order, List<Order> newOrderList){
        if(passiveOrder.getQuantity()>order.getQuantity()){
            tradeList.add(new Trade(order.getId(),passiveOrder.getId(),passiveOrder.getPrice(),order.getQuantity()));
            passiveOrder.setQuantity(passiveOrder.getQuantity()-order.getQuantity());
            order.setQuantity(0);
            newOrderList.add(passiveOrder);
        }else if(passiveOrder.getQuantity()<order.getQuantity()){
            tradeList.add(new Trade(order.getId(),passiveOrder.getId(),passiveOrder.getPrice(),passiveOrder.getQuantity()));
            order.setQuantity(order.getQuantity()-passiveOrder.getQuantity());
            passiveOrder.setQuantity(0);
        }else{
            tradeList.add(new Trade(order.getId(),passiveOrder.getId(),passiveOrder.getPrice(),order.getQuantity()));
            order.setQuantity(0);
            passiveOrder.setQuantity(0);
        }
    }

    /**
     * Returns all remaining orders in the order book, in priority order, for the specified side.
     *
     * <p>Priority for buy orders is defined as highest price, followed by time priority (first come, first served).
     * For sell orders lowest price comes first, followed by time priority (same as for buy orders).
     *
     * @param side the side, not null.
     * @return all remaining orders in the order book, in priority order, for the specified side, not null.
     */
    public List<Order> getOrders(Side side) {
        //throw new UnsupportedOperationException("getOrders is not implemented yet"); // FIXME
        List<Order> outputList = new ArrayList<Order>();
        if(side.equals(Side.BUY)){
            outputList = orderList.stream().filter(j -> j.getSide().equals(Side.BUY)).collect(Collectors.toList());
            Collections.sort(outputList,new Comparator<Order>(){
                @Override
                public int compare(Order o1, Order o2){
                    return Long.valueOf(o2.getPrice()).compareTo(Long.valueOf(o1.getPrice()));
                }
            });
        }else{
            outputList = orderList.stream().filter(j -> j.getSide().equals(Side.SELL)).collect(Collectors.toList());
            Collections.sort(outputList,new Comparator<Order>(){
                @Override
                public int compare(Order o1, Order o2){
                    return Long.valueOf(o1.getPrice()).compareTo(Long.valueOf(o2.getPrice()));
                }
            });
        }
        System.out.println("orders: ");
        outputList.stream().map(Order::toString).forEach(System.out::println);
        return outputList;
    }

    public static void main(String... args) throws Exception {
        OrderMatcher matcher = new OrderMatcher();
        System.out.println("Welcome to the order matcher. Type 'help' for a list of commands.");
        System.out.println();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        LOOP: while ((line=reader.readLine()) != null) {
            line = line.trim();
            try {
                switch(line) {
                    case "help":
                        System.out.println("Available commands: \n"
                                + "  buy|sell <quantity>@<price> [#<id>]  - Enter an order.\n"
                                + "  list                                 - List all remaining orders.\n"
                                + "  quit                                 - Quit.\n"
                                + "  help                                 - Show help (this message).\n");
                        break;
                    case "":
                        // ignore
                        break;
                    case "quit":
                        break LOOP;
                    case "list":
                        System.out.println("BUY:");
                        matcher.getOrders(Side.BUY).stream().map(Order::toString).forEach(System.out::println);
                        System.out.println("SELL:");
                        matcher.getOrders(Side.SELL).stream().map(Order::toString).forEach(System.out::println);
                        break;
                    default: // order
                        matcher.addOrder(Order.parse(line)).stream().map(Trade::toString).forEach(System.out::println);
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Bad input: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.err.println("Sorry, this command is not supported yet: " + e.getMessage());
            }
        }
        System.out.println("Good bye!");
    }
}
