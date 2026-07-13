package vn.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import vn.demo.model.MovieModel;

/**
 * REPOSITORY — truy vấn collection {@code mymoviedb}.
 */
public interface MovieRepository extends MongoRepository<MovieModel, String> {

	List<MovieModel> findByTypeIgnoreCase(String type, Pageable pageable);

	@Query("{ 'listed_in': { $regex: ?0, $options: 'i' } }")
	List<MovieModel> findByListedInRegex(String regex, Pageable pageable);

}
