package tqs.group4.bestofbooks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.group4.bestofbooks.dto.OrderDTO;
import tqs.group4.bestofbooks.dto.RequestOrderDTO;
import tqs.group4.bestofbooks.exception.BookNotFoundException;
import tqs.group4.bestofbooks.exception.OrderNotFoundException;
import tqs.group4.bestofbooks.exception.UserNotFoundException;
import tqs.group4.bestofbooks.model.Book;
import tqs.group4.bestofbooks.model.BookOrder;
import tqs.group4.bestofbooks.model.Order;
import tqs.group4.bestofbooks.model.Revenue;
import tqs.group4.bestofbooks.repository.OrderRepository;
import tqs.group4.bestofbooks.repository.RevenueRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tqs.group4.bestofbooks.dto.OrderDTO.fromOrder;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private RevenueService revenueService;

    public OrderDTO getOrderById(int id) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            throw new OrderNotFoundException("No order with the given id.");
        }
//        Collection<BookOrder> bookOrders = order.getBookOrders(); // loads the lazy relationship
        return fromOrder(order);
    }

    public List<OrderDTO> getOrderByBuyerUsername(String buyerUsername) {
        List<Order> orders = orderRepository.findByBuyerUsername(buyerUsername);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders for the given username.");
        }
        return orders.stream().map(OrderDTO::fromOrder).collect(Collectors.toList());
    }

    public OrderDTO createOrderDTO(RequestOrderDTO requestOrderDTO) throws BookNotFoundException, UserNotFoundException {
        Order order = createOrder(requestOrderDTO);
        List<BookOrder> bookOrders = createBooksOrders(order, requestOrderDTO);

        for (BookOrder bookOrder : bookOrders) {
            order.addBookOrder(bookOrder);
        }

        List<Revenue> revenues = createRevenues(bookOrders);
        persistNewOrder(order, revenues);

        return fromOrder(order);
    }

    private Order createOrder(RequestOrderDTO request)
            throws BookNotFoundException, UserNotFoundException {
        double finalPrice = bookService.computeFinalPriceFromIncomingOrder(request.getIncomingBookOrderDTOS());
        return new Order(
                request.getPaymentReference(),
                request.getAddress(),
                finalPrice,
                userService.getBuyerFromUsername(request.getBuyerUsername())
        );
    }

    private List<BookOrder> createBooksOrders(Order order, RequestOrderDTO requestOrderDTO)
            throws BookNotFoundException {
        Map<Book, Integer> booksWithQuantities = bookService.retrieveBooksAndQuantitiesFromIncomingOrderDTOS(
                requestOrderDTO.getIncomingBookOrderDTOS());
        List<BookOrder> bookOrders = new ArrayList<>();

        for (Map.Entry<Book, Integer> entry : booksWithQuantities.entrySet()) {
            BookOrder bookOrder = new BookOrder(entry.getKey(), order, entry.getValue());
            bookOrders.add(bookOrder);
        }
        return bookOrders;
    }

    private List<Revenue> createRevenues(List<BookOrder> bookOrders) {
        List<Revenue> revenues = new ArrayList<>();
        for (BookOrder bookOrder : bookOrders) {
            Revenue revenue = new Revenue(revenueService.computeRevenueAmountByBookOrder(bookOrder),
                    bookOrder, bookOrder.getBook().getPublisherName());
            revenues.add(revenue);
        }
        return revenues;
    }

    private void persistNewOrder(Order order, List<Revenue> revenues) {
        orderRepository.save(order);
        for (Revenue revenue : revenues) {
            revenueRepository.save(revenue);
        }
    }
}