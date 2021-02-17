package org.imarcus.covidswe.model.response

data class RegionCases(
        val regionName: String,
        val casesPerWeek: HashMap<Int, Int>
)