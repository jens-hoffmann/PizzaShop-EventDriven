package com.example.pizzashop.catalog.adapter.out.persistence;

import com.example.pizzashop.catalog.application.port.out.PizzaRepository;
import com.example.pizzashop.catalog.domain.model.Pizza;
import com.example.pizzashop.catalog.domain.model.PizzaSize;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
class PizzaRepositoryAdapter implements PizzaRepository {

    private final SpringDataPizzaRepository repository;

    PizzaRepositoryAdapter(SpringDataPizzaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Pizza> findAll(Optional<List<UUID>> ids, Optional<Boolean> available) {
        List<PizzaJpaEntity> entities = available
                .map(repository::findByAvailableOrderByNameAsc)
                .orElseGet(repository::findAllByOrderByNameAsc);
        return entities.stream()
                .filter(entity -> ids.isEmpty() || ids.get().contains(entity.getId()))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Pizza> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Pizza> findByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public Pizza save(Pizza pizza) {
        return toDomain(repository.save(toEntity(pizza)));
    }

    @Override
    public void delete(Pizza pizza) {
        repository.deleteById(pizza.id());
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).isPresent();
    }

    private Pizza toDomain(PizzaJpaEntity entity) {
        Map<PizzaSize, java.math.BigDecimal> prices = new EnumMap<>(PizzaSize.class);
        prices.put(PizzaSize.SMALL, entity.getSmallPrice());
        prices.put(PizzaSize.MEDIUM, entity.getMediumPrice());
        prices.put(PizzaSize.LARGE, entity.getLargePrice());
        return Pizza.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isAvailable(),
                prices,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private PizzaJpaEntity toEntity(Pizza pizza) {
        PizzaJpaEntity entity = new PizzaJpaEntity();
        entity.setId(pizza.id());
        entity.setName(pizza.name());
        entity.setDescription(pizza.description());
        entity.setAvailable(pizza.available());
        entity.setSmallPrice(pizza.priceFor(PizzaSize.SMALL));
        entity.setMediumPrice(pizza.priceFor(PizzaSize.MEDIUM));
        entity.setLargePrice(pizza.priceFor(PizzaSize.LARGE));
        entity.setCreatedAt(pizza.createdAt());
        entity.setUpdatedAt(pizza.updatedAt());
        entity.setVersion(pizza.version());
        return entity;
    }
}
