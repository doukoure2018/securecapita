package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.dto.ImportFileDto;
import io.getarrayus.securecapita.dto.SmsMessagesDto;
import io.getarrayus.securecapita.entity.Campaigns;
import io.getarrayus.securecapita.entity.SmsMessages;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.enumeration.MessageStatus;
import io.getarrayus.securecapita.exception.ApiException;
import io.getarrayus.securecapita.repository.CampaignsRepository;
import io.getarrayus.securecapita.repository.SmsMessagesRepository;
import io.getarrayus.securecapita.repository.UserRepository;
import io.getarrayus.securecapita.service.EmailService;
import io.getarrayus.securecapita.service.SmsMessagesService;
import io.getarrayus.securecapita.service.UserService;
import io.getarrayus.securecapita.utils.ExcelUtils;
import io.getarrayus.securecapita.utils.FileFactory;
import io.getarrayus.securecapita.utils.ImportConfig;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsMessagesServiceImpl implements SmsMessagesService {

    private final SmsMessagesRepository smsMessagesRepository;
    private final ModelMapper mapper;
    private final UserRepository userRepository;
    private final CampaignsRepository campaignsRepository;
    private final EmailService emailService;
    @Override
    public SmsMessagesDto create(SmsMessagesDto smsMessagesDto) {
        SmsMessages smsMessages =mapper.map(smsMessagesDto,SmsMessages.class);
        // get Campaign
        Campaigns campaigns = campaignsRepository.getReferenceById(smsMessagesDto.getCampaign_id());
        // get User
        Users users = userRepository.getReferenceById(smsMessagesDto.getId_user());
        smsMessages.setCampaigns(campaigns);
        smsMessages.setUsers(users);
        smsMessages.setSentAt(LocalDateTime.now());
        smsMessages.setStatus(MessageStatus.SENT);
        SmsMessages newSmsMessage = smsMessagesRepository.save(smsMessages);
        emailService.sendSMS(smsMessagesDto.getRecipientNumber(), smsMessagesDto.getMessage());
        return mapper.map(newSmsMessage,SmsMessagesDto.class);
    }

    @Transactional
    @Override
    public void importCampaignFiles(MultipartFile importFile, Long campaignId, Long userId) {
        Campaigns campaign = campaignsRepository.findById(campaignId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + campaignId));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<ImportFileDto> importFileDtoList = parseExcelFile(importFile);

        List<SmsMessages> messages = importFileDtoList.stream()
                .map(dto -> createSmsMessage(dto, campaign, user))
                .collect(Collectors.toList());

        sendSmsMessages(messages);
    }

    private List<ImportFileDto> parseExcelFile(MultipartFile importFile) {
        try (Workbook workbook = FileFactory.getWorkbookStream(importFile)) {
            return ExcelUtils.getImportData(workbook, ImportConfig.customerImport);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
    }

    private SmsMessages createSmsMessage(ImportFileDto dto, Campaigns campaign, Users user) {
        SmsMessages message = new SmsMessages();
        message.setCampaigns(campaign);
        message.setUsers(user);
        message.setSentAt(LocalDateTime.now());
        message.setStatus(MessageStatus.PENDING);
        message.setRecipientNumber(dto.getContact());
        message.setMessage(dto.getMessage());
        return message;
    }

    private void sendSmsMessages(List<SmsMessages> messages) {
        messages.parallelStream().forEach(this::sendAndSaveSmsMessage);
    }

    private void sendAndSaveSmsMessage(SmsMessages message) {
        try {
            boolean sent = isNumeric(message.getRecipientNumber());
            if(sent){
                 message.setStatus(MessageStatus.SENT);
                 emailService.sendSMS(message.getRecipientNumber(), message.getMessage());
            }else{
                message.setStatus(MessageStatus.FAILED);
            }
        } catch (Exception e) {
            message.setStatus(MessageStatus.FAILED);
            log.error("Error sending SMS: ", e);
        } finally {
            smsMessagesRepository.save(message);
        }
    }

    private static boolean isNumeric(String input) {
        // Regular expression to match only digits (0-9)
        return input != null && input.matches("\\d{9}");
    }

}
