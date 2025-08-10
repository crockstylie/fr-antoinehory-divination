package fr.antoinehory.divination.viewmodels

/**
 * Represents a generic user interaction event within the application.
 * This sealed interface serves as a base for specific interaction types
 * that can be observed and reacted to by ViewModels or UI components.
 */
interface InteractionEvent

/**
 * Represents a shake gesture detected by the device.
 * This event is typically emitted when the user shakes the device.
 */
object ShakeEvent : InteractionEvent

/**
 * Represents a tap gesture on the screen.
 * This event is typically emitted when the user taps on a designated area or component.
 */
object TapEvent : InteractionEvent
