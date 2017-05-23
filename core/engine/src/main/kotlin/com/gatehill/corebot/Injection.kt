package com.gatehill.corebot

import com.google.inject.Singleton
import com.google.inject.binder.ScopedBindingBuilder

/**
 * Syntactic sugar for binding Guice singletons.
 */
fun ScopedBindingBuilder.asSingleton() = this.`in`(Singleton::class.java)
