package com.quote.mosaic.core.di

import androidx.databinding.DataBindingComponent

/*
 * This class must remain a pure java class to be generated at the right time during compilation.
 *
 * The code generation chain looks something like this:
 *      1. APT populates DataBindingComponent interface with functions required to create databinding adapters
 *      2. Kotlin generates bytecode from .kt files
 *      3. KAPT generates code for dagger
 *
 */
interface AppDataBindingComponent: DataBindingComponent