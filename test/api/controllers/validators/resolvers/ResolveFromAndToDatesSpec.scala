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

package api.controllers.validators.resolvers

import api.models.errors.{EndDateFormatError, StartDateFormatError}
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

import java.time.LocalDate

class ResolveFromAndToDatesSpec extends UnitSpec {

  private val minTaxYear = 1900
  private val maxTaxYear = 2100

  val resolveFromAndToDates = new ResolveFromAndToDates(minTaxYear, maxTaxYear)

  "ResolvePeriodIdSpec" should {
    "return no errors" when {
      "passed valid from and to dates" in {
        val fromDate = LocalDate.parse("2019-04-06")
        val toDate   = LocalDate.parse("2019-08-06")

        val result = resolveFromAndToDates(DateRange(fromDate, toDate))

        result shouldBe Valid(DateRange(fromDate, toDate))
      }

      "passed valid from and to dates equal to the minimum and maximum" in {
        val fromDate = LocalDate.parse("1900-01-01")
        val toDate   = LocalDate.parse("2099-12-31")

        val result = resolveFromAndToDates(DateRange(fromDate, toDate))

        result shouldBe Valid(DateRange(fromDate, toDate))
      }

      "passed valid from and to dates are equal" in {
        val fromDate = LocalDate.parse("2019-04-06")
        val toDate   = LocalDate.parse("2019-04-06")

        val result = resolveFromAndToDates(DateRange(fromDate, toDate))

        result shouldBe Valid(DateRange(fromDate, toDate))
      }
    }

    "return an error" when {
      "passed a fromDate less than or equal to minimumTaxYear" in {
        val fromDate = LocalDate.parse("1789-04-06")
        val toDate   = LocalDate.parse("2019-04-05")
        val result   = resolveFromAndToDates(DateRange(fromDate, toDate))
        result shouldBe Invalid(List(StartDateFormatError))
      }

      "passed a toDate greater than or equal to maximumTaxYear" in {
        val fromDate = LocalDate.parse("2020-04-06")
        val toDate   = LocalDate.parse("2178-04-05")
        val result   = resolveFromAndToDates(DateRange(fromDate, toDate))
        result shouldBe Invalid(List(EndDateFormatError))
      }

      "passed both dates that are out of range" in {
        val fromDate = LocalDate.parse("1899-04-06")
        val toDate   = LocalDate.parse("2178-04-05")
        val result   = resolveFromAndToDates(DateRange(fromDate, toDate))
        result shouldBe Invalid(List(StartDateFormatError, EndDateFormatError))
      }

    }
  }

}
