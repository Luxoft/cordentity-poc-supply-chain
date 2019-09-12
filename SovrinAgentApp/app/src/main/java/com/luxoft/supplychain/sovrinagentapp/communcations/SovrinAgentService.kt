/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.luxoft.supplychain.sovrinagentapp.communcations

import com.luxoft.supplychain.sovrinagentapp.data.AskForPackageRequest
import com.luxoft.supplychain.sovrinagentapp.data.Invite
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.data.Serial
import retrofit.http.Body
import retrofit.http.GET
import retrofit.http.POST
import retrofit.http.Path
import rx.Observable

interface SovrinAgentService {

    @GET("/api/tc/tails")
    fun getTails(): Observable<Map<String, String>>

    @POST("{url}")
    fun packageHistory(@Body serial: Serial, @Path("url") url: String): Observable<Invite>

    @GET("/api/tc/package/list")
    fun getPackages(): Observable<List<Product>>

    @POST("/api/tc/request/create")
    fun createRequest(@Body tcname: AskForPackageRequest): Observable<Unit>

    @POST("/api/tc/package/withdraw")
    fun collectPackage(@Body serial: Serial): Observable<Unit>

}
