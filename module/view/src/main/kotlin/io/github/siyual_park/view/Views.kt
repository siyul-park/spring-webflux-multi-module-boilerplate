package io.github.siyual_park.view

interface Views {
    interface Public
    interface Protect : Public
    interface Private : Protect
}
