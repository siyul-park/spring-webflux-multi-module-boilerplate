package io.github.siyual_park.data.repository

import reactor.core.scheduler.Schedulers

val dataIOSchedulers = Schedulers.newParallel("data-io")
