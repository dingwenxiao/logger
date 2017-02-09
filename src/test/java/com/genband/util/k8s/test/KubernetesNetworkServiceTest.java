package com.genband.util.k8s.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.genband.util.log.KubernetesNetworkService;
import com.genband.util.log.config.ConfigManager;
import com.genband.util.log.config.KafkaConfigManager;

public class KubernetesNetworkServiceTest {

  private final static String kubernetesMasterUrl = "172.28.250.4:8080";
  private static KubernetesNetworkService kubernetesNetworkService = null;

  // @BeforeClass
  public static void testBefore() {
    ConfigManager configManager = KafkaConfigManager.getInstance();
    new KubernetesNetworkService.SingletonBuilder(configManager).build();
    kubernetesNetworkService = KubernetesNetworkService.SERVICE_INSTANCE;
    kubernetesNetworkService.getEndPointsWatcher();
  }


  // @Test
  public void testFetchKafkaAddress() {
    List<String> addrList = kubernetesNetworkService.getEndPointsAddressFromConfigMap();
    assertEquals(addrList.get(0), "asdf");
  }

}
