/*
 * Copyright 2023 HM Revenue & Customs
 *
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
 */

package v1.connectors

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveOtherDeductions.RetrieveOtherDeductionsRequestData
import v1.models.response.retrieveOtherDeductions.RetrieveOtherDeductionsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherDeductionsConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveOtherDeductionsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveOtherDeductionsResponse]] = {

    import request._

    val url = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveOtherDeductionsResponse](s"income-tax/deductions/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[RetrieveOtherDeductionsResponse](s"income-tax/deductions/$nino/${taxYear.asMtd}")
    }

    get(url)
  }

}
