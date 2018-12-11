package com.ermu.springbootactiviti.activiti.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Description: [Activiti工作流公共方法Controller，提供工作流相关公共方法]
 * @Author: [Double]
 * @CreateDate: [2015-10-22]
 * @Version: [v2.0.0]
 */
@RestController
@RequestMapping("/activitiController")
public class ActivitiController{

	static final Logger logger = Logger.getLogger(ActivitiController.class);

	@Resource
	private RepositoryService repositoryService;

	@Resource
	private HistoryService historyService;



}
