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

package common.controllers.validators.resolvers;

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import shared.controllers.validators.resolvers.ResolverSupport
import shared.models.domain.DateRange
import shared.models.errors.{EndDateFormatError, MtdError, StartDateFormatError}

import java.time.LocalDate
import scala.math.Ordering.Implicits.infixOrderingOps

object ResolveDateRange extends ResolverSupport {

  def validateRange(parsedStartDate: LocalDate, parsedEndDate: LocalDate, endBeforeStartDateError: MtdError): Validated[Seq[MtdError], DateRange] =
    if (parsedEndDate < parsedStartDate)
      Invalid(List(endBeforeStartDateError))
    else
      Valid(DateRange(parsedStartDate, parsedEndDate))

  def validateMaxAndMinDate(minYear: Int, maxYear: Int, value: DateRange): Validated[Seq[MtdError], DateRange] = {
    val validatedFromDate: Validated[List[MtdError], Unit] =
      if (value.startDate.getYear < minYear) Invalid(List(StartDateFormatError)) else Valid(())

    val validatedToDate: Validated[List[MtdError], Unit] =
      if (value.endDate.getYear >= maxYear) Invalid(List(EndDateFormatError)) else Valid(())

    List(validatedFromDate, validatedToDate).sequence_
      .map(_ => value)
  }

}
