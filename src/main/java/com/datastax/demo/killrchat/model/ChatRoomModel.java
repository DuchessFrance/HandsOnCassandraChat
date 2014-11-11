package com.datastax.demo.killrchat.model;

import com.datastax.demo.killrchat.entity.ChatTheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomModel {

    private UUID roomId;

    private String roomName;

    private ChatTheme theme;

    private List<UserModel> participants;


    public boolean isPrivate() {
        return theme == ChatTheme.PRIVATE;
    }
}
