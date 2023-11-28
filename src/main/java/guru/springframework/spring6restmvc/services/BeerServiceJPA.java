package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
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
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {

        Optional<Beer> beer = beerRepository.findById(id);

        //orElse method is used to retrieve object inside Optional,
        // if optional is empty it returns null
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer.orElse(null));

        return Optional.ofNullable(beerDTO);
    }

    public List<Beer> listBeerByName(String beerName){
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%");//Concatnating sql wildcard with beerName
    }
    public List<Beer> listBeerByStyle(BeerStyle beerStyle){
        return beerRepository.findAllByBeerStyle(beerStyle);
    }
    private List<Beer> listBeerByNameAndStyle(String beerName, BeerStyle beerStyle) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle);
    }
    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory) {
        List<Beer> beerList;
        if(StringUtils.hasText(beerName) && beerStyle == null){
            beerList = listBeerByName(beerName);
        }
        else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            beerList = listBeerByStyle(beerStyle);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            beerList = listBeerByNameAndStyle(beerName, beerStyle);
        } else {
            beerList = beerRepository.findAll();
        }

        if (showInventory != null && !showInventory){ //If showInventory if False, then we set the quantityOnHand field to null
            beerList.forEach(beer -> beer.setQuantityOnHand(null));
        }
        return beerList.stream()
                .map(beerMapper::beerToBeerDto)
                .collect(Collectors.toList());
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
