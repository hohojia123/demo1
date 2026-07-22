package com.example.demo.service;


import com.example.demo.common.RequireRole;
import com.example.demo.service.impl.AiTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ToolFactor {

    private final List<MethodToolCallback> toolCallbacks = new ArrayList<>();
    private final Map<String, String> toolRoleMap;
    private final Map<MethodToolCallback, String> callbackRoleMap;

    private final AiTool aiTool;

    public ToolFactor(AiTool aiTool) {
        this.aiTool = aiTool;
        Class<?> clazz = aiTool.getClass();

        Map<String, String> roleMapBuilder = new HashMap<>();
        Map<MethodToolCallback, String> cbRoleMap = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            Tool toolAnn = method.getAnnotation(Tool.class);
            if (toolAnn == null) continue;

            String methodName = method.getName();

            // 读取 @RequireRole 注解构建角色映射
            RequireRole roleAnn = method.getAnnotation(RequireRole.class);
            String role = (roleAnn != null && roleAnn.value().length > 0)
                    ? roleAnn.value()[0] : "PUBLIC";
            roleMapBuilder.put(methodName, role);

            // 注册所有 @Tool 方法，运行时再按角色过滤
            ToolDefinition toolDefinition=ToolDefinitions.builder(method).build();
            MethodToolCallback callback = MethodToolCallback.builder()
                    .toolDefinition(toolDefinition)
                    .toolObject(aiTool)
                    .toolMethod(method)
                    .build();
            this.toolCallbacks.add(callback);
            cbRoleMap.put(callback, role);
        }

        this.toolRoleMap = Collections.unmodifiableMap(roleMapBuilder);
        this.callbackRoleMap = cbRoleMap;
    }

    public List<MethodToolCallback> getToolCallbacks() {
        return toolCallbacks;
    }

    public Map<String, String> getToolRoleMap() {
        return toolRoleMap;
    }

    public String getToolRole(String toolName) {
        return toolRoleMap.getOrDefault(toolName, "PUBLIC");
    }

    public List<ToolCallback> getToolForRoles(Set<String> roles) {
        boolean isadmin = roles.contains("ADMIN");
        boolean isuser = roles.contains("USER");
        return toolCallbacks.stream()
                .filter(callback -> {
                    String required = callbackRoleMap.get(callback);
                    if (required == null) return false;
                    return switch (required) {
                        case "PUBLIC" -> true;
                        case "admin" -> isadmin;
                        case "user" -> isuser || isadmin;
                        default -> false;
                    };
                }).collect(Collectors.toList());
    }
}
