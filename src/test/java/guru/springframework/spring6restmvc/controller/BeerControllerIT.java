package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testListBeersByName() throws Exception {

        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(336)));
    }

    @Test
    void testListBeerByStyle() throws Exception {

        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(548)));
    }

    @Test
    void testListBeerByStyleAndName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerName", "IPA")
                .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)));
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerName", "IPA")
                .queryParam("beerStyle", BeerStyle.IPA.name())
                .queryParam("showInventory", "True"))//If showInventory is True, then there should be quantityOnHand field in the json value.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()));
    }
    @Test
    void testListBeersByStyleAndNameShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "False"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void testPatchBeerName() throws Exception {

        Beer beer = beerRepository.findAll().get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Nameeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

        MvcResult mvcResult = mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Transactional
    @Test
    void testEmptyList() {
        beerRepository.deleteAll();

        assertThat(beerController.listBeers(null, null, false).size()).isEqualTo(0);
    }
    @Test
    void testListBeers() {
        List<BeerDTO> list = beerController.listBeers(null, null, false);

        assertThat(list.size()).isEqualTo(2413);

    }

    @Test
    void testBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () ->{
            beerController.getBeerById(UUID.randomUUID());
        });
    }

    @Test
    void testGetBeerById() {
        Beer beer = beerRepository.findAll().get(0);

        BeerDTO dto = beerController.getBeerById(beer.getId());
        assertThat(dto).isNotNull();
    }

    @Rollback
    @Transactional //We are modifying the database,
    //so we should rollback the changes after test is completed.
    @Test
    void saveNewBeerTest() {

        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New Beer")
                .build();

        ResponseEntity responseEntity = beerController.handlePost(beerDTO);

//checks status code 201
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
//checks location header exist
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID =responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

//checks beer is saved in repo
        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
    }

    @Test
    void testUpdateBeerNotFound() {

        assertThrows(NotFoundException.class, () ->{
            beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateBeer(){
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setVersion(null);
        beerDTO.setId(null);
        final String name = "Updated Name";
        beerDTO.setBeerName(name);

        ResponseEntity responseEntity = beerController.updateById(beer.getId(), beerDTO);

        //Checks if Status code is correct
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        //Checks if updated beer has correct name.
        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(name);
    }

    @Test
    void testDeleteByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            beerController.deleteById(UUID.randomUUID());
        });
    }

    @Transactional
    @Rollback
    @Test
    public void testDeleteById() {

        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity responseEntity = beerController.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

       assertThat(beerRepository.findById(beer.getId()).isEmpty());
    }

    @Test
    void testPatchByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            beerController.updateBeerPatchById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Transactional
    @Rollback
    @Test
    void testPatchById() {

        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);

        final String name = "Patched Name";
        beerDTO.setBeerName(name);

        ResponseEntity responseEntity = beerController.updateBeerPatchById(beer.getId(), beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Beer patchedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(patchedBeer.getBeerName()).isEqualTo(name);
    }
}