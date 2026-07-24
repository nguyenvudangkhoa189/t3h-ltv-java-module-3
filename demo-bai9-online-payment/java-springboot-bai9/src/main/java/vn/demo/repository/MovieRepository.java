package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.MovieModel;

public interface MovieRepository extends MongoRepository<MovieModel, String> {
}
