package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Campaigns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignsRepository extends JpaRepository<Campaigns,Long> {
}
