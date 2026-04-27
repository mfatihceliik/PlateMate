package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.ChatRoomDto;

import java.util.List;

public interface IChatRoomService {
    DataResult<ChatRoomDto> getOrCreateChatRoom(Long userOneId, Long userTwoId);
    DataResult<List<ChatRoomDto>> getUserRooms(Long userId); // Kullanıcının tüm sohbet odaları
}
