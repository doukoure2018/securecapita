package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.CampaignsDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignsService {

    CampaignsDto create(CampaignsDto campaignsDto);

    List<CampaignsDto> getAllCampaigns();

    CampaignsDto getCampaign(Long id);


}
