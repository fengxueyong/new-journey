package com.naixue.server.server;

import com.naixue.server.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chendong on 2019/9/3.
 */
public class UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int addUser(User userinfo){
        logger.debug("create user success, uid=" + userinfo.getUid());
        return 0;
    }
}
