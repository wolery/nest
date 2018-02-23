//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : A trait from which JavaFX applications can extend.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package fx
package util

import javafx.application.{Application ⇒ application}
import javafx.stage.Stage

import scala.collection.JavaConverters._

import com.wolery.util.Logging

import applications._

/**
 * A trait from which JavaFX applications can extend.
 *
 * == Rationale ==
 *
 * The JavaFX framework requires its client to provide the class of a concrete
 * application subclass it can then instantiate via reflection.
 *
 * A Scala application, on the other hand,  must specify a Scala object as the
 * owner of the `main` entry point,  and such an object cannot be instantiated
 * by the JavaFX framework directly.
 *
 * For this reason,  JavaFX applications implemented in Scala typically define
 * both an application object and application subclass. It would be convenient
 * if these two entities could be one and the same, however;  this is the goal
 * of the Application trait.
 *
 * == Example ==
 *
 * The following example illustrates a simple use of the Application trait:
 * {{{
 *    import javafx.scene.Group
 *    import javafx.scene.Scene
 *    import javafx.scene.shape.Circle
 *    import javafx.stage.Stage
 *
 *    object MyApplication extends com.wolery.fx.util.Application =
 *    {
 *      def start(stage: Stage): Unit =
 *      {
 *        stage.setScene(new Scene(new Group(new Circle(40,40,30)),400,300))
 *        stage.setTitle("My Application")
 *        stage.show()
 *      }
 *    }
 * }}}
 *
 * == Life-Cycle==
 *
 * The JavaFX runtime does the following, in order, whenever an application is
 * launched:
 *
 *  1. Starts the JavaFX runtime, if not already started.
 *  1. Calls the application's `init` method.
 *  1. Calls the application's `start` method.
 *  1. Waits for the application to finish, which happens when either:
 *   - the application calls `Platform.exit`
 *   - the application calls `System.exit`
 *   - the last window has been closed and the `implicitExit` attribute of the
 *     `Platform` is true.
 *  1. Calls the application's `stop` method.
 *
 * Calling `Platform.exit` is the  preferred way to  explicitly  terminate the
 * application.  Directly calling `System.exit` is an  acceptable alternative,
 * but does not allow the application's `stop` method to run.
 *
 * The application should not make calls to the JavaFX library once `stop` has
 * run or `System.exit` has been called.
 *
 * == Threading ==
 *
 * JavaFX creates an application thread for invoking `start`, processing input
 * events, and running animation timelines.
 *
 * Creating `Scene` and `Stage` objects, as well as modifying ''live'' objects
 * (those attached to a scene), must all be performed on this thread.
 *
 * The `init` method is called on the launcher thread, however, not the JavaFX
 * application thread.
 *
 * Any unhandled exceptions on the JavaFX application thread that occur during
 * event dispatching, running animation timelines, or any other code, are sent
 * to the thread's uncaught-exception handler.
 *
 * @author Jonathon Bell
 */
trait Application extends Logging
{
  App = this

  /**
   * The application initialization method.
   *
   * Called immediately after the application class is loaded and constructed.
   *
   * The application object may override this method to perform initialization
   * prior to the actual starting of the application.
   *
   * The default implementation does nothing.
   *
   * Not called on the JavaFX application thread,  so should create `Scene` or
   * `Stage` objects.  The application may create other JavaFX objects in this
   * this method, however, and hook them into the scene later.
   *
   * @throws Exception if something goes wrong.
   */
  def init(): Unit =
  {
    log.debug("init()")                                  // Trace our progress
  }

  /**
   * The main entry point for the application.
   *
   * Called after the `init` method has returned and after the system is ready
   * for the application to begin running.
   *
   * Called on the JavaFX application thread.
   *
   * @param  stage  The primary stage for this application onto which the main
   *                application scene is then set.
   *
   * @throws Exception if something goes wrong.
   */
  def start(stage: Stage): Unit

  /**
   * Called when the application should stop, and provides a convenient place
   * to prepare for application exit and destroy resources.
   *
   * The default implementation does nothing.
   *
   * Called on the JavaFX application thread.
   *
   * @throws Exception if something goes wrong.
   */
  def stop(): Unit =
  {
    log.debug("stop()")                                  // Trace our progress
  }

  /**
   * Returns the user agent stylesheet used by the entire application. This is
   * used to specify default styling for all UI controls and nodes.
   *
   * A value of `None` means the platform default stylesheet is being used.
   *
   * Must be called on the JavaFX application thread.
   *
   * @return The URL string for the user agent stylesheet.
   */
  def stylesheet: Option[String] =
  {
    Option(application.getUserAgentStylesheet)           // Delegate to JavaFX
  }

  /**
   * Updates the user agent stylesheet used by the entire application. This is
   * used to specify default styling for all UI controls and nodes.
   *
   * Must be called on the JavaFX application thread.
   *
   * @param  url  The URL of the user agent stylesheet, or `None` to revert to
   *              using the default stylesheet.
   */
  def stylesheet_=(url: Option[String]): Unit =
  {
    log.debug("stylesheet_=({})",url)                    // Trace out progress

    val u = url.getOrElse(null)                          // May be null string

    application.setUserAgentStylesheet(u)                // Delegate to JavaFX
  }

  /**
   * Returns the entire set of command line arguments given to `main` prior to
   * parsing them for named parameters.
   *
   * @return  The unparsed set of command line arguments to `main`.
   */
  def rawParameters: Seq[String] =
  {
    app.getParameters.getRaw.asScala                     // Delegate to JavaFX
  }

  /**
   * Returns a map of the named command line arguments given to `main`.
   *
   * Arguments are named on the command line with the syntax `--name=value`.
   *
   * @return The named command line arguments.
   */
  def namedParameters: Map[String,String] =
  {
    app.getParameters.getNamed.asScala.toMap             // Delegate to JavaFX
  }

  /**
   * Returns the sequence of unnamed command line arguments given to `main`.
   *
   * @return The unnamed command line arguments.
   */
  def unnamedParameters: Seq[String] =
  {
    app.getParameters.getUnnamed.asScala                 // Delegate to JavaFX
  }

  /**
   * Launches the application.
   *
   * Throws an an exception if called more than once.
   *
   * Does not return until the application  has exited,  either with a call to
   * `Platform.exit`, `System.exit`, or the closing of the last stage window.
   *
   * @param  args  The raw command line arguments. They can be retrieved later
   *               with the `raw/named/unnamedParameters` methods.
   *
   * @throws IllegalStateException if called more than once.
   *
   * @throws RuntimeException if unable to launch the JavaFX runtime.
   */
  def main(args: Array[String]): Unit =
  {
    log.debug("main({})",args)                           // Trace our progress

    application.launch(classOf[ApplicationProxy],args:_*)// Delegate to JavaFX
  }
}

/**
 * A concrete subclass of the JavaFX application class will beinstantiated via
 * reflection by the framework on launching, and that forwards its key methods
 * on to the the Scala application object.
 *
 * @author Jonathon Bell
 */
private final
class ApplicationProxy extends application
{
  app = this                                             // Save this instance
  override def init()         : Unit = App.init()        // Delegate to JavaFX
  override def start(s: Stage): Unit = App.start(s)      // Delegate to JavaFX
  override def stop()         : Unit = App.stop()        // Delegate to JavaFX
}

/**
 * Saves references to both the Scala application object (''ours'') and JavaFX
 * instantiated application subclass (''theirs'') so that each can delegate to
 * the other.
 *
 * @author Jonathon Bell
 */
private
object applications
{
  var App: Application = _                               // Our   application
  var app: application = _                               // Their application
}

//****************************************************************************
