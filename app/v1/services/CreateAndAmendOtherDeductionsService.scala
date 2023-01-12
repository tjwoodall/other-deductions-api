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

package v1.services

import cats.implicits._
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAndAmendOtherDeductionsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.createAndAmendOtherDeductions.CreateAndAmendOtherDeductionsRequest
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendOtherDeductionsService @Inject() (connector: CreateAndAmendOtherDeductionsConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAndAmend(request: CreateAndAmendOtherDeductionsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[CreateAndAmendOtherDeductionsServiceOutcome] = {

    val result = EitherT(connector.createAndAmend(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))

    result.value
  }

  private def downstreamErrorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"        -> NinoFormatError,
      "INVALID_TAX_YEAR"                 -> TaxYearFormatError,
      "INCOME_SOURCE_NOT_FOUND"          -> NotFoundError,
      "INVALID_PAYLOAD"                  -> DownstreamError,
      "INVALID_CORRELATIONID"            -> DownstreamError,
      "BUSINESS_VALIDATION_RULE_FAILURE" -> DownstreamError,
      "SERVER_ERROR"                     -> DownstreamError,
      "SERVICE_UNAVAILABLE"              -> DownstreamError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> DownstreamError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
