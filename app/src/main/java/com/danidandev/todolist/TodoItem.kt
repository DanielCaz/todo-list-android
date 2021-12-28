package com.danidandev.todolist

import org.json.JSONObject

data class TodoItem(val id: String, val text: String, val completed: Boolean) {
    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        jsonObject.put("text", text)
        jsonObject.put("completed", completed)
        return jsonObject
    }
}
