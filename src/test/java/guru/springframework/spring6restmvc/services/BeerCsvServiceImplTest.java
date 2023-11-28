package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerCSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
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

class BeerCsvServiceImplTest {
    
    BeerCsvService beerCsvService = new BeerCsvServiceImpl();

    @Test
    void convertCSV() throws FileNotFoundException {

        File file = ResourceUtils.getFile("classpath:csvdata/beers.csv");

        List<BeerCSVRecord> beerCSVRecords = beerCsvService.convertCSV(file);

        System.out.println(beerCSVRecords.size());

        assertThat(beerCSVRecords.size()).isGreaterThan(0);
    }
}