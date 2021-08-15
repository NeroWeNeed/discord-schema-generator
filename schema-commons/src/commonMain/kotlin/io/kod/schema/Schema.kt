package io.kod.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DiscordSchema(
    val resources: List<Resource>
)

@Serializable
sealed class Resource {
    abstract val name: String
}

@Serializable
@SerialName("structure")
data class StructureResource(
    override val name: String,
    val items: List<StructureItem>
) : Resource()

@Serializable
data class StructureItem(
    val fieldName: String,
    val fieldType: String,
    val description: String,
    val scope: String,
    val optional: Boolean,
    val nullable: Boolean
) {
    constructor(
        fieldName: String,
        fieldType: String,
        description: String,
        scope: String
    ) : this(
        fieldName = fieldName.substringBefore('*').trimEnd().removeSuffix("?").trimEnd(),
        fieldType = fieldType.substringBefore('*').trimEnd().removePrefix("?").trimStart(),
        description = description,
        scope = scope,
        optional = fieldName.substringBefore('*').trimEnd().endsWith('?'),
        nullable = fieldType.trimStart().startsWith('?'),
    )
}

@Serializable
@SerialName("flags")
data class FlagResource(
    override val name: String,
    val flags: List<FlagItem>
) : Resource()

@Serializable
data class FlagItem(
    val bit: Int,
    val name: String,
    val description: String? = null
)

@Serializable
@SerialName("value_enum")
data class ValueEnumResource(
    override val name: String,
    val items: List<ValueEnumItem>
) : Resource()

@Serializable
data class ValueEnumItem(
    val name: String,
    val value: String,
    val description: String? = null
)

@Serializable
@SerialName("enum")
data class EnumResource(
    override val name: String,
    val items: List<EnumItem>
) : Resource()

@Serializable
data class EnumItem(
    val name: String,
    val description: String? = null
)