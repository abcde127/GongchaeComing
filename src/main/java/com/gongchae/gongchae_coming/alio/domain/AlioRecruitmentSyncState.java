package com.gongchae.gongchae_coming.alio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "alio_recruitment_sync_states")
public class AlioRecruitmentSyncState {

	public static final String GLOBAL_ID = "GLOBAL";

	@Id
	@Column(length = 50)
	private String id;

	private LocalDateTime lastSucceededAt;

	protected AlioRecruitmentSyncState() {
	}

	private AlioRecruitmentSyncState(String id) {
		this.id = id;
	}

	public static AlioRecruitmentSyncState global(LocalDateTime lastSucceededAt) {
		AlioRecruitmentSyncState state = new AlioRecruitmentSyncState(GLOBAL_ID);
		state.lastSucceededAt = lastSucceededAt;
		return state;
	}

	public LocalDateTime getLastSucceededAt() {
		return lastSucceededAt;
	}
}
