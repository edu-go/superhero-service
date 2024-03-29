package com.edugo.superheroservice.service;

import com.edugo.superheroservice.domain.Superhero;
import com.edugo.superheroservice.repository.SuperheroRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames={"superheros"})
public class SuperheroService implements BaseService<Superhero> {

  private final SuperheroRepository superheroRepository;

  public SuperheroService(SuperheroRepository superheroRepository) {
    this.superheroRepository = superheroRepository;
  }

  @Override
  public Collection<Superhero> findAll() {
    return superheroRepository.findAll();
  }

  @Override
  @Cacheable
  public Superhero findById(Long id) throws EntityNotFoundException {
    return superheroRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("Superhero with id: %s - Not Found", id)));
  }

  @Override
  @CachePut(key = "#id")
  public Superhero updateSuperhero(Long id, Superhero heroUpdate) {
    return superheroRepository.findById(id)
        .map(hero -> updateSuperhero(hero, heroUpdate))
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("Superhero with id: %s - Not Found", id)));
  }

  @Override
  @CacheEvict(key = "#id", allEntries = true)
  public void deleteById(Long id) {
    superheroRepository.deleteById(id);
  }

  public Collection<Superhero> searchByName(String name) {
    // Only to Demonstrate stream filtering, same result can be obtained with
    // superheroRepository.findByNameContainingIgnoreCase(name)
    // and Unit Test
    return superheroRepository.findAll().stream()
        .filter(hero -> Objects.nonNull(name) && hero.getName().toLowerCase().contains(name))
        .collect(Collectors.toList());
  }

  private Superhero updateSuperhero(Superhero hero, Superhero heroUpdate) {
    hero.setName(heroUpdate.getName());
    return superheroRepository.save(hero);
  }
}
