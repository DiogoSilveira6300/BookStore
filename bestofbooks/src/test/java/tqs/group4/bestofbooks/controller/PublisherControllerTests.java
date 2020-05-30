package tqs.group4.bestofbooks.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tqs.group4.bestofbooks.utils.Json.toJson;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;

import tqs.group4.bestofbooks.dto.BookDTO;
import tqs.group4.bestofbooks.dto.BookListDTO;
import tqs.group4.bestofbooks.dto.RevenueDTO;
import tqs.group4.bestofbooks.dto.StockDto;
import tqs.group4.bestofbooks.dto.UserDto;
import tqs.group4.bestofbooks.exception.BookNotFoundException;
import tqs.group4.bestofbooks.exception.ForbiddenUserException;
import tqs.group4.bestofbooks.exception.LoginRequiredException;
import tqs.group4.bestofbooks.exception.RepeatedBookIsbnException;
import tqs.group4.bestofbooks.exception.UserNotFoundException;
import tqs.group4.bestofbooks.mocks.RevenueMocks;
import tqs.group4.bestofbooks.model.Book;
import tqs.group4.bestofbooks.service.LoginService;
import tqs.group4.bestofbooks.service.RevenueService;
import tqs.group4.bestofbooks.service.StockService;

@WebMvcTest(PublisherController.class)
public class PublisherControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StockService stockService;

    @MockBean
    private RevenueService revenueService;

	@MockBean
	private LoginService loginService;

    Pageable p = PageRequest.of(0, 20);

		 
	 @AfterEach
	 public void after() {
		 reset(stockService);
		 reset(revenueService);
	 }
	 
	 @Test
	 void givenValidPublisherTokenValidName_whenGetAvailableStock_thenReturnJsonWithBooks() throws JsonProcessingException, Exception {
		 UserDto dto = new UserDto("username", "Publisher");
		 dto.addAttribute("name", "PublisherName");
		 dto.addAttribute("tin", "PublisherTIN");
		 Book b1 = new Book("1234567891234", "Title 1", "Author 1", "Description 1", 20, 5,
	                "Travelogue", "Publisher");
		 Book b2 = new Book("9876543216842", "Title 2", "Author 2", "Description 2", 15, 3,
	                "Travelogue", "Publisher");
		 Pageable p = PageRequest.of(0, 20);
	     Page<Book> bookPage = new PageImpl<>(Lists.newArrayList(b1, b2), p, 2);
		 given(stockService.getPublisherBooks(eq("Publisher"), any(HttpServletRequest.class), eq(p))).willReturn(bookPage);
		 
		 String url = "/api/publisher/Publisher/stock/";
		 
		 mvc.perform(get(url)
	                .contentType(MediaType.APPLICATION_JSON)
	        ).andExpect(status()
	                .isOk())
	           .andExpect(content().json(toJson(bookPage)));
		 
		 verify(stockService, VerificationModeFactory.times(1)).getPublisherBooks(eq("Publisher"), any(HttpServletRequest.class), eq(p));
	 }
	 
	 @Test
	 void givenInvalidPublisherToken_whenGetAvailableStock_thenStatusUnauthorized() throws JsonProcessingException, Exception {
		 Pageable p = PageRequest.of(0, 20);
		 given(stockService.getPublisherBooks(eq("Publisher"),any(HttpServletRequest.class), eq(p))).willThrow(new LoginRequiredException("Login required for this request."));
		 String url = "/api/publisher/Publisher/stock/";
		 
		 mvc.perform(get(url)
	                .contentType(MediaType.APPLICATION_JSON)
	        ).andExpect(status()
	                .isUnauthorized());
		 
		 verify(stockService, VerificationModeFactory.times(1)).getPublisherBooks(eq("Publisher"), any(HttpServletRequest.class), eq(p));
	 }
	 
	 @Test
	 void givenUnauthorizedOrMismatchedPublisherToken_whenGetAvailableStock_thenStatusForbidden() throws JsonProcessingException, Exception {
		 Pageable p = PageRequest.of(0, 20);
		 given(stockService.getPublisherBooks(eq("Publisher"),any(HttpServletRequest.class), eq(p))).willThrow(new ForbiddenUserException("User not allowed."));
		 String url = "/api/publisher/Publisher/stock/";
		 
		 mvc.perform(get(url)
	                .contentType(MediaType.APPLICATION_JSON)
	        ).andExpect(status()
	                .isForbidden());
		 
		 verify(stockService, VerificationModeFactory.times(1)).getPublisherBooks(eq("Publisher"), any(HttpServletRequest.class), eq(p));
	 }
	 
	 @Test
	 void givenValidPublisherTokenAndValidNameAndValidBookIsbn_whenUpdateAvailableStock_thenReturnJsonWithStockDto() throws JsonProcessingException, Exception {
		 UserDto dto = new UserDto("username", "Publisher");
		 dto.addAttribute("name", "PublisherName");
		 dto.addAttribute("tin", "PublisherTIN");
		 StockDto InstockDto = new StockDto("1234567891234", 5);
		 StockDto OutstockDto = new StockDto("1234567891234", 10);
		 given(stockService.updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class))).willReturn(OutstockDto);
		 
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(InstockDto);
		 
		 mvc.perform(put(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(body)
	        ).andExpect(status()
	                .isOk())
	           .andExpect(content().json(toJson(OutstockDto)));
		 verify(stockService, VerificationModeFactory.times(1)).updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenInvalidPublisherToken_whenUpdateAvailableStock_thenStatusUnauthorized() throws JsonProcessingException, Exception {
		 StockDto InstockDto = new StockDto("1234567891234", 5);
		 given(stockService.updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class))).willThrow(new LoginRequiredException("Login required for this request."));
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(InstockDto);
		 
		 mvc.perform(put(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(body)
	        ).andExpect(status()
	                .isUnauthorized());
		 
		 verify(stockService, VerificationModeFactory.times(1)).updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenUnauthorizedOrMismatchedPublisherToken_whenUpdateAvailableStock_thenStatusForbidden() throws JsonProcessingException, Exception {
		 StockDto InstockDto = new StockDto("1234567891234", 5);
		 given(stockService.updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class))).willThrow(new ForbiddenUserException("User not allowed."));
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(InstockDto);
		 
		 mvc.perform(put(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(body)
	        ).andExpect(status()
	                .isForbidden());
		 
		 verify(stockService, VerificationModeFactory.times(1)).updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenValidPublisherTokenAndValidNameAndInvalidBookIsbn_whenUpdateAvailableStock_thenStatusNotFound() throws Exception {
		 StockDto InstockDto = new StockDto("1234567891234", 5);
		 given(stockService.updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class))).willThrow(new BookNotFoundException("Book with " + InstockDto.getIsbn() + " was not found in the platform."));
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(InstockDto);
		 
		 mvc.perform(put(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(body)
	        ).andExpect(status()
	                .isNotFound());
		 
		 verify(stockService, VerificationModeFactory.times(1)).updateBookStock(eq("Publisher"), eq(InstockDto), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenValidPublisherTokenAndValidNameAndValidBookDTOList_whenAddBooks_thenStatusNoContent() throws Exception {
		 String publisherName = "Publisher";
		 BookDTO b1 = new BookDTO("1234567891234", "Title 1", "Author 1", "Description 1", 20, 5,
	                "Travelogue");
		 BookDTO b2 = new BookDTO("1234567891234", "Title 2", "Author 2", "Description 2", 18, 3,
	               "Travelogue");
		 List<BookDTO> l = new ArrayList<>();
		 l.add(b1);
		 l.add(b2);
		 List<Book> lb = new ArrayList<>();
		 lb.add(b1.getBookObject(publisherName));
		 lb.add(b2.getBookObject(publisherName));
		 BookListDTO input = new BookListDTO(lb);
		 given(stockService.addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class))).willReturn(input);
		 
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(l);
		 
		 mvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andExpect(status()
				.isCreated())
		 	.andExpect(content().json(toJson(input)));
		 
		 verify(stockService, VerificationModeFactory.times(1)).addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenUnauthorizedOrMismatchedPublisherToken_whenAddBooks_thenStatusForbidden() throws Exception {
		 BookDTO b1 = new BookDTO("1234567891234", "Title 1", "Author 1", "Description 1", 20, 5,
	                "Travelogue");
		 BookDTO b2 = new BookDTO("1234567891234", "Title 2", "Author 2", "Description 2", 18, 3,
	               "Travelogue");
		 List<BookDTO> l = new ArrayList<>();
		 l.add(b1);
		 l.add(b2);
		 given(stockService.addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class))).willThrow(new ForbiddenUserException("User not allowed."));
		 
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(l);
		 
		 mvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andExpect(status()
				.isForbidden());
		 
		 verify(stockService, VerificationModeFactory.times(1)).addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenRequestWithoutToken_whenAddBooks_thenStatusForbidden() throws Exception {
		 BookDTO b1 = new BookDTO("1234567891234", "Title 1", "Author 1", "Description 1", 20, 5,
	                "Travelogue");
		 BookDTO b2 = new BookDTO("1234567891234", "Title 2", "Author 2", "Description 2", 18, 3,
	               "Travelogue");
		 List<BookDTO> l = new ArrayList<>();
		 l.add(b1);
		 l.add(b2);
		 given(stockService.addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class))).willThrow(new LoginRequiredException("Login required for this request."));
		 
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(l);
		 
		 mvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andExpect(status()
				.isUnauthorized());
		 
		 verify(stockService, VerificationModeFactory.times(1)).addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class));
	 }
	 
	 @Test
	 void givenValidPublisherTokenAndValidNameAndBookDTOListRepeatedIsbn_whenAddBooks_thenStatusBadRequest() throws Exception {
		 BookDTO b1 = new BookDTO("1234567891234", "Title 1", "Author 1", "Description 1", 20, 5,
	                "Travelogue");
		 BookDTO b2 = new BookDTO("1234567891234", "Title 2", "Author 2", "Description 2", 18, 3,
	               "Travelogue");
		 List<BookDTO> l = new ArrayList<>();
		 l.add(b1);
		 l.add(b2);
		 given(stockService.addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class))).willThrow(new RepeatedBookIsbnException("Tried to add book with repeated isbn."));
		 
		 String url = "/api/publisher/Publisher/stock/";
		 String body = toJson(l);
		 
		 mvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
			).andExpect(status()
				.isBadRequest());
		 
		 verify(stockService, VerificationModeFactory.times(1)).addNewBook(eq("Publisher"), eq(l), any(HttpServletRequest.class));
	 }

    @Test
    void givenExistentPublisherName_whenGetRevenuesByPublisherName_thenReturnJson() throws Exception {
        doNothing().when(loginService).checkIfUserIsTheRightPublisher("Publisher 1", "pub1");
        String knownPublisher = RevenueMocks.revenue1.getPublisherName();
        String url = "/api/publisher/" + knownPublisher + "/revenue";

        Page<RevenueDTO> revenueDTOPage = new PageImpl<>(Lists.newArrayList(RevenueDTO.fromRevenue(RevenueMocks.revenue1)), p, 1);
        given(revenueService.getRevenuesByPublisher(knownPublisher, p)).willReturn(revenueDTOPage);

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status()
                .isOk())
           .andExpect(content().json(toJson(revenueDTOPage)));
        verify(revenueService, VerificationModeFactory.times(1)).getRevenuesByPublisher(knownPublisher, p);
    }

    @Test
    void givenUnknownPublisherName_thenThrowHTTPStatusNotFound_forRevenues() throws Exception {
        String unknownPublisher = "none";
        String url = "/api/publisher/" + unknownPublisher + "/revenue";

        given(revenueService.getRevenuesByPublisher(unknownPublisher, p)).willThrow(new UserNotFoundException());

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void givenExistentPublisherName_whenGetTotalRevenuesByPublisherName_thenReturnJson() throws Exception {
		doNothing().when(loginService).checkIfUserIsTheRightPublisher("Publisher 1", "pub1");
        String knownPublisher = RevenueMocks.revenue1.getPublisherName();
        String url = "/api/publisher/" + knownPublisher + "/revenue/total";

        given(revenueService.getRevenuesTotalByPublisher(knownPublisher)).willReturn(500.00);

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status()
                .isOk())
           .andExpect(content().json(toJson(500.00)));
        verify(revenueService, VerificationModeFactory.times(1)).getRevenuesTotalByPublisher(knownPublisher);
    }

    @Test
    void givenUnknownPublisherName_thenThrowHTTPStatusNotFound_forTotalInRevenues() throws Exception {
        String unknownPublisher = "none";
        String url = "/api/publisher/" + unknownPublisher + "/revenue/total";

        given(revenueService.getRevenuesTotalByPublisher(unknownPublisher)).willThrow(new UserNotFoundException());

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

}
