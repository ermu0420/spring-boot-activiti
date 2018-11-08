package com.ermu.springbootactiviti.activiti.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

/**
 * @author：xusonglin ===============================
 * Created with IDEA.
 * Date：18-11-5
 * Time：上午10:07
 * ================================
 */
@Slf4j
@Controller
@RequestMapping("/model")
public class ActivitiModelController {


    @Autowired
    ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;

    /**
     * 新建一个空模型
     * 新建页面
     */
    @RequestMapping("/create")
    public void newModel(@RequestParam(name = "key") String key,
                         @RequestParam(name = "name",defaultValue = "new-process") String name,
                         @RequestParam(name = "description",defaultValue = "") String description,
                         @RequestParam(name = "revision",defaultValue = "1") int revision,
                         @RequestParam(name = "category") String category,
                         HttpServletResponse response) throws IOException {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //初始化一个空模型
        Model model = repositoryService.newModel();

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(MODEL_NAME, name);
        modelNode.put(MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setCategory(category);
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        response.sendRedirect("/modeler.html?modelId="+id);
    }

    /**
     * 获取所有模型
     */
    @RequestMapping("/modelList")
    @ResponseBody
    public Object modelList(){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        return repositoryService.createModelQuery().list();
    }

    /**
     * 发布模型为流程定义
     */
    @RequestMapping("/{modelId}/deploy")
    @ResponseBody
    public Object deploy(@PathVariable String modelId) throws Exception {

        //获取模型
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return "SUCCESS";
    }

    /**
     * 导出model的xml文件
     */
    @RequestMapping(value = "export/{modelId}")
    public void export(HttpServletRequest request, HttpServletResponse response, @PathVariable String modelId) {
        // 清空response
        response.reset();
        // 设置response的Header
        response.setContentType("application/octet-stream");
        String agent =  request.getHeader("USER-AGENT");
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);


            String filename = modelData.getName() + ".bpmn20.xml";
            // 火狐
            if(agent != null &&  agent.toLowerCase().indexOf("firefox") > 0) {
                String enableFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(filename.getBytes("UTF-8")))) + "?=";
                response.setHeader("Content-Disposition", "attachment; filename=" + enableFileName);
            } else {
                String enableFileName = new String(filename.getBytes("GBK"), "ISO-8859-1");
                response.setHeader("Content-Disposition", "attachment; filename=" + enableFileName);
            }

            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("导出model的xml文件失败：modelId={}", modelId, e);
        }
    }


}