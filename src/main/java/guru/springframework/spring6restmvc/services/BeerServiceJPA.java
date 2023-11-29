package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    //If the pageNumber and pageSize parameter are not set, we want default page to be 0, and size to be 25.
    private final static int DEFAULT_PAGE = 0;
    private final static int DEFAULT_PAGE_SIZE = 25;

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){

        int queryPageNumber;
        int queryPageSize;

        if(pageNumber != null && pageNumber > 0){
            queryPageNumber = pageNumber - 1; //pageNumber are 0 indexed, but API specification has default pageNumber 1. So we subtract 1.
        }
        else//pageNumber field is not passed
        {
            queryPageNumber = DEFAULT_PAGE;
        }

        if (pageSize == null){

            queryPageSize = DEFAULT_PAGE_SIZE;
        }
        else {

            if(pageSize > 1000){ //Defensive coding: If someone requests a large pageSize, we default to returning only 1000
                queryPageSize = 1000;
            }
            else {
                queryPageSize = pageSize;
            }
        }

        Sort sort = Sort.by(Sort.Order.asc("beerName"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort); //Creates a PageRequest
    }

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                   Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        Page<Beer> beerPage;
        if(StringUtils.hasText(beerName) && beerStyle == null){
            beerPage = listBeerByName(beerName, pageRequest);
        }
        else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeerByStyle(beerStyle, pageRequest);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeerByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory){ //If showInventory if False, then we set the quantityOnHand field to null
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }
        return beerPage.map(beerMapper::beerToBeerDto);
    }
    public Page<Beer> listBeerByName(String beerName, Pageable pageable){
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageable);
    }
    public Page<Beer> listBeerByStyle(BeerStyle beerStyle, Pageable pageable){
        return beerRepository.findAllByBeerStyle(beerStyle, pageable);
    }
    public Page<Beer> listBeerByNameAndStyle(String beerName, BeerStyle beerStyle, Pageable pageable) {
        return beerRepository.
                findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageable);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {

        Optional<Beer> beer = beerRepository.findById(id);

        //orElse method is used to retrieve object inside Optional,
        // if optional is empty it returns null
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer.orElse(null));

        return Optional.ofNullable(beerDTO);
    }



    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        Beer entity = beerMapper.beerDtoToBeer(beer);
        Beer savedEntity = beerRepository.save(entity);
        return beerMapper.beerToBeerDto(savedEntity);
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {

        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(
                foundBeer -> {
                    foundBeer.setBeerName(beer.getBeerName());
                    foundBeer.setBeerStyle(beer.getBeerStyle());
                    foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
                    foundBeer.setPrice(beer.getPrice());
                    foundBeer.setUpc(beer.getUpc());
                    Optional<BeerDTO>beerDTO = Optional.of(beerMapper.beerToBeerDto(foundBeer));
                    atomicReference.set(beerDTO);
                },
                () ->{
                    atomicReference.set(Optional.empty());
                }
        );
        return atomicReference.get();
    }

    @Override
    public Boolean deleteBeerById(UUID beerId) {

        if(beerRepository.existsById(beerId)){
            beerRepository.deleteById(beerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerID, BeerDTO beer) {

        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerID).ifPresentOrElse(
                oldBeer -> {
                    if(StringUtils.hasText(beer.getBeerName())){
                        oldBeer.setBeerName(beer.getBeerName());
                    }
                    if(beer.getBeerStyle() != null){
                        oldBeer.setBeerStyle(beer.getBeerStyle());
                    }
                    if(beer.getPrice() != null){
                        oldBeer.setPrice(beer.getPrice());
                    }
                    if(StringUtils.hasText(beer.getUpc())){
                        oldBeer.setUpc(beer.getUpc());
                    }
                    if(beer.getQuantityOnHand() != null){
                        oldBeer.setQuantityOnHand(beer.getQuantityOnHand());
                    }
                    beerRepository.save(oldBeer);
                    Optional<BeerDTO> beerDTO = Optional.of(beerMapper.beerToBeerDto(oldBeer));
                    atomicReference.set(beerDTO);
                },
                () -> {
                    atomicReference.set(Optional.empty());
                }
        );
        return atomicReference.get();
    }
}
