package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo.UserSetInfoId;

public interface UserSetInfoRepository extends JpaRepository<UserSetInfo,UserSetInfoId>{
    List<UserSetInfo> findByCardOwnerId(Long cardOwnerId);
    List<UserSetInfo> findByCardOwnerIdAndSetCode(Long cardOwnerId,String setCode);
    @Transactional
    void deleteByCardOwnerId(Long cardOwnerId);
}
