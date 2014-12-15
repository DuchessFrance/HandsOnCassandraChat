package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.resource.model.MessagePaging;
import com.datastax.demo.killrchat.resource.model.MessagePosting;
import com.datastax.demo.killrchat.service.MessageService;
import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MessageResourceTest {

    @InjectMocks
    private MessageResource resource;

    @Mock
    private MessageService service;

    private LightUserModel jdoe = new LightUserModel("jdoe","John","DOE");

    @Test
    public void should_post_new_message() throws Exception {
        //When
        final LightUserModel author = new LightUserModel("jdoe", "John", "DOE");
        resource.postNewMessage("games", new MessagePosting(author, "Wow sucks!"));

        //Then
        verify(service).postNewMessage(author, "games", "Wow sucks!");
    }

    @Test
    public void should_fetch_first_messages_from_room() throws Exception {
        //Given
        UUID messageId = UUIDs.timeBased();
        final Date creationDate = new Date(UUIDs.unixTimestamp(messageId));
        final ChatMessageModel message = new ChatMessageModel(messageId, creationDate, jdoe, "bla bla", false);
        when(service.fetchNextMessagesForRoom(eq("games"), any(UUID.class),eq(MessageResource.DEFAULT_MESSAGES_FETCH_SIZE)))
                .thenReturn(Arrays.asList(message));

        //When
        final List<ChatMessageModel> messages = resource.fetchNextMessagesForRoom("games", null, 0);

        //Then
        assertThat(messages).containsExactly(message);
    }

    @Test
    public void should_fetch_messages_from_lower_bound() throws Exception {
        //Given
        UUID messageId = UUIDs.timeBased();
        UUID now = UUIDs.timeBased();
        final Date creationDate = new Date(UUIDs.unixTimestamp(messageId));
        final ChatMessageModel message = new ChatMessageModel(messageId, creationDate, jdoe, "bla bla", false);
        when(service.fetchNextMessagesForRoom("games", now,11)).thenReturn(Arrays.asList(message));

        //When
        final List<ChatMessageModel> messages = resource.fetchNextMessagesForRoom("games", now, 11);

        //Then
        assertThat(messages).containsExactly(message);
    }
}