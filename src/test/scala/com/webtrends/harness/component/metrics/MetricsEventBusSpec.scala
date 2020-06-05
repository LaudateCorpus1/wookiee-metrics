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
import com.webtrends.harness.component.metrics.messages.{CounterObservation, MeterObservation}
import com.webtrends.harness.component.metrics.metrictype.{Counter, Meter}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class MetricsEventBusSpec extends TestKit(ActorSystem("harness")) with WordSpecLike with Matchers with BeforeAndAfterAll {

  val metric: Counter = Counter("group.subgroup.name.scope")
  val meter: Meter = Meter("group.subgroup.name.scope")

  "The event bus should " should {

    " allow actors to subscribe and receive metric observations" in {
      val probe = new TestProbe(system)
      MetricsEventBus.subscribe(probe.ref)

      val obs = CounterObservation(metric, 1)
      MetricsEventBus.publish(obs)
      MetricsEventBus.unsubscribe(probe.ref)

      obs mustBe probe.expectMsg(obs)
    }

    " allow actors to subscribe and then un-subscribe" in {
      val probe = new TestProbe(system)
      MetricsEventBus.subscribe(probe.ref)

      val obs = MeterObservation(meter, 1)
      MetricsEventBus.publish(obs)
      obs mustBe probe.expectMsg(obs)

      MetricsEventBus.unsubscribe(probe.ref)
      MetricsEventBus.publish(obs)
      probe.expectNoMsg()
      succeed
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
