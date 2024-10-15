package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.dto.CampaignsDto;
import io.getarrayus.securecapita.dto.ImportFileDto;
import io.getarrayus.securecapita.entity.Campaigns;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.enumeration.CampaignStatus;
import io.getarrayus.securecapita.repository.CampaignsRepository;
import io.getarrayus.securecapita.repository.UserRepository;
import io.getarrayus.securecapita.service.CampaignsService;
import io.getarrayus.securecapita.service.EmailService;
import io.getarrayus.securecapita.utils.ExcelUtils;
import io.getarrayus.securecapita.utils.FileFactory;
import io.getarrayus.securecapita.utils.ImportConfig;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignsServiceImpl implements CampaignsService {

    private final CampaignsRepository campaignsRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    @Override
    public CampaignsDto create(CampaignsDto campaignsDto) {
        Campaigns campaigns = mapper.map(campaignsDto,Campaigns.class);
        campaigns.setCreatedAt(LocalDateTime.now());
        campaigns.setStatus(CampaignStatus.ACTIVE);
        Users users = userRepository.getReferenceById(campaignsDto.getId_user());
        campaigns.setUsers(users);
        Campaigns newCampaign = campaignsRepository.save(campaigns);
        return  mapper.map(newCampaign,CampaignsDto.class);
    }

    @Override
    public List<CampaignsDto> getAllCampaigns() {
        return null;
    }

    @Override
    public CampaignsDto getCampaign(Long id) {
        return mapper.map(campaignsRepository.getReferenceById(id),CampaignsDto.class);
    }


}
