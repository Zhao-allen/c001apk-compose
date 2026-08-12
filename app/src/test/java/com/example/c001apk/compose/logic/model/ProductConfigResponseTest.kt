package com.example.c001apk.compose.logic.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductConfigResponseTest {
    @Test
    fun preservesGroupsWhenFieldNamesRepeat() {
        val response = Gson().fromJson(
            """
            {
              "title": "12GB+256GB",
              "dataRows": {
                "影像": [
                  {"type":"groupData","keyName":"前置主摄参数","depth":1},
                  {"type":"itemData","data":{"title":"型号","unit":"","show_value":"A","addition_value":"1/3英寸"}},
                  {"type":"groupData","keyName":"后置主摄参数","depth":1},
                  {"type":"itemData","data":{"title":"型号","unit":"","show_value":"B","addition_value":"1/1.3英寸"}},
                  {"type":"itemData","data":{"title":"像素","unit":"万像素","show_value":5000,"addition_value":""}}
                ]
              }
            }
            """.trimIndent(),
            ProductConfigResponse::class.java,
        )

        val sections = ProductConfigData(response.title, response.dataRows.orEmpty()).specSections()
        val items = sections.single().items

        assertEquals(listOf("前置主摄参数", "后置主摄参数", "后置主摄参数"), items.map { it.group })
        assertEquals(listOf("型号", "型号", "像素"), items.map { it.name })
        assertEquals(listOf("A\n1/3英寸", "B\n1/1.3英寸", "5000万像素"), items.map { it.value })
    }
}
