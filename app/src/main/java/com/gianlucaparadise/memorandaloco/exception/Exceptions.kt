package com.gianlucaparadise.memorandaloco.exception

/**
 * This is thrown when the required permissions for a feature are not granted
 */
class PermissionsNotGrantedException : Exception()

/**
 * This is thrown when the user has not saved the location of his/her home
 */
class MissingHomeException : Exception()

/**
 * This is thrown when the location is invalid e needs to be retrieved in another way
 */
class InvalidLocationException : Exception()

/**
 * This is thrown when the GPS is turned off and the user
 */
class GpsTurnedOffException : Exception()