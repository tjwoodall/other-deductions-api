/*
 * Copyright 2024 HM Revenue & Customs
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

package common.errors

import play.api.http.Status.BAD_REQUEST
import shared.models.errors.MtdError

object CustomerReferenceFormatError extends MtdError("FORMAT_CUSTOMER_REFERENCE", "The provided customer reference is not valid", BAD_REQUEST)

object NameOfShipFormatError extends MtdError("FORMAT_NAME_OF_SHIP", "The provided name of ship is not valid", BAD_REQUEST)

object DateFormatError extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD", BAD_REQUEST)

object OutsideAmendmentWindowError extends MtdError("RULE_OUTSIDE_AMENDMENT_WINDOW", "You are outside the amendment window", BAD_REQUEST)

object RangeToDateBeforeFromDateError
  extends MtdError(code = "RANGE_TO_DATE_BEFORE_FROM_DATE", message = "The To date is before the From date", BAD_REQUEST)