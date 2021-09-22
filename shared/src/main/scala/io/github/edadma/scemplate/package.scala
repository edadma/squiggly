package io.github.edadma

package object scemplate {

  trait Platform {
    def yaml(s: String): Any
  }

  lazy val platform: Platform = scemplate.platformSpecific

}
