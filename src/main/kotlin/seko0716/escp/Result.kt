package seko0716.escp

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize


data class Result(var hits: Hits) {
    fun getData(): List<Hit> {
        return hits.hits
    }
}

data class Hits(var hits: MutableList<Hit>)

data class Hit(var _index: String, var _type: String, var _id: String,
               @JsonDeserialize(using = CustomDeserializer::class) var _source: String)

internal class CustomDeserializer : JsonDeserializer<String>() {
    override fun deserialize(jsonParser: JsonParser?, context: DeserializationContext?): String {
        if (jsonParser == null) {
            return ""
        }
        val node: TreeNode = jsonParser.readValueAsTree()
        return node.toString()
    }
}