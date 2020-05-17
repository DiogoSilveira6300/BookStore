package tqs.group4.bestofbooks.integration;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.group4.bestofbooks.BestofbooksApplication;
import tqs.group4.bestofbooks.mocks.BookMocks;
import tqs.group4.bestofbooks.model.Book;


import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tqs.group4.bestofbooks.utils.Json.toJson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BestofbooksApplication.class)
@AutoConfigureMockMvc
@Transactional
public class BookControllerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void before() {
        entityManager.createNativeQuery("TRUNCATE books, orders, commissions, books_orders").executeUpdate();
    }

    @AfterEach
    public void after() {
        entityManager.remove(BookMocks.infiniteJest);
        entityManager.remove(BookMocks.onTheRoad);
        entityManager.flush();
    }

    @Test
    void givenExistentIsbn_whenGetBookByIsbn_thenReturnJson() throws Exception {
        entityManager.persist(BookMocks.onTheRoad);
        entityManager.flush();

        String existentIsbn = BookMocks.onTheRoad.getIsbn();
        String url = "/api/books/isbn/" + existentIsbn;

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status()
                .isOk())
           .andExpect(content().json(toJson(BookMocks.onTheRoad)));
    }

    @Test
    void givenInvalidIsbn_thenThrowHTTPStatusNotFound() throws Exception {
        String invalidIsbn = "123";
        String url = "/api/books/isbn/" + invalidIsbn;

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void givenBookNotFoundException_whenGetBookByIsbn_thenThrowHTTPStatusNotFound() throws Exception {
        String unknownIsbn = "9780140042543";
        String url = "/api/books/isbn/" + unknownIsbn;

        mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
        )
           .andExpect(status().isNotFound());
    }

    @Test
    void givenPageOfBooks_whenGetAvailableBooks_thenReturnJson() throws Exception {
        entityManager.persist(BookMocks.onTheRoad);
        entityManager.persist(BookMocks.infiniteJest);
        entityManager.flush();

        Pageable p = PageRequest.of(0, 20);
        Page<Book> bookPage = new PageImpl<>(Lists.newArrayList(BookMocks.infiniteJest, BookMocks.onTheRoad), p, 2);

        mvc.perform(get("/api/books/available").contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(toJson(bookPage)));
    }

    @Test
    void givenPageOfBooks_whenSearchingForBooks_thenReturnJson() throws Exception {
        entityManager.persist(BookMocks.onTheRoad);
        entityManager.flush();
        Pageable p = PageRequest.of(0, 20);
        Page<Book> bookPage = new PageImpl<>(Lists.newArrayList(BookMocks.onTheRoad), p, 1);

        mvc.perform(get("/api/books/search")
                .contentType(MediaType.APPLICATION_JSON)
                .param("title", BookMocks.onTheRoad.getTitle())
                .param("category", BookMocks.onTheRoad.getCategory())
                .param("author", BookMocks.onTheRoad.getAuthor()))
           .andExpect(status().isOk())
           .andExpect(content().json(toJson(bookPage)));
    }
}
