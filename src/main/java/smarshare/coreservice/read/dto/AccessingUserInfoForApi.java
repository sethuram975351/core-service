package smarshare.coreservice.read.dto;

import lombok.Data;

public @Data
class AccessingUserInfoForApi {

    private String userName;
    private Boolean read;
    private Boolean write;
    private Boolean delete;

}
