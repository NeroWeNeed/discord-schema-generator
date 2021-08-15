package io.kod.commons

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed class Optional<Value> {
    abstract var value: Value
    abstract val present: Boolean


    @Serializable(with = NonNullable.Serializer::class)
    class NonNullable<Value : Any> : Optional<Value> {

        class Serializer<Value : Any>(private val dataSerializer: KSerializer<Value>) :
            KSerializer<NonNullable<Value>> {

            override val descriptor: SerialDescriptor = dataSerializer.descriptor
            override fun deserialize(decoder: Decoder): NonNullable<Value> {
                return NonNullable<Value>(decoder.decodeSerializableValue(dataSerializer))
            }

            override fun serialize(encoder: Encoder, value: NonNullable<Value>) {
                if (value.present) {
                    encoder.encodeSerializableValue(dataSerializer, value.value)
                }
            }

        }

        private lateinit var _value: Value
        override var present: Boolean
            private set
        override var value: Value
            get() = _value
            set(value) {
                _value = value
                present = true
            }

        constructor(value: Value) {
            _value = value
            present = true
        }

        constructor() {
            present = false
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NonNullable<*>) return false
            if (!present) return false
            return _value == other._value
        }

        override fun hashCode(): Int {
            var result = present.hashCode()
            if (!present) return result
            result = 31 * result + _value.hashCode()
            return result
        }
    }

    @Serializable(with = Nullable.Serializer::class)
    class Nullable<Value : Any> : Optional<Value?> {
        class Serializer<Value : Any>(private val dataSerializer: KSerializer<Value>) :
            KSerializer<Nullable<Value>> {

            override val descriptor: SerialDescriptor = dataSerializer.descriptor
            override fun deserialize(decoder: Decoder): Nullable<Value> {
                return Nullable(decoder.decodeSerializableValue(dataSerializer))
            }

            override fun serialize(encoder: Encoder, value: Nullable<Value>) {
                if (value.present) {
                    value.value?.run {
                        encoder.encodeSerializableValue(dataSerializer, this)
                    } ?: encoder.encodeNull()
                }
            }

        }

        override var value: Value?
            set(value) {
                field = value
                present = true
            }
        override var present: Boolean
            private set

        constructor(value: Value?) {
            this.value = value
            present = true
        }

        constructor() {
            this.value = null
            this.present = false
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Nullable<*>) return false
            if (!present) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            var result = present.hashCode()
            if (!present) return result
            result = 31 * result + value.hashCode()
            return result
        }


    }


}

class NonNullableOptionalDelegate<T : Any> : ReadOnlyProperty<Any, Optional.NonNullable<T>> {

    private var value: Optional.NonNullable<T>

    constructor(value: T) {
        this.value = Optional.NonNullable(value)
    }

    constructor() {
        this.value = Optional.NonNullable()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Optional.NonNullable<T> {
        return value
    }
}

class NullableOptionalDelegate<T : Any> : ReadOnlyProperty<Any, Optional.Nullable<T>> {

    private var value: Optional.Nullable<T>

    constructor(value: T) {
        this.value = Optional.Nullable(value)
    }

    constructor() {
        this.value = Optional.Nullable()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Optional.Nullable<T> {
        return value
    }
}