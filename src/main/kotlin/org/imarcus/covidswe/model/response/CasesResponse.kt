package org.imarcus.covidswe.model.response

data class CasesResponse(
        val startWeek: Int,
        val endWeek: Int,
        val regionCases: List<RegionCases>
)