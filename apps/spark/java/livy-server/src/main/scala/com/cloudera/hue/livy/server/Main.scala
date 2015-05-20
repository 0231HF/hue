/*
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
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

package com.cloudera.hue.livy.server

import javax.servlet.ServletContext

import com.cloudera.hue.livy.server.batch.{BatchSessionProcessFactory, BatchSessionServlet, BatchManager, BatchSessionYarnFactory}
import com.cloudera.hue.livy.server.interactive._
import com.cloudera.hue.livy.{Utils, Logging, LivyConf, WebServer}
import org.scalatra._
import org.scalatra.servlet.ScalatraListener

object Main {

  val SESSION_KIND = "livy-server.session.kind"
  val THREAD_SESSION = "thread"
  val PROCESS_SESSION = "process"
  val YARN_SESSION = "yarn"

  def main(args: Array[String]): Unit = {
    val livyConf = new LivyConf()
    Utils.loadDefaultLivyProperties(livyConf)

    val host = livyConf.get("livy.server.host", "0.0.0.0")
    val port = livyConf.getInt("livy.server.port", 8998)

    val server = new WebServer(host, port)

    server.context.setResourceBase("src/main/com/cloudera/hue/livy/server")
    server.context.setInitParameter(ScalatraListener.LifeCycleKey, classOf[ScalatraBootstrap].getCanonicalName)
    server.context.addEventListener(new ScalatraListener)

    server.start()

    try {
      System.setProperty("livy.server.callback-url", f"http://${server.host}:${server.port}")
    } finally {
      server.join()
      server.stop()

      // Make sure to close all our outstanding http requests.
      dispatch.Http.shutdown()
    }
  }
}

class ScalatraBootstrap extends LifeCycle with Logging {

  var sessionManager: SessionManager = null
  var batchManager: BatchManager = null

  override def init(context: ServletContext): Unit = {
    val livyConf = new LivyConf()

    val sessionFactoryKind = try {
      livyConf.sessionKind()
    } catch {
      case e: IllegalStateException =>
        println(f"Unknown session factory: $e}")
        sys.exit(1)
    }

    info(f"Using $sessionFactoryKind sessions")

    val (sessionFactory, batchFactory) = sessionFactoryKind match {
      case LivyConf.Thread() =>
        (new InteractiveSessionProcessFactory(livyConf), new BatchSessionProcessFactory(livyConf) )
      case LivyConf.Process() =>
        (new InteractiveSessionProcessFactory(livyConf), new BatchSessionProcessFactory(livyConf))
      case LivyConf.Yarn() =>
        (new InteractiveSessionYarnFactory(livyConf), new BatchSessionYarnFactory(livyConf))
    }

    sessionManager = new SessionManager(sessionFactory)
    batchManager = new BatchManager(batchFactory)

    context.mount(new InteractiveSessionServlet(sessionManager), "/sessions/*")
    context.mount(new BatchSessionServlet(batchManager), "/batches/*")
  }

  override def destroy(context: ServletContext): Unit = {
    if (sessionManager != null) {
      sessionManager.shutdown()
    }

    if (batchManager != null) {
      batchManager.shutdown()
    }
  }
}
