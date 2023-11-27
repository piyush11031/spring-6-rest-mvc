package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Primary
@Service
@RequiredArgsConstructor
public class CustomerServiceJPA implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDTO> getCustomerById(UUID uuid) {
        if (customerRepository.existsById(uuid)){
            Customer customer = customerRepository.findById(uuid).get();
            return Optional.of(customerMapper.customerToCustomerDto(customer));
        }
        return Optional.empty();
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        List<Customer> customerList = customerRepository.findAll();

        return customerList.stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO saveNewCustomer(CustomerDTO customer) {
        Customer savedCustomer = customerRepository.save(customerMapper.customerDtoToCustomer(customer));

        return customerMapper.customerToCustomerDto(savedCustomer);
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customer) {

        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();

        customerRepository.findById(customerId).ifPresentOrElse(
                (oldCustomer) -> {
                    oldCustomer.setName(customer.getName());
                    Customer savedCustomer = customerRepository.save(oldCustomer);
                    CustomerDTO customerDTO = customerMapper.customerToCustomerDto(savedCustomer);
                    atomicReference.set(Optional.of(customerDTO));
                },
                () -> {
                    atomicReference.set(Optional.empty());
                }
        );
        return atomicReference.get();
    }

    @Override
    public Boolean deleteCustomerById(UUID customerId) {

        if(customerRepository.existsById(customerId)){
            customerRepository.deleteById(customerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<CustomerDTO> patchCustomerById(UUID customerId, CustomerDTO customer) {

        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();

        customerRepository.findById(customerId).ifPresentOrElse(
                (oldCustomer) -> {
                    if(StringUtils.hasText(customer.getName())){
                        oldCustomer.setName(customer.getName());
                    }
                    Customer savedCustomer = customerRepository.save(oldCustomer);
                    Optional<CustomerDTO> customerDTO = Optional.of(customerMapper.customerToCustomerDto(savedCustomer));
                    atomicReference.set(customerDTO);
                },
                () -> {
                    atomicReference.set(Optional.empty());
                }
        );
        return atomicReference.get();

    }
}
