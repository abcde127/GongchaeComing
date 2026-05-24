package com.gongchae.gongchae_coming.alio.repository;

import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicInstitutionRepository extends JpaRepository<PublicInstitution, String> {

	List<PublicInstitution> findAllByOrderByInstNmAsc();

	List<PublicInstitution> findByInstCdIn(Collection<String> instCds);
}
