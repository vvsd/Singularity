package com.hubspot.singularity.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.hubspot.singularity.WebhookType;
import com.hubspot.singularity.hooks.WebhookQueueType;

public class WebhookQueueConfiguration {
  @JsonProperty
  private WebhookQueueType queueType = WebhookQueueType.ZOOKEEPER;

  @JsonProperty
  private Map<WebhookType, String> snsTopics = ImmutableMap.of(
      WebhookType.TASK, "singularity-task-updates",
      WebhookType.DEPLOY, "singularity-deploy-updates",
      WebhookType.REQUEST, "singularity-request-updates"
  );

  @JsonProperty
  private Optional<String> awsAccessKey = Optional.absent();

  @JsonProperty
  private Optional<String> awsSecretKey = Optional.absent();

  @JsonProperty
  private Optional<String> awsRegion = Optional.absent();

  public WebhookQueueType getQueueType() {
    return queueType;
  }

  public void setQueueType(WebhookQueueType queueType) {
    this.queueType = queueType;
  }

  public Map<WebhookType, String> getSnsTopics() {
    return snsTopics;
  }

  public void setSnsTopics(Map<WebhookType, String> snsTopics) {
    this.snsTopics = snsTopics;
  }

  public Optional<String> getAwsAccessKey() {
    return awsAccessKey;
  }

  public void setAwsAccessKey(Optional<String> awsAccessKey) {
    this.awsAccessKey = awsAccessKey;
  }

  public Optional<String> getAwsSecretKey() {
    return awsSecretKey;
  }

  public void setAwsSecretKey(Optional<String> awsSecretKey) {
    this.awsSecretKey = awsSecretKey;
  }

  public Optional<String> getAwsRegion() {
    return awsRegion;
  }

  public void setAwsRegion(Optional<String> awsRegion) {
    this.awsRegion = awsRegion;
  }
}
