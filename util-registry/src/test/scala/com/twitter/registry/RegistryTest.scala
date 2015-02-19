package com.twitter.registry

import java.lang.{Character => JCharacter}
import org.scalatest.FunSuite

abstract class RegistryTest extends FunSuite {
  def mkRegistry(): Registry
  def name: String

  test(s"$name can insert a key/value pair and then read it") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    assert(registry.toSet == Set(Entry(Seq("foo"), "bar")))
  }

  test(s"$name's iterator is not affected by adding an element") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    val iter = registry.iterator()
    registry.put(Seq("foo"), "baz")
    assert(iter.next() == Entry(Seq("foo"), "bar"))
    assert(!iter.hasNext)
  }

  test(s"$name can overwrite old element") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    registry.put(Seq("foo"), "baz")
    assert((registry.toSet) == Set(Entry(Seq("foo"), "baz")))
  }

  test(s"$name can return the old element when replacing") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    assert(registry.put(Seq("foo"), "baz") == Some("bar"))
    assert(registry.toSet == Set(Entry(Seq("foo"), "baz")))
  }

  test(s"$name can support multiple elements") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    registry.put(Seq("baz"), "qux")
    assert(registry.toSet == Set(Entry(Seq("foo"), "bar"), Entry(Seq("baz"), "qux")))
  }

  test(s"$name can support nontrivial keys") {
    val registry = mkRegistry()
    registry.put(Seq("foo", "bar", "baz"), "qux")
    assert(registry.toSet == Set(Entry(Seq("foo", "bar", "baz"), "qux")))
  }

  test(s"$name can support empty keys") {
    val registry = mkRegistry()
    registry.put(Seq(), "qux")
    assert(registry.toSet == Set(Entry(Seq(), "qux")))
  }

  test(s"$name can sanitize bad values") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "q/ux")
    assert(registry.toSet == Set(Entry(Seq("foo"), "qux")))
  }

  test(s"$name can sanitize bad keys") {
    val registry = mkRegistry()
    registry.put(Seq("fo☃o", s"bar${JCharacter.toString(31)}"), "qux")
    assert(registry.toSet == Set(Entry(Seq("foo", "bar"), "qux")))
  }

  test(s"$name can support keys that are subsequences of other keys") {
    val registry = mkRegistry()
    registry.put(Seq("foo"), "bar")
    registry.put(Seq("foo", "baz"), "qux")
    assert(registry.toSet == Set(Entry(Seq("foo"), "bar"), Entry(Seq("foo", "baz"), "qux")))
  }
}
