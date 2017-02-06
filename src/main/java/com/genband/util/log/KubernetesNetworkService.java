package com.genband.util.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import org.apache.log4j.Logger;

import com.genband.util.log.config.ConfigManager;
import com.genband.util.log.constants.LogConfigConstants;

public enum KubernetesNetworkService {

  SERVICE_INSTANCE;

  private Logger logger = Logger.getLogger(KubernetesNetworkService.class);
  private String kubernetesMasterUrl;
  private Config config;
  private KubernetesClient client;
  private List<String> addressList;// pods address list
  private ConfigManager configManager = null;

  /**
   * Get instance of KubernetesNetworkService
   * 
   * @return
   */
  public synchronized static KubernetesNetworkService getInstance() {
    return SERVICE_INSTANCE;
  }

  public ConfigManager getConfigManager() {
    return configManager;
  }

  /**
   * Get values from builder and assign those values to all the fields
   * 
   * @param builder singleton builder
   */
  private void build(SingletonBuilder builder) {
    if (builder != null) {
      this.kubernetesMasterUrl =
          (builder.kubernetesMasterUrl == null || builder.kubernetesMasterUrl.isEmpty())
              ? getMasterUrlFromSystemVariable() : builder.kubernetesMasterUrl;
      this.configManager = builder.configManager;

      if (kubernetesMasterUrl != null) {
        config = new ConfigBuilder().withMasterUrl(kubernetesMasterUrl).build();
        client = new DefaultKubernetesClient(this.config);
        addressList = new ArrayList<>();
      }
    }
  }

  /**
   * Get all address of endpoints with same label in config map
   * 
   * @return endpoints address list
   */
  public List<String> getEndPointsAddressFromConfigMap() {
    return getEndpointsAddressByLabel(configManager.getLabelMap());
  }

  /**
   * Get all address from endpoints
   * 
   * @return endpoints address list
   */
  private List<String> getEndPointsAddressList(Endpoints endpoints) {
    // currently only have one kafka entry address.
    List<String> endPointAddressList = new ArrayList<>();
    if (endpoints != null && endpoints.getSubsets() != null) {
      for (EndpointSubset subset : endpoints.getSubsets()) {
        if (subset.getAddresses() != null) {
          EndpointAddress address = subset.getAddresses().iterator().next();
          if (subset.getPorts() != null) {
            EndpointPort port = subset.getPorts().iterator().next();
            endPointAddressList.add(address.getIp() + ":" + port.getPort());
          }
        }
      }
    }
    return endPointAddressList;
  }

  /**
   * start to watch those endpoints with same specified label (in a same service)
   * 
   */
  public Watch getEndPointsWatcher() {
    Watch watcher = null;
    if (client != null && client.endpoints() != null && configManager != null
        && configManager.getLabelMap() != null && !configManager.getLabelMap().isEmpty()) {
      watcher =
          client.endpoints().withLabels(configManager.getLabelMap()).watch(new EndpointWatcher());
    }
    return watcher;
  }

  public void close() {
    client.close();
  }

  public List<String> getEndpointsAddressByLabel(HashMap<String, String> labelsMap) {
    List<String> resAddressList = new ArrayList<>();
    EndpointsList endPointsList = KubernetesConnectionUtils.getEndPointsListBylabels(client,
        labelsMap, configManager.getRetries());
    if (endPointsList != null) {
      for (Endpoints endPoints : endPointsList.getItems()) {
        resAddressList.addAll(getEndPointsAddressList(endPoints));
      }
    }
    return resAddressList;
  }


  /**
   * Get kubernetes url from system variable which will be set in docker file. sometimes, this
   * doesn't work under windows system. Dont't know the reason yet.
   * 
   * @return kubernetes Master Url
   */
  private String getMasterUrlFromSystemVariable() {
    kubernetesMasterUrl =
        System.getenv().get(LogConfigConstants.kubernetes_master_url_evn.toString());
    if (kubernetesMasterUrl == null) {
      logger.error("no environment variables configured for k8s master url");
      return null;
    }
    return kubernetesMasterUrl;
  }

  private class EndpointWatcher implements Watcher<Endpoints> {

    @Override
    public void eventReceived(io.fabric8.kubernetes.client.Watcher.Action action,
        Endpoints endpoints) {
      if (action == Action.MODIFIED) {
        processModifiedAction(endpoints);
      } else if (action == Action.ADDED) {
        processAddedAction(endpoints);
      } else if (action == Action.DELETED) {
        processDeletedFunction(endpoints);
      }
    }

    private void processDeletedFunction(Endpoints endpoints) {
      logger.debug("Deleting event on endpoints");
      addressList = getEndPointsAddressList(endpoints);
      LoggerOperation.LoggerAppenderUpdate();
    }

    private void processAddedAction(Endpoints endpoints) {
      addressList = getEndPointsAddressList(endpoints);
      logger.info("Adding event on endpoints: " + addressList);
      LoggerOperation.LoggerAppenderUpdate();
    }

    private void processModifiedAction(Endpoints endpoints) {
      addressList = getEndPointsAddressList(endpoints);
      logger.info("Modifying event on endpoints: " + addressList);
      LoggerOperation.LoggerAppenderUpdate();
    }

    @Override
    public void onClose(KubernetesClientException cause) {
      logger.info("Watch closed");
    }
  }

  /**
   * Set initial KubernetesNetworkService parameters with the singleton builder class
   * 
   * @author dixiao
   *
   */
  public static class SingletonBuilder {

    private String kubernetesMasterUrl; // Mandatory
    private ConfigManager configManager = null; // Mandatory

    /**
     * Get configs from configManager, and assign them to builder
     * 
     * @param configManager
     */
    public SingletonBuilder(ConfigManager configManager) {
      this.kubernetesMasterUrl = configManager.getKubernetesMasterUrl();
      this.configManager = configManager;
    }

    /**
     * Assgin values from builder to fields of KubernetesNetworkService
     */
    public void build() {
      KubernetesNetworkService.SERVICE_INSTANCE.build(this);
    }
  }
}
