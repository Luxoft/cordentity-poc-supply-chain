package com.luxoft.supplychain.sovrinagentapp.communcations


import com.luxoft.supplychain.sovrinagentapp.data.AskForPackageRequest
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.data.Serial
import com.squareup.okhttp.Response
import retrofit.http.*
import rx.Observable


interface SovrinAgentService  {

    @GET("/api/sa/claim/list")
    fun getClaims(): Observable<List<String>>


    @GET("/api/sa/package/list")
    fun getPackages(): Observable<List<Product>>

    @POST("/api/sa/request/create")
    fun createRequest(@Body tcname: AskForPackageRequest): Observable<String>

    @POST("/api/sa/package/withdraw")
    fun collectPackage(@Body serial: Serial): Observable<Response>

}
