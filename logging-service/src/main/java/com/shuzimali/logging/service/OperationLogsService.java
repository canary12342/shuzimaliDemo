package com.shuzimali.logging.service;

import com.shuzimali.logging.domain.OperationLogs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 1
* @description 针对表【operation_logs】的数据库操作Service
* @createDate 2025-06-15 23:25:15
*/
public interface OperationLogsService extends IService<OperationLogs> {

    boolean existsByMessageId(Long messageId);
}
