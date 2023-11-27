package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;

    // unhappy path
    @Transactional
    @Rollback
    @Test
    void testEmptyList() {
        customerRepository.deleteAll();

        assertThat(customerController.listCustomers().size()).isEqualTo(0);
    }
    //happy path
    @Test
    void testListCustomer() {
        List<CustomerDTO> list = customerController.listCustomers();

        assertThat(list.size()).isEqualTo(3);

    }

    //unhappy path
    @Test
    void testCustomByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            customerController.getCustomerById(UUID.randomUUID());
        });
    }
    //happy path
    @Test
    void testFindCustomById() {

        Customer customer = customerRepository.findAll().get(0);

        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());

        assertThat(customerDTO).isNotNull();
    }

    //happy path only for save beer
    @Test
    @Transactional
    @Rollback
    void saveNewCustomer() {

        CustomerDTO customer = CustomerDTO.builder().build();

        ResponseEntity responseEntity = customerController.saveCustomer(customer);

        String[] location = responseEntity.getHeaders().getLocation().toString().split("/");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        UUID savedUUID = UUID.fromString(location[4]);
        Customer savedCustomer = customerRepository.findById(savedUUID).get();

        assertThat(savedCustomer).isNotNull();
    }

    //non happy path
    @Test
    void testUpdateCustomByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            customerController.updateCustomerById(UUID.randomUUID(), CustomerDTO.builder().build());
        });
    }

    //happy path
    @Transactional
    @Rollback
    @Test
    void testUpdateCustomById() {

        final String name = "Name";
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);
        customerDTO.setVersion(null);
        customerDTO.setId(null);

        customerDTO.setName(name);

        ResponseEntity responseEntity = customerController.updateCustomerById(customer.getId(), customerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Customer updatedCustomer = customerRepository.findById(customer.getId()).get();
        assertThat(updatedCustomer.getName()).isEqualTo(name);
    }

    //unhappy path
    @Test
    void testDeleteByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            customerController.deleteCustomerById(UUID.randomUUID());
        });
    }
    //happy path
    @Transactional
    @Rollback
    @Test
    void testDeleteById() {

        Customer customer = customerRepository.findAll().get(0);

        ResponseEntity responseEntity = customerController.deleteCustomerById(customer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Optional<Customer> deletedCustomer = customerRepository.findById(customer.getId());
        assertThat(deletedCustomer).isEmpty();
    }

    //unhappy path

    @Test
    void testPatchByIdNotFound() {

        assertThrows(NotFoundException.class, () -> {
            customerController.patchCustomerById(UUID.randomUUID(), CustomerDTO.builder().build());
        });
    }

    //happy path
    @Transactional
    @Rollback
    @Test
    void testPatchById() {

        final String name = "Name";
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);
        customerDTO.setVersion(null);
        customerDTO.setId(null);

        customerDTO.setName(name);

        ResponseEntity responseEntity = customerController.patchCustomerById(customer.getId(), customerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Customer patchedCustomer = customerRepository.findById(customer.getId()).get();
        assertThat(patchedCustomer.getName()).isEqualTo(name);
    }
}