package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

//We use SpringBootTest instead of DataJpaTest because DataJpaTest is a test slice, so that test slice does not
//include BootStrapData class which loads up the repository with data. That is why we use SpringBootTest so that
//we can use the full spring boot context which includes BootStrapData.
@SpringBootTest
class BeerOrderRepositoryTest {

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;

// Setting up our test data, we can either create our own data or use data that already exists
    Customer testCustomer;
    Beer testBeer;
    @BeforeEach
    void setUp() {
        testBeer = beerRepository.findAll().get(0);
        testCustomer = customerRepository.findAll().get(0);
    }

    //Enables lazy loading
    @Transactional
    @Test
    void testBeerOrders() {

        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Test Order")
                .beerOrderShipment(BeerOrderShipment.builder()
                                                    .trackingNumber("Test Tracking")
                                                    .build())
                .customer(testCustomer)
                .build();

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        System.out.println(savedBeerOrder.getBeerOrderShipment());

    }

}