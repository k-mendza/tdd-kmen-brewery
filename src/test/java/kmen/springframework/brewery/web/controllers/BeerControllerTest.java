package kmen.springframework.brewery.web.controllers;

import kmen.springframework.brewery.services.BeerService;
import kmen.springframework.brewery.web.model.BeerDto;
import kmen.springframework.brewery.web.model.BeerPagedList;
import kmen.springframework.brewery.web.model.BeerStyleEnum;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @MockBean
    BeerService service;

    @Autowired
    MockMvc mockMvc;

    BeerDto validBeer;

    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder().id(UUID.randomUUID())
                .version(1)
                .beerName("Beer1")
                .beerStyle(BeerStyleEnum.PALE_ALE)
                .price(new BigDecimal("12.99"))
                .quantityOnHand(4)
                .upc(123456789L)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(service);
    }

    @Test
    void testGetBeerById() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        given(service.findBeerById(any())).willReturn(validBeer);

        mockMvc.perform(get("/api/v1/beer/" + validBeer.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(validBeer.getBeerName())))
                .andExpect(jsonPath("$.price", is(new BigDecimal("12.99").toString())))
                .andExpect(jsonPath("$.createdDate", is(dateTimeFormatter.format(validBeer.getCreatedDate()))));
    }


    @DisplayName("List Ops - ")
    @Nested
    public class TestListOperations {

        @Captor
        ArgumentCaptor<String> beerNameCaptor;

        @Captor
        ArgumentCaptor<BeerStyleEnum> beerStyleEnumCaptor;

        @Captor
        ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerPagedList beerPagedList;

        @BeforeEach
        void setUp() {
            List<BeerDto> beers = new ArrayList<>();
            beers.add(validBeer);
            beers.add(BeerDto.builder().id(UUID.randomUUID())
                    .version(1)
                    .beerName("Beer4")
                    .upc(123123123122L)
                    .beerStyle(BeerStyleEnum.PALE_ALE)
                    .price(new BigDecimal("12.99"))
                    .quantityOnHand(66)
                    .createdDate(OffsetDateTime.now())
                    .lastModifiedDate(OffsetDateTime.now())
                    .build());

            beerPagedList = new BeerPagedList(beers, PageRequest.of(1, 1), 2L);

            given(service.listBeers(beerNameCaptor.capture(), beerStyleEnumCaptor.capture(),
                    pageRequestCaptor.capture())).willReturn(beerPagedList);
        }

        @DisplayName("Test list beers - no parameters")
        @Test
        void testListBeers() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/beer")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(validBeer.getId().toString())))
                    .andReturn();
            System.out.println(result.getResponse().getContentAsString());
        }
    }
}
