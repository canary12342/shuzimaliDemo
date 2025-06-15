package com.shuzimali.logging.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuzimali.logging.domain.OperationLogs;
import com.shuzimali.logging.service.OperationLogsService;
import com.shuzimali.logging.mapper.OperationLogsMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【operation_logs】的数据库操作Service实现
* @createDate 2025-06-15 23:25:15
*/
@Service
public class OperationLogsServiceImpl extends ServiceImpl<OperationLogsMapper, OperationLogs>
    implements OperationLogsService{

    @Override
    public boolean existsByMessageId(Long messageId) {
        OperationLogs operationLog = getById(messageId);
        return operationLog != null;
    }
}




