package io.github.siyual_park.viewer

interface Views {
    interface Public
    interface Protect : Public
    interface Private : Protect
}
