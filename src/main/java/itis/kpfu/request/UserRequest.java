package itis.kpfu.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class UserRequest {

    private final Long id;
    private final String email;
    private final String password;
    private final int x = 81;
    private final int y = 23;
    private final String video = "";
    private final String lan = "";
}
