package com.ermu.springbootactiviti.activiti.controller;

import com.ermu.springbootactiviti.activiti.service.ProcessCoreService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author：xusonglin ===============================
 * Created with IDEA.
 * Date：18-11-7
 * Time：下午2:34
 * ================================
 */
@RestController
@RequestMapping(value = "/flow")
@Slf4j
@Api(value="流程控制")
public class FlowController {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessCoreService processCoreService;
    @Autowired
    private IdentityService identityService;


    @PostMapping(value = "/start")
    public Map<String,Object> startProcessByKey(@RequestParam(name = "key") String key) throws Exception{
        Map<String,Object> map =new HashMap<>();
        //1.启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
        String procInstId = processInstance.getId();
        TaskEntity taskEntity = this.findRunTaskByInstId(procInstId);
        //获取下一步权限信息
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskEntity.getId());


        this.completeTaskNode(processInstance.getId(),taskEntity.getId(),1, "同意",1L,"用户1");
        TaskEntity nextTask = this.findRunTaskByInstId(procInstId);
        String assignee = nextTask.getAssignee();


        log.info("processInstance:"+processInstance.toString());
        log.info("lastTask:"+taskEntity.toString());
        log.info("nextTask:"+nextTask.toString());
        log.info("assignee:"+assignee);
        return map;
    }

    /**
     * 获取实例正在运行的任务
     * @param instId
     * @return
     * @throws Exception
     */
    private TaskEntity findRunTaskByInstId(String instId) throws Exception{
        TaskEntity taskEntity =  (TaskEntity) taskService.createTaskQuery()
                .processInstanceId(instId)
                .active()
                .singleResult();
        if (taskEntity == null){
            throw new Exception("任务实例未找到!");
        }
        return taskEntity;
    }
    /**
     * 完成任务节点
     * @param procInstanceId
     * @param taskId
     * @param status
     * @param comment
     */
    @PostMapping(value = "/complete")
    public void completeTaskNode(@RequestParam String procInstanceId,
                                 @RequestParam String taskId,
                                 @RequestParam Integer status,
                                 @RequestParam String comment,
                                 @RequestParam Long userId,
                                 @RequestParam String userName) {
        //判断该任务是否有条件:  start
        List<PvmTransition> outgoingTransitions = processCoreService.getOutgoingTransitions(procInstanceId);
        if (outgoingTransitions != null && !outgoingTransitions.isEmpty()) {
            Map<String, Object> variables = new HashMap<>();
            //智能做法：根据上面匹配出参数key
            variables.put("apply_status", status);
            //任务完成，设置本签收人员
            //设置审核人
            Authentication.setAuthenticatedUserId("user_key_" + userName + "@id_" + userId + "");
            taskService.addComment(taskId, procInstanceId, comment);
            taskService.complete(taskId, variables);
        } else {
            //表示没有参数设置
            //任务完成，设置本签收人员
            //设置审核人
            Authentication.setAuthenticatedUserId("user_key_" + userName + "@id_" + userId + "");
            taskService.addComment(taskId, procInstanceId, comment);
            taskService.complete(taskId);
        }
    }

}
