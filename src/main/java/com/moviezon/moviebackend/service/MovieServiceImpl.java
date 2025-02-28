package com.moviezon.moviebackend.service;

import com.moviezon.moviebackend.dto.MovieDto;
import com.moviezon.moviebackend.dto.MoviePageResponse;
import com.moviezon.moviebackend.entities.Movie;
import com.moviezon.moviebackend.exceptions.FileExistsException;
import com.moviezon.moviebackend.exceptions.MovieNotFoundException;
import com.moviezon.moviebackend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class MovieServiceImpl implements MovieService {

    private final FileService fileService;

    private final MovieRepository movieRepository;

    @Value("${project.poster}")
    String path;

    @Value("${base.url}")
    String baseUrl;

    public MovieServiceImpl(FileService fileService, MovieRepository movieRepository) {
        this.fileService = fileService;
        this.movieRepository = movieRepository;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File Already Exists! Please give another file");
        }

        String uploadedFileName = fileService.uploadFile(path, file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie savedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/file/" + uploadedFileName;

//        MovieDto response = new MovieDto(
//            savedMovie.getMovieId(),
//            savedMovie.getTitle(),
//            savedMovie.getDirector(),
//            savedMovie.getStudio(),
//            savedMovie.getMovieCast(),
//            savedMovie.getReleaseYear(),
//            savedMovie.getPoster(),
//            posterUrl
//        );
        //        System.out.println("Poster URL: " + posterUrl);
        return convertToMovieDto(savedMovie, posterUrl);
    }

    @Override
    public MovieDto getMovieById(Integer movieId) {
        // 1 . verify in database, then fetch data of given id
        Movie movie = movieRepository.findById(movieId).
                orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id: " + movieId));

        //2. Generate Poster url
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        //3. Mapping movie to MovieDto object and return it
        return convertToMovieDto(movie, posterUrl);

    }

    @Override
    public List<MovieDto> getAllMovies() {
        //1. Fetch all the data(movies) from db in a list
        List<Movie> movieList = movieRepository.findAll();

        if(movieList.isEmpty()) {
            throw new MovieNotFoundException("Movies Not Found");
        }
        List<MovieDto> movieDtoList = new ArrayList<>();
        //2. Iterate through the list and generate poster url for each movie Obj
        // and map tp movie Dto object
        for(Movie movie : movieList) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
//            MovieDto reponse = new MovieDto(
//                    movie.getMovieId(),
//                    movie.getTitle(),
//                    movie.getDirector(),
//                    movie.getStudio(),
//                    movie.getMovieCast(),
//                    movie.getReleaseYear(),
//                    movie.getPoster(),
//                    posterUrl
//            );
            MovieDto response = convertToMovieDto(movie, posterUrl);
            movieDtoList.add(response);
        }
        return movieDtoList;
    }

    @Override
    public MovieDto updateMovieById(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        //1. check if movieId exists
        Movie existingMovie = movieRepository.findById(movieId).
                orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id: " + movieId));

        //2. if file from user is null, do nothing
        // Else delete the previous file asstd with movie obj and upload new file

        String fileName = existingMovie.getPoster();

        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }
        //3. set movieDto's poster with filename
        movieDto.setPoster(fileName);

        Movie movie = new Movie(
                existingMovie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //4. Save Updated movie to repository(DB)
        Movie updatedMovie = movieRepository.save(movie);

        //5. set poster url
        String posterUrl = baseUrl + "/file/" + updatedMovie.getPoster();

        //6. MovieDto Response
//        MovieDto response = new MovieDto(
//                updatedMovie.getMovieId(),
//                updatedMovie.getTitle(),
//                updatedMovie.getDirector(),
//                updatedMovie.getStudio(),
//                updatedMovie.getMovieCast(),
//                updatedMovie.getReleaseYear(),
//                updatedMovie.getPoster(),
//                posterUrl
//        );
        return convertToMovieDto(updatedMovie, posterUrl);
    }

    @Override
    public String deleteMovieById(Integer movieId) throws IOException {
        //1. Check if movie object exixts in Db
        Movie existingMovie = movieRepository.findById(movieId).
                orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id: " + movieId));

        //2. delete the file associate with this object

        String fileName = existingMovie.getPoster();
        Files.delete(Paths.get(path + File.separator + fileName));

        //3. Delete the Movie object
        movieRepository.deleteById(existingMovie.getMovieId());
        return "Movie Deleted Successfully";
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return getMoviePageResponse(pageNumber, pageSize, pageable);
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        System.out.println("Sorting by: " + sort);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        return getMoviePageResponse(pageNumber, pageSize, pageable);
    }

    private MoviePageResponse getMoviePageResponse(Integer pageNumber, Integer pageSize, Pageable pageable) {
        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtoList = new ArrayList<>();

        for(Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();

            MovieDto movieDto = convertToMovieDto(movie, posterUrl);
            movieDtoList.add(movieDto);
        }
        return new MoviePageResponse(movieDtoList, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }

    private MovieDto convertToMovieDto(Movie movie, String posterUrl){
        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }
}
