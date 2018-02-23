//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Miscellaneous utility functions for reading the manifest.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package util

import java.util.jar.Manifest

import scala.util.matching.Regex

/**
 * Miscellaneous utility functions for reading the manifest.
 *
 * @author Jonathon Bell
 */
trait manifest
{
  /**
   * Returns the main attributes of the parent executable manifest as a map of
   * key-value string pairs.
   */
  def attributes: Map[String,String] =
  {
    val manifest = new Manifest(Thread.currentThread
                               .getContextClassLoader
                               .getResourceAsStream("META-INF/MANIFEST.MF"))
    val map      = collection.mutable.Map[String,String]()

    manifest.getMainAttributes.forEach                   // For each (key,val)
    {
      case (k,v) ⇒ map += k.toString → v.toString        // ...add string pair
    }

    map.toMap                                            // As immutable map
  }

  /**
   * Replaces occurrences of attribute  placeholders within the given template
   * string with their respective values in the manifest.
   *
   * An attribute placeholder is specified with the syntax `\${attribute}`, as
   * if referring to a variable from within a Scala string interpolation.  The
   * attribute values themselves are specified in the parent POM, and embedded
   * within the manifest at build time.
   *
   * For example:
   * {{{
   *    manifest.format("v$\{Implementation-Version}")   // Get version string
   * }}}
   * might return something like:
   * {{{
   *    "v1.0.0-SNAPSHOT"
   * }}}
   *
   * @param  string  A template string containing attribute placeholders.
   *
   * @return A copy of the input string in  which each placeholder is replaced
   *         with its respective value in the manifest, or the empty string if
   *         no such attribute exists.
   */
  def format(string: String): String =
  {
    val a = attributes                                   // Get the attributes
    val p = m_regex                                      // Matches ${attrib}

    p.replaceAllIn(string,m ⇒ a.getOrElse(m.group(1),""))// Replace with value
  }

  /**
   * Replaces occurrences of attribute  placeholders within the given template
   * string with their respective values in the manifest and prints the result
   * to the console.
   *
   * @param  string  A template string containing attribute placeholders.
   */
  def println(string: String): Unit =
  {
    Console.println(format(string))                      // Format, then print
  }

  /**
   * A regular expression that matches an attribute placeholder.
   */
  private lazy
  val m_regex: Regex = """\$\{([A-Za-z-]+)\}""".r        // Matches ${attrib}
}

/**
 * Miscellaneous utility functions for working with a manifest.
 *
 * @author Jonathon Bell
 */
object manifest extends manifest

//****************************************************************************
