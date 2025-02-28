package com.moviezon.moviebackend.service;

import com.moviezon.moviebackend.dto.MovieDto;
import com.moviezon.moviebackend.dto.MoviePageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public interface MovieService {

    MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws FileAlreadyExistsException, IOException;

    MovieDto getMovieById(Integer movieId);

    //Pagination
    List<MovieDto> getAllMovies();

    MovieDto updateMovieById(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException;

    String deleteMovieById(Integer movieId) throws IOException;

    MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize);

    MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                           String sortBy, String direction);

}
