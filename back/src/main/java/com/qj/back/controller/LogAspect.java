package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qj.back.annotation.Log;
import com.qj.back.entity.Consultants;
import com.qj.back.entity.OperationLog;
import com.qj.back.mapper.OperationLogMapper;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Aspect
@Component
@Slf4j
@SuppressWarnings("all")
public class LogAspect {
    @Autowired
    private OperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.qj.back.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 执行方法
        Object result = point.proceed();

        // 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;

        // 保存日志
        saveLog(point, time);

        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog operationLog = new OperationLog();

            // 获取请求参数
            Object[] args = joinPoint.getArgs();
            Consultants consultants = null;
            String operationType = null;

            // 解析参数，跳过 HttpServletRequest
            for (Object arg : args) {
                if (arg instanceof Consultants) {
                    consultants = (Consultants) arg;
                } else if (arg instanceof String) {
                    operationType = (String) arg;
                }
            }

            // 生成操作描述
            String operation;
            if (consultants != null && operationType != null) {
                switch(operationType) {
                    // 普通客资操作
                    case "addCountNormal":
                        operation = String.format("【%s】顾问[%s]: 分配普通客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountNormal(),
                                consultants.getCountNormal() + 1);
                        break;
                    case "skipNormal":
                        operation = String.format("【%s】顾问[%s]: 跳过普通客资 (当前客资数: %d)",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountNormal());
                        break;
                    case "confirmNormal":
                        operation = String.format("【%s】顾问[%s]: 确认普通客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountNormal() - 1,
                                consultants.getCountNormal());
                        break;

                    // SEM客资操作111
                    case "addCountSem":
                        operation = String.format("【%s】顾问[%s]: 分配SEM客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSem(),
                                consultants.getCountSem() + 1);
                        break;
                    case "skipSem":
                        operation = String.format("【%s】顾问[%s]: 跳过SEM客资 (当前客资数: %d)",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSem());
                        break;
                    case "confirmSem":
                        operation = String.format("【%s】顾问[%s]: 确认SEM客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSem() - 1,
                                consultants.getCountSem());
                        break;

                    // AP/Alevel客资操作
                    case "addCountSingle1":
                        operation = String.format("【%s】顾问[%s]: 分配AP/Alevel客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSingle1(),
                                consultants.getCountSingle1() + 1);
                        break;
                    case "skipSingle1":
                        operation = String.format("【%s】顾问[%s]: 跳过AP/Alevel客资 (当前客资数: %d)",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSingle1());
                        break;
                    case "confirmSingle1":
                        operation = String.format("【%s】顾问[%s]: 确认AP/Alevel客资 %d→%d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSingle1() - 1,
                                consultants.getCountSingle1());
                        break;
                    case "restoreSingle1":
                        operation = String.format("【%s】顾问[%s]: 恢复AP/Alevel客资分配状态",
                                getTypeDesc(consultants.getType()),
                                consultants.getName());
                        break;

                    // 抖音客资操作
                    case "addCountDy":
                        operation = String.format("【抖音/B站国际课程】顾问[%s]: 分配抖音客资 %d→%d",
                                consultants.getName(),
                                consultants.getCountSingle1(),
                                consultants.getCountSingle1() + 1);
                        break;
                    case "skipDy":
                        operation = String.format("【抖音/B站国际课程】顾问[%s]: 跳过抖音客资 (当前客资数: %d)",
                                consultants.getName(),
                                consultants.getCountSingle1());
                        break;
                    case "confirmDy":
                        operation = String.format("【抖音/B站国际课程】顾问[%s]: 确认抖音客资 %d→%d",
                                consultants.getName(),
                                consultants.getCountSingle1() - 1,
                                consultants.getCountSingle1());
                        break;
                    case "restoreDy":
                        operation = String.format("【抖音/B站国际课程】顾问[%s]: 恢复抖音客资分配状态",
                                consultants.getName());
                        break;

                    // B站申诉客资操作
                    case "addCountIC":
                        if (consultants.getType() == 1) {
                            operation = String.format("【辅导】顾问[%s]: 分配AP/Alevel客资 %d→%d",
                                    consultants.getName(),
                                    consultants.getCountSingle1(),
                                    consultants.getCountSingle1() + 1);
                        } else {
                            operation = String.format("【B站申诉】顾问[%s]: 分配B站申诉客资 %d→%d",
                                    consultants.getName(),
                                    consultants.getCountSingle1(),
                                    consultants.getCountSingle1() + 1);
                        }
                        break;
                    case "skipIC":
                        if (consultants.getType() == 1) {
                            operation = String.format("【辅导】顾问[%s]: 跳过AP/Alevel客资 (当前客资数: %d)",
                                    consultants.getName(),
                                    consultants.getCountSingle1());
                        } else {
                            operation = String.format("【B站申诉】顾问[%s]: 跳过B站申诉客资 (当前客资数: %d)",
                                    consultants.getName(),
                                    consultants.getCountSingle1());
                        }
                        break;
                    case "confirmIC":
                        if (consultants.getType() == 1) {
                            operation = String.format("【辅导】顾问[%s]: 确认AP/Alevel客资 %d→%d",
                                    consultants.getName(),
                                    consultants.getCountSingle1() - 1,
                                    consultants.getCountSingle1());
                        } else {
                            operation = String.format("【B站申诉】顾问[%s]: 确认B站申诉客资 %d→%d",
                                    consultants.getName(),
                                    consultants.getCountSingle1() - 1,
                                    consultants.getCountSingle1());
                        }
                        break;
                    case "restoreIC":
                        if (consultants.getType() == 1) {
                            operation = String.format("【辅导】顾问[%s]: 恢复AP/Alevel客资分配状态",
                                    consultants.getName());
                        } else {
                            operation = String.format("【B站申诉】顾问[%s]: 恢复B站申诉客资分配状态",
                                    consultants.getName());
                        }
                        break;

                    // 恢复客资操作
                    case "restoreNormal":
                        operation = String.format("【%s】顾问[%s]: 恢复普通客资分配状态",
                                getTypeDesc(consultants.getType()),
                                consultants.getName());
                        break;
                    case "restoreSem":
                        operation = String.format("【%s】顾问[%s]: 恢复SEM客资分配状态",
                                getTypeDesc(consultants.getType()),
                                consultants.getName());
                        break;

                    case "updateNormalCount":
                        operation = String.format("【%s】在总表修改顾问[%s]的普通客资为: %d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountNormal());
                        break;
                    case "updateSemCount":
                        operation = String.format("【%s】在总表修改顾问[%s]的SEM客资为: %d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSem());
                        break;
                    case "updateSingle1Count":
                        operation = String.format("【%s】在总表修改顾问[%s]的AP/Alevel客资为: %d",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                consultants.getCountSingle1());
                        break;
                    case "updateDyCount":
                        operation = String.format("【抖音/B站国际课程】在总表修改顾问[%s]的抖音客资为: %d",
                                consultants.getName(),
                                consultants.getCountSingle1());
                        break;
                    case "updateICCount":
                        if (consultants.getType() == 1) {
                            operation = String.format("【辅导】在总表修改顾问[%s]的AP/Alevel客资为: %d",
                                    consultants.getName(),
                                    consultants.getCountSingle1());
                        } else {
                            operation = String.format("【B站申诉】在总表修改顾问[%s]的B站申诉客资为: %d",
                                    consultants.getName(),
                                    consultants.getCountSingle1());
                        }
                        break;
                    case "updateStatus":
                        operation = String.format("【%s】在总表修改顾问[%s]状态为: %s",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                getStatusDesc(consultants.getStatus()));
                        break;
                    case "updateComplement":
                        operation = String.format("【%s】补客资分配给顾问[%s]",
                                getTypeDesc(consultants.getType()),
                                consultants.getName());
                        break;
                    default:
                        operation = String.format("【%s】顾问[%s]: %s",
                                getTypeDesc(consultants.getType()),
                                consultants.getName(),
                                operationType);
                }
            } else {
                Log logAnnotation = method.getAnnotation(Log.class);
                operation = logAnnotation.value();
            }

            operationLog.setOperation(operation);
            operationLog.setModule(method.getAnnotation(Log.class).module());

            // 获取当前登录用户
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    String account = JwtUtil.getAccountFromToken(token);
                    operationLog.setAccount(account);
                }
                operationLog.setIp(getIpAddr(request));
            }

            operationLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + method.getName());

            // 只序列化业务相关的参数
            List<Object> paramsList = new ArrayList<>();
            if (consultants != null) {
                paramsList.add(consultants);
            }
            if (operationType != null) {
                paramsList.add(operationType);
            }
            operationLog.setParams(new ObjectMapper().writeValueAsString(paramsList));
            operationLog.setCreateTime(new Date());

            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败:", e);
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取顾问类型描述
     * @param type 类型编码
     * @return 类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) return "未知类型";
        
        switch (type) {
            case 1: return "辅导";
            case 2: return "申诉";
            case 3: return "抖音/B站国际课程";
            case 4: return "B站申诉";
            default: return "未知类型(" + type + ")";
        }
    }
    
    /**
     * 获取顾问状态描述
     * @param status 状态编码
     * @return 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知状态";
        
        switch (status) {
            case 0: return "未分配";
            case 1: return "开始接单";
            case 2: return "休息";
            case 3: return "暂停";
            default: return "未知状态(" + status + ")";
        }
    }
}
