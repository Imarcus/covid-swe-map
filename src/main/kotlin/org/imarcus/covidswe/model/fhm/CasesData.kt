package org.imarcus.covidswe.model.fhm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class CasesData(
        @JsonProperty("fall_per_region") var casesPerRegion: List<RegionCase>,
        @JsonProperty("fohm_pdate") var date: LocalDate
)