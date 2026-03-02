javapackage com.revshop.repository;

import com.revshop.entity.User;
import com.revshop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(User user);
    boolean existsByUser(User user);
}