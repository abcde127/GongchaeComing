package com.gongchae.gongchae_coming.notification.repository;

import com.gongchae.gongchae_coming.notification.domain.NotificationHistory;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

	List<NotificationHistory> findByMemberIdOrderBySentAtDescIdDesc(Long memberId, Pageable pageable);
}
