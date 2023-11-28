package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.bootstrap.BootStrapData;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;


@DataJpaTest
@Import({BootStrapData.class, BeerCsvServiceImpl.class})
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testGetBeerListByNameAndStyle() {

        List<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%IPA%",BeerStyle.IPA);

        assertThat(list.size()).isEqualTo(310);
    }

    @Test
    void testGetBeerListByStyle() {

        List<Beer> list = beerRepository.findAllByBeerStyle(BeerStyle.PALE_ALE);

        assertThat(list.size()).isEqualTo(14);
    }

    @Test
    void testGetBeerListByName() {

        List<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%");

        assertThat(list.size()).isEqualTo(336);
    }

    @Test
    void testSaveBeerNameLong() {

        assertThrows(ConstraintViolationException.class, () -> {
            Beer savedBeer = beerRepository.save(Beer.builder()
                    .beerName("Testttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("upc")
                    .price(BigDecimal.ZERO)
                    .build());

            beerRepository.flush();
        });
    }

    @Test
    void testSaveBeer() {

        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("Test")
                        .beerStyle(BeerStyle.PALE_ALE)
                        .upc("upc")
                        .price(BigDecimal.ZERO)
                .build());

        beerRepository.flush();

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }
}