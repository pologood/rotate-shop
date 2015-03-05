package com.dianping.rotate.shop.service.impl;

import com.dianping.rotate.shop.dao.TaskDAO;
import com.dianping.rotate.shop.dto.TaskDTO;
import com.dianping.rotate.shop.json.TaskEntity;
import com.dianping.rotate.shop.service.TaskService;
import com.dianping.rotate.shop.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luoming on 15/2/4.
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskDAO taskDAO;

    @Override
    public TaskDTO getTask(String taskName) {
        TaskEntity taskEntity = null;
        String tableName = getTableNameByTaskName(taskName);
        if(tableName != null) {
            taskEntity = taskDAO.queryTask(tableName);
        }
        return trans(taskEntity);
    }

    private String getTableNameByTaskName(String taskName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("messageQueueHistoryDataTask", "MessageQueueHistory");
        map.put("apolloShopRatingDataProcessorTask", "ApolloShopExtend");
        map.put("apolloShopBusinessDataProcessorTask", "ApolloShopBusinessStatus");
        map.put("syncShopExtendTypeTask", "ApolloShopExtend");
        map.put("syncRotateGroupTypeTask", "RotateGroup");
        return map.get(taskName);
    }

    private TaskDTO trans(TaskEntity taskEntity) {
        TaskDTO taskDTO = new TaskDTO();
        if(taskEntity != null) {
            CommonUtil commonUtil = new CommonUtil();
            taskDTO.setStartTime(commonUtil.datetimeToString(taskEntity.getStartTime()));
            taskDTO.setEndTime(commonUtil.datetimeToString(taskEntity.getEndTime()));
            taskDTO.setRunTime(transTime(taskEntity.getRunTime()));
            taskDTO.setTotalCount(taskEntity.getTotalCount());
        }
        return taskDTO;
    }

    private String transTime(Long time) {
        String times = "";
        if(time != null) {
            times = time / 3600 + " H " + time % 3600 / 60 + " m " + time % 3600 % 60 + " s";
        }
        return times;
    }

}
