package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.SmsMessagesDto;
import org.springframework.web.multipart.MultipartFile;

public interface SmsMessagesService {

    SmsMessagesDto create(SmsMessagesDto smsMessagesDto);

    void importCampaignFiles(MultipartFile importFile,Long campaign_id,Long id_user);


}
