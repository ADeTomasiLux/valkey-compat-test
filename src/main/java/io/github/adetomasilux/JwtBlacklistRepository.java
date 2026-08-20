package io.github.adetomasilux;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtBlacklistRepository extends CrudRepository<JwtBlacklist, String> {
}