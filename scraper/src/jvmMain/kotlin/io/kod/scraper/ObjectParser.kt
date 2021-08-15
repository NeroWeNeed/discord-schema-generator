package io.kod.scraper

import io.kod.schema.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openqa.selenium.By
import org.openqa.selenium.WebElement


data class Column(val names: List<String>, val required: Boolean = true) {
    constructor(vararg names: String, required: Boolean = true) : this(names.toList(), required)
}

data class ColumnSet(val columns: List<Column>) {
    constructor(vararg columns: Column) : this(columns.toList())
}

val HANDLERS = listOf(
    ColumnSet(
        Column("field"),
        Column("type"),
        Column("description","note"),
        Column("valid types", "valid for", "interaction type", required = false)
    ) to StructureHandler,
    ColumnSet(
        Column("value"), Column("flag", "permission"), Column("description", required = false),
        Column("channel type")
    ) to FlagsHandler,
    ColumnSet(
        Column("type", "mode", "key", "level", "name"),
        Column("value", "integer"),
        Column("description", "note", required = false)
    ) to ValueEnumResourceHandler,
    ColumnSet(Column("feature"), Column("description")) to EnumResourceHandler
).buildHandlerMap().associate { it }

fun List<Pair<ColumnSet, ResourceHandler<*>>>.buildHandlerMap(): List<Pair<Set<String>, ResourceHandler<*>>> {
    val result = ArrayList<Pair<Set<String>, ResourceHandler<*>>>()
    this.forEach { (columnSet, handler) ->
        buildHandlerMap(columnSet, 0, handler, HashSet(), result)
    }
    return result
}

private fun buildHandlerMap(
    columnSet: ColumnSet,
    index: Int,
    handler: ResourceHandler<*>,
    columnConfiguration: MutableSet<String>,
    result: MutableList<Pair<Set<String>, ResourceHandler<*>>>
) {

    columnSet.columns[index].names.forEach {
        if (index + 1 < columnSet.columns.size) {
            buildHandlerMap(columnSet, index + 1, handler, HashSet(columnConfiguration.plus(it)), result)
        } else {
            result.add(columnConfiguration.plus(it) to handler)
        }
    }
    if (!columnSet.columns[index].required) {
        if (index + 1 < columnSet.columns.size) {
            buildHandlerMap(columnSet, index + 1, handler, HashSet(columnConfiguration), result)
        } else {
            result.add(columnConfiguration to handler)
        }
    }

}


fun parseObject(header: WebElement, table: WebElement): Resource? {

    val name = header.getAttribute("id")

    val columns = table.findElement(By.tagName("thead")).findElements(By.tagName("th")).map {
        it.text.lowercase()
    }

    return HANDLERS[columns.toSet()]?.handle(name, columns, table)

}

sealed class ResourceHandler<T : Resource> {
    abstract fun handle(name: String, columns: List<String>, element: WebElement): T
}

object StructureHandler : ResourceHandler<StructureResource>() {
    override fun handle(name: String, columns: List<String>, element: WebElement): StructureResource {
        val fieldNameIndex = columns.indexOf("field")
        val typeIndex = columns.indexOf("type")
        val descriptionIndex = columns.findIndex("description", "note")
        val scopeIndex = columns.findIndex("valid types", "valid for", "interaction type")
        return StructureResource(name = name,
            items = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { tr ->
                tr.findElements(By.tagName("td")).let { tds ->
                    StructureItem(
                        tds[fieldNameIndex].text,
                        tds[typeIndex].text,
                        tds[descriptionIndex].text,
                        if (scopeIndex >= 0) tds[scopeIndex].text else ""
                    )
                }
            }
        )
    }

}

object FlagsHandler : ResourceHandler<FlagResource>() {
    private val bitRegex = Regex("1\\s*\\<\\<\\s*([0-9]+)")
    override fun handle(name: String, columns: List<String>, element: WebElement): FlagResource {
        val bitIndex = columns.indexOf("value")
        val nameIndex = columns.findIndex(listOf("flag", "permission"))
        val descriptionIndex = columns.indexOf("description")
        return FlagResource(
            name = name,
            flags = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { tr ->
                tr.findElements(By.tagName("td")).let { tds ->
                    val bitText = tds[bitIndex].text
                    val nameText = tds[nameIndex].text
                    val descriptionText = if (descriptionIndex >= 0) tds[descriptionIndex].text else null

                    FlagItem(bitRegex.find(bitText)!!.groupValues[1].toInt(), nameText, descriptionText)

                }
            }
        )
    }
}

object ValueEnumResourceHandler : ResourceHandler<ValueEnumResource>() {
    override fun handle(name: String, columns: List<String>, element: WebElement): ValueEnumResource {
        val nameIndex = columns.findIndex(listOf("type", "mode", "key", "level", "name"))
        val valueIndex = columns.findIndex(listOf("value", "integer"))
        val descriptionIndex = columns.findIndex("description","note")
        return ValueEnumResource(
            name = name,
            items = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { tr ->
                tr.findElements(By.tagName("td")).let { tds ->
                    val valueText = tds[valueIndex].text
                    val nameText = tds[nameIndex].text
                    val descriptionText = if (descriptionIndex >= 0) tds[descriptionIndex].text else null
                    ValueEnumItem(nameText, valueText, descriptionText)
                }
            }
        )
    }


}

private fun <T> List<T>.findIndex(items: List<T>): Int {
    for (item in items) {
        val index = this.indexOf(item)
        if (index >= 0)
            return index
    }
    return -1
}

private fun <T> List<T>.findIndex(vararg items: T): Int {
    for (item in items) {
        val index = this.indexOf(item)
        if (index >= 0)
            return index
    }
    return -1
}

object EnumResourceHandler : ResourceHandler<EnumResource>() {
    override fun handle(name: String, columns: List<String>, element: WebElement): EnumResource {
        val nameIndex = columns.indexOf("feature")
        val descriptionIndex = columns.indexOf("description")
        return EnumResource(
            name = name,
            items = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { tr ->
                tr.findElements(By.tagName("td")).let { tds ->
                    val nameText = tds[nameIndex].text
                    val descriptionText = if (descriptionIndex >= 0) tds[descriptionIndex].text else null
                    EnumItem(nameText, descriptionText)
                }
            }
        )
    }


    private fun findIndex(columns: List<String>, names: List<String>): Int {
        for (name in names) {
            val index = columns.indexOf(name)
            if (index >= 0)
                return index
        }
        return -1
    }

}
