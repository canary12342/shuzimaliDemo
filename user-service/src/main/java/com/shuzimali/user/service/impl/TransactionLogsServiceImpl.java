package com.shuzimali.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.user.entity.TransactionLogs;
import com.shuzimali.user.mapper.TransactionLogsMapper;
import com.shuzimali.user.service.TransactionLogsService;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【transaction_logs】的数据库操作Service实现
* @createDate 2025-06-20 20:55:55
*/
@Service
public class TransactionLogsServiceImpl extends ServiceImpl<TransactionLogsMapper, TransactionLogs>
    implements TransactionLogsService {

}




