package org.imarcus.covidswe.model.fhm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class RegionCase(
        @JsonProperty("fall") var cases: Int,
        @JsonProperty("region") var region: String,
        @JsonProperty("date") var date: LocalDate
)