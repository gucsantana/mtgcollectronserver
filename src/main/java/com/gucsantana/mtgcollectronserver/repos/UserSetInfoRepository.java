package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo.UserSetInfoId;

public interface UserSetInfoRepository extends JpaRepository<UserSetInfo,UserSetInfoId>{
    List<UserSetInfo> findByCardOwnerId(Long cardOwnerId);
}
