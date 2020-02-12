package com.webank.webase.front.transaction.websocket.Server;

import com.alibaba.fastjson.JSON;

import com.webank.webase.front.transaction.TransService;
import com.webank.webase.front.transaction.entity.ReqTransHandle;
import com.webank.webase.front.transaction.entity.ReqTransHandleWithSign;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.core.Response;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/websocket/{frontId}")
@Component
@Slf4j
public class WebSocketServer {

    public static Map<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

    private static ApplicationContext applicationContext;
    //你要注入的service或者dao
    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketServer.applicationContext = applicationContext;
    }


     private TransService transService = null ;
    /**
     * onOpen是当用户发起连接时，会生成一个用户的Session 注意此Session是 javax.websocket.Session;
     * 然后我们用userId作为Key Session作为Vaule存入Map中暂存起来
     *
     * @param frontId
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("frontId") String frontId, Session session) {
        log.info("====>WebSocketService onOpen: " + frontId);
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<String, Session>();
        }
        sessionMap.put(frontId, session);
    }

    /**
     * onClose 是用户关闭聊天窗时，将用户session移除
     *
     * @param frontId
     */
    @OnClose
    public void onClose(@PathParam("frontId") String frontId) {
        log.info("====>WebSocketService OnClose: " + frontId);
        sessionMap.remove(frontId);
    }

    /**
     * onMessage 实现聊天功能， message是前端传来的JSON字符串。其中含有MessageVo里的信息。根据vo实现点对点/广播聊天。
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) throws BaseException {
        log.info("====>WebSocketService OnMessage: " + message);
        MessageRequest messageRequest = JSON.parseObject(message, MessageRequest.class);

            one2one(messageRequest);
        }


    /**
     * 当出现异常时候自动调用该方法。
     *
     * @param t
     */
    @OnError
    public void error(Throwable t) {
        t.printStackTrace();
    }

    /**
     * 点对点
     *  session.getAsyncRemote().sendText(message); 即向目标session发送消息。
     *
     */
    private  void one2one(MessageRequest vo) throws BaseException {
        Session consumerSession = sessionMap.get(vo.getFrontId());
        if (consumerSession == null) {
            log.info("消息消费者不存在");
        } else {
           if(transService == null) {
               transService = applicationContext.getBean(TransService.class);
           }

           MessageResponse messageResponse = methodHandle(vo);
           consumerSession.getAsyncRemote().sendText(JSON.toJSONString(messageResponse));
        }
    }


    private   MessageResponse methodHandle(MessageRequest vo)  {

        MessageResponse response = new MessageResponse();
        response.setMessageId(vo.getMessageId());

        Object result;
             try {

                 if (vo.getMethod().equals("trans")) {
                     ReqTransHandle transReq = JSON.parseObject(vo.getRequestJson(), ReqTransHandle.class);

                     result = transService.transHandle(transReq);
                     response.setRawResponse(JSON.toJSONString(result));
                 } else if (vo.getMethod().equals("transWithSign")) {
                     ReqTransHandleWithSign transWithSignReq = JSON.parseObject(vo.getRequestJson(), ReqTransHandleWithSign.class);
                     result = transService.transHandleWithSign(transWithSignReq);
                     response.setRawResponse(JSON.toJSONString(result));
                 } else {
                     Response.Error error = new Response.Error(200001, "method not support");
                     response.setError(error);
                 }
             }catch (Exception e) {
                e.printStackTrace();
               response.setError(new Response.Error(210002,e.getMessage()));
        }
         return response;

    }


}
