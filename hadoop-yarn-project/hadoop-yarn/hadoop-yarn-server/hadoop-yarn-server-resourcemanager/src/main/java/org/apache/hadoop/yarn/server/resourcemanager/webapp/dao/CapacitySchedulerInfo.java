/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.webapp.dao;

import org.apache.hadoop.yarn.nodelabels.NodeLabel;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CSQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.LeafQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.QueueCapacities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "capacityScheduler")
@XmlType(name = "capacityScheduler")
@XmlAccessorType(XmlAccessType.FIELD)
public class CapacitySchedulerInfo extends SchedulerInfo {

  protected float capacity;
  protected float usedCapacity;
  protected float maxCapacity;
  protected String queueName;
  protected CapacitySchedulerQueueInfoList queues;

  @XmlTransient
  static final float EPSILON = 1e-8f;

  public CapacitySchedulerInfo() {
  } // JAXB needs this

  public CapacitySchedulerInfo(CSQueue parent, String nodeLabel) {
    this(parent, new NodeLabel(nodeLabel));
  }

  public CapacitySchedulerInfo(CSQueue parent, NodeLabel nodeLabel) {
    this.queueName = parent.getQueueName();
    this.usedCapacity = parent.getUsedCapacity() * 100;
    this.capacity = parent.getCapacity() * 100;
    float max = parent.getMaximumCapacity();
    if (max < EPSILON || max > 1f)
      max = 1f;
    this.maxCapacity = max * 100;

    queues = getQueues(parent, nodeLabel);
  }

  public CapacitySchedulerInfo(CSQueue parent, CapacityScheduler cs,
      NodeLabel nodeLabel) {
    String label = nodeLabel.getLabelName();
    QueueCapacities parentQueueCapacities = parent.getQueueCapacities();
    this.queueName = parent.getQueueName();
    this.usedCapacity = parent.getUsedCapacity(label) * 100;
    this.capacity = parentQueueCapacities.getCapacity(label) * 100;
    float max = parentQueueCapacities.getMaximumCapacity(label);
    if (max < EPSILON || max > 1f)
      max = 1f;
    this.maxCapacity = max * 100;

    queues = getQueues(parent, nodeLabel);
  }

  public float getCapacity() {
    return this.capacity;
  }

  public float getUsedCapacity() {
    return this.usedCapacity;
  }

  public float getMaxCapacity() {
    return this.maxCapacity;
  }

  public String getQueueName() {
    return this.queueName;
  }

  public CapacitySchedulerQueueInfoList getQueues() {
    return this.queues;
  }

  protected CapacitySchedulerQueueInfoList getQueues(CSQueue parent,
      NodeLabel nodeLabel) {
    CSQueue parentQueue = parent;
    CapacitySchedulerQueueInfoList queuesInfo =
        new CapacitySchedulerQueueInfoList();
    for (CSQueue queue : parentQueue.getChildQueues()) {
      CapacitySchedulerQueueInfo info;
      if (queue instanceof LeafQueue) {
        info =
            new CapacitySchedulerLeafQueueInfo((LeafQueue) queue,
                nodeLabel.getLabelName());
      } else {
        info = new CapacitySchedulerQueueInfo(queue, nodeLabel.getLabelName());
        info.queues = getQueues(queue, nodeLabel);
      }
      queuesInfo.addToQueueInfoList(info);
    }
    return queuesInfo;
  }

}
