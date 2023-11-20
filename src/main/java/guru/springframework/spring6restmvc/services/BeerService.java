package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.Beer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<Beer> getBeerById(UUID id);
    List<Beer> listBeers();

    Beer saveNewBeer(Beer beer);

    void updateBeerById(UUID beerId, Beer beer);

    void deleteBeerById(UUID beerId);

    void patchBeerById(UUID beerID, Beer beer);
}
