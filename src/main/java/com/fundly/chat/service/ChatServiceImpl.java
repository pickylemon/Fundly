package com.fundly.chat.service;

import com.fundly.chat.repository.ChatRepository;
import com.persistence.dto.ChatRequest;
import com.persistence.dto.SelBuyMsgDto;
import com.persistence.dto.FileDto;
import com.persistence.dto.SelBuyMsgDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    ChatRepository chatRepository;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public SelBuyMsgDto joinChatRoom(ChatRequest request) {
        SelBuyMsgDto selBuyMsgDto;
        try {
//            기존의 채팅방이 존재하면 그대로 전달, 없으면 만들어서 전달한다.
            return (selBuyMsgDto = chatRepository.getChatRoom(request)) != null ? selBuyMsgDto : chatRepository.makeChatRoom(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean saveMessage(SelBuyMsgDetailsDto message) {
        try {
            message.setSvr_intime_string(getHoursAndMinutes());

            chatRepository.saveMessage(message);
        } catch (Exception e) {
            log.error("error with save Message");
            throw new RuntimeException("error with saveMessage(SelBuyMsgDetailsDto message)", e);
        }
        return true;
    }

    @Override
    @Transactional
    public void saveFileMessage(FileDto savedFile, SelBuyMsgDetailsDto message) {
        String fileSavedUrl = savedFile.getFile_saved_url();
//            파일 저장과 동시에 채팅창에 보여지기 위해 이미지 url을 넣어준다.
//            메시지에 파일 경로를 저장
        message.setFile_url(fileSavedUrl);
        message.setSvr_intime_string(getHoursAndMinutes());
        savedFile.setTable_name(SEL_BUY_MSG_DETAILS);
//            파일 Dto에 해당 메시지의 식별자를 적어서 저장한다.
//            파일 정보를 db에 저장한다.
        try {
            chatRepository.saveMessage(message);
//            메시지가 db에 저장 될 때 마이바티스가 seq를 생성한다.
//            savedFile.setTable_key(String.valueOf(message.getSeq()));
            savedFile.setTable_key(String.valueOf(message.getMsg_id()));
            chatRepository.saveImageFile(savedFile);
        } catch (Exception e) {
            log.error("error ChatServiceImpl.saveImageFile()");
            throw new RuntimeException(e);
        }
    }

    private String getHoursAndMinutes() {
        String hoursAndMinutes = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return hoursAndMinutes;
    }

    @ExceptionHandler(Exception.class)
    public String handleException() {
        return "chat/error";
    }
}
