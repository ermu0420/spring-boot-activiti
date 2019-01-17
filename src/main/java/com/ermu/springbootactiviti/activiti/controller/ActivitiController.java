package com.ermu.springbootactiviti.activiti.controller;


import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
