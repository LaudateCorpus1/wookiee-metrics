/*
 * Copyright 2015 Webtrends (http://www.webtrends.com)
 *
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webtrends.harness.component.metrics

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.webtrends.harness.component.metrics.messages._
import com.webtrends.harness.component.metrics.metrictype._
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class MetricSpec extends TestKit(ActorSystem("metricspec"))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val probe = new TestProbe(system)
  MetricsEventBus.subscribe(probe.ref)

  "metrics " should {
    "allow for counters" in {
      val metric = Counter("group.subgroup.name.scope")
      metric.incr

      val obs = CounterObservation(metric, 1)
      obs mustBe probe.expectMsg(obs)

      metric.incr(5)
      val obs2 = CounterObservation(metric, 5)
      obs2 mustBe probe.expectMsg(obs2)
    }

    "allow for gauges" in {
      val metric = Gauge("group.subgroup.name.scope")
      metric.update(3.25F)
      val obs = GaugeObservation(metric, 3.25F)
      obs mustBe probe.expectMsg(obs)
    }

    "allow for histograms" in {
      val metric = Histogram("group.subgroup.name.scope")
      metric.update(15)
      val obs = HistogramObservation(metric, 15)
      obs mustBe probe.expectMsg(obs)
    }

    "allow for meters" in {
      val metric = Meter("group.subgroup.name.scope.event")
      metric.mark
      val obs = MeterObservation(metric, 1)
      obs mustBe probe.expectMsg(obs)

      metric.mark(5)
      val obs2 = MeterObservation(metric, 5)
      obs2 mustBe probe.expectMsg(obs2)

      metric.meter() {
        val x = 1
      }

      probe.expectMsgClass(classOf[MeterObservation]).isInstanceOf[MeterObservation] mustBe true
    }

    "allow for timers" in {
      val metric = Timer("group.subgroup.name.scope")

      metric.time() {
        val x = 1
      }

      probe.expectMsgClass(classOf[TimerObservation]).isInstanceOf[TimerObservation] mustBe true
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
