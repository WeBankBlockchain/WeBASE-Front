package com.webank.webase.front.gm.pressure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceHelloWorldCallback extends TransactionCallback {
  private static ObjectMapper objectMapper = new ObjectMapper();

  public  static Logger logger = LoggerFactory.getLogger(PerformanceHelloWorldCallback.class);

  private Long startTime = System.currentTimeMillis();

  private PerformanceCollector collector;

  public PerformanceCollector getCollector() { return collector; }

  public void setCollector(PerformanceCollector collector) {
    this.collector = collector;
  }

  PerformanceHelloWorldCallback() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                           false);
  }

  @Override
  public void onResponse(TransactionReceipt receipt) {
    Long cost = System.currentTimeMillis() - startTime;

    try {
      collector.onMessage(receipt, cost);
    } catch (Exception e) {
      logger.error("onMessage error: ", e);
    }
  }

}
