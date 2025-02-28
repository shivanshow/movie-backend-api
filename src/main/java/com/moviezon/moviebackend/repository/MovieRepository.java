package com.moviezon.moviebackend.repository;

import com.moviezon.moviebackend.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

}
