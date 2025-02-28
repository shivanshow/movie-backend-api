package com.moviezon.moviebackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviezon.moviebackend.dto.MovieDto;
import com.moviezon.moviebackend.dto.MoviePageResponse;
import com.moviezon.moviebackend.entities.Movie;
import com.moviezon.moviebackend.exceptions.FileMissingException;
import com.moviezon.moviebackend.service.MovieService;
import com.moviezon.moviebackend.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file,
                                                    @RequestPart String movieDto) throws IOException {
        if(file.isEmpty()) {
            throw new FileMissingException("File is required but was not provided.");
        }
        MovieDto obj = convertToMovieDto(movieDto);
        return new ResponseEntity<>(movieService.addMovie(obj, file), HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovieById(movieId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId,
                                                       @RequestPart MultipartFile file, @RequestPart String movieDto) throws IOException {
        if(file.isEmpty()) file = null;
        MovieDto obj = convertToMovieDto(movieDto);
        return new ResponseEntity<>(movieService.updateMovieById(movieId, obj, file), HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovieById(movieId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<MoviePageResponse> getAllMoviesPageHandler(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPagination(pageNumber, pageSize));
    }

    @GetMapping("/paginated-sorted")
    public ResponseEntity<MoviePageResponse> getAllMoviesPageHandler(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIRECTION, required = false) String direction
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPaginationAndSorting(pageNumber, pageSize, sortBy, direction));
    }

    private MovieDto convertToMovieDto(String movieObj) {
        MovieDto movieDto = new MovieDto();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            movieDto = objectMapper.readValue(movieObj, MovieDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return movieDto;
    }
}
