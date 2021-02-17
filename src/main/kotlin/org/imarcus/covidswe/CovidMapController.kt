package org.imarcus.covidswe

import org.imarcus.covidswe.model.response.CasesResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CovidMapController(val covidMapService: CovidMapService) {

    @GetMapping("/ping", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun ping(): String {
        return "pong"
    }

    @GetMapping("/cases", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getStats(): CasesResponse {
        return covidMapService.getCases()
    }

    @GetMapping("/regions", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRegions(): String {
        return covidMapService.getRegions().toString()
    }
}