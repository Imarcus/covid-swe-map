package org.imarcus.covidswe

import org.imarcus.covidswe.model.fhm.CasesData
import org.imarcus.covidswe.model.response.CasesResponse
import org.imarcus.covidswe.model.response.RegionCases
import org.json.JSONObject
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.lang.IllegalStateException
import java.math.BigInteger
import java.time.LocalDate
import java.time.temporal.TemporalField
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.collections.HashMap


@Service
class CovidMapService {

    var restTemplate = RestTemplate()

    companion object {
        const val SVT_DATA_URL = "https://www.svt.se/special/articledata/2322/fohm_covid.json"
        val WEEK_TEMPORAL_FIELD: TemporalField = WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear()
        val START_OF_WEEK_ONE_2021: LocalDate = LocalDate.of(2021, 1, 4)
    }

    @Cacheable("cases")
    fun getCases(): CasesResponse {
        println("Getting cases")
        val regionCasesList = getRegionCases()
        val maxWeek = regionCasesList[0].casesPerWeek.keys.stream().max(Integer::compare).get()
        val minWeek = regionCasesList[0].casesPerWeek.keys.stream().min(Integer::compare).get()
        val sortedRegionCasesList = regionCasesList.sortedBy { regionCase -> regionCase.regionName  }
        return CasesResponse(minWeek, maxWeek, sortedRegionCasesList)
    }

    private fun getRegionCases(): ArrayList<RegionCases> {
        val casesData = getSvtData()
        val regionCasesList: ArrayList<RegionCases> = ArrayList()
        getAllRegions().forEach{ region ->
            regionCasesList.add(getCasesPerWeekForRegion(casesData, region))
        }
        return regionCasesList;
    }

    private fun getCasesPerWeekForRegion(casesData:CasesData, region: String):RegionCases {
        val weekMap = HashMap<Int, Int>()
        casesData.casesPerRegion
                .filter { case -> case.region == region }
                .forEach { dateCases ->
                    val weekNr =
                            if (dateCases.date.isBefore(START_OF_WEEK_ONE_2021))
                                dateCases.date.get(WEEK_TEMPORAL_FIELD)
                            else
                                dateCases.date.get(WEEK_TEMPORAL_FIELD) + 53
                    weekMap.merge(weekNr, dateCases.cases) { old, new -> old + new }
                }
        return RegionCases(region, weekMap)
    }

    private fun getAllRegions(): List<String> {
        return getRegions().getJSONArray("features")
                .map { f -> (f as JSONObject).getJSONObject("properties") }
                .filter { props -> props is JSONObject }
                .map { prop -> prop.getString("name") }
    }

    private fun getSvtData(): CasesData {
        return restTemplate.getForEntity(SVT_DATA_URL, CasesData::class.java).body
                ?: throw IllegalStateException("Failed to fetch svt data")
    }

    @Cacheable("regions")
    fun getRegions(): JSONObject {
        return JSONObject(this::class.java.classLoader.getResource("static/sweden-counties.geojson")?.readText())
    }

    @CacheEvict(allEntries = true, value = ["cases"])
    @Scheduled(initialDelay = 3600000, fixedDelay = 3600000)
    fun evictCasesCache() {
        println("Evicting cases cache")
    }
}