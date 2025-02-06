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

package v2.controllers.validators

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.models.request.deleteOtherDeductions.DeleteOtherDeductionsRequestData

trait MockDeleteOtherDeductionsValidatorFactory extends MockFactory {

  val mockDeleteOtherDeductionsValidatorFactory: DeleteOtherDeductionsValidatorFactory = mock[DeleteOtherDeductionsValidatorFactory]

  object MockedDeleteOtherDeductionsValidatorFactory {

    def validator(): CallHandler[Validator[DeleteOtherDeductionsRequestData]] =
      (mockDeleteOtherDeductionsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeleteOtherDeductionsRequestData]): CallHandler[Validator[DeleteOtherDeductionsRequestData]] = {
    MockedDeleteOtherDeductionsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteOtherDeductionsRequestData): Validator[DeleteOtherDeductionsRequestData] =
    new Validator[DeleteOtherDeductionsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteOtherDeductionsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteOtherDeductionsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteOtherDeductionsRequestData] =
    new Validator[DeleteOtherDeductionsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteOtherDeductionsRequestData] = Invalid(result)
    }

}
