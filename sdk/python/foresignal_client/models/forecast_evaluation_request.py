from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastEvaluationRequest")


@_attrs_define
class ForecastEvaluationRequest:
    """Retrospective evaluation of one forecaster over past resolved questions

    Attributes:
        forecaster_id (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        categories (list[str] | Unset):
        max_questions (int | Unset):
    """

    forecaster_id: str
    from_: datetime.datetime
    to: datetime.datetime
    categories: list[str] | Unset = UNSET
    max_questions: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecaster_id = self.forecaster_id

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        categories: list[str] | Unset = UNSET
        if not isinstance(self.categories, Unset):
            categories = self.categories

        max_questions = self.max_questions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
                "from": from_,
                "to": to,
            }
        )
        if categories is not UNSET:
            field_dict["categories"] = categories
        if max_questions is not UNSET:
            field_dict["maxQuestions"] = max_questions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        categories = cast(list[str], d.pop("categories", UNSET))

        max_questions = d.pop("maxQuestions", UNSET)

        forecast_evaluation_request = cls(
            forecaster_id=forecaster_id,
            from_=from_,
            to=to,
            categories=categories,
            max_questions=max_questions,
        )

        forecast_evaluation_request.additional_properties = d
        return forecast_evaluation_request

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
