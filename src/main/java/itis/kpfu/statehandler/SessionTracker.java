package itis.kpfu.statehandler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SessionTracker {
    String email;
    String password;
    ChatState state = ChatState.AWAITING_EMAIL;
}
