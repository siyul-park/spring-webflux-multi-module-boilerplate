package io.github.siyual_park.data.repository

import reactor.core.scheduler.Schedulers

val dataIOSchedulers = Schedulers.newParallel("db")
val dataSchedulers = Schedulers.immediate()
