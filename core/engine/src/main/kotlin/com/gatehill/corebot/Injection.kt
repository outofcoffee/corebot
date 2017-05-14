package com.gatehill.corebot;

import com.google.inject.Singleton
import com.google.inject.binder.ScopedBindingBuilder

fun ScopedBindingBuilder.asSingleton() = this.`in`(Singleton::class.java)
