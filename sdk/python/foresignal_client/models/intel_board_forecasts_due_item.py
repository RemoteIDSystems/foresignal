from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelBoardForecastsDueItem")


@_attrs_define
class IntelBoardForecastsDueItem:
    """Question due in the next 7 days

    Attributes:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        due_at (datetime.datetime):
        probability (float | None | Unset):
        estimative_term (IcdTerm | None | Unset):
    """

    question_id: str
    title: str
    due_at: datetime.datetime
    probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        question_id = self.question_id

        title = self.title

        due_at = self.due_at.isoformat()

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        estimative_term: None | str | Unset
        if isinstance(self.estimative_term, Unset):
            estimative_term = UNSET
        elif isinstance(self.estimative_term, IcdTerm):
            estimative_term = self.estimative_term.value
        else:
            estimative_term = self.estimative_term

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "questionId": question_id,
                "title": title,
                "dueAt": due_at,
            }
        )
        if probability is not UNSET:
            field_dict["probability"] = probability
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        question_id = d.pop("questionId")

        title = d.pop("title")

        due_at = datetime.datetime.fromisoformat(d.pop("dueAt"))

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_estimative_term(data: object) -> IcdTerm | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                estimative_term_type_0 = IcdTerm(data)

                return estimative_term_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IcdTerm | None | Unset, data)

        estimative_term = _parse_estimative_term(d.pop("estimativeTerm", UNSET))

        intel_board_forecasts_due_item = cls(
            question_id=question_id,
            title=title,
            due_at=due_at,
            probability=probability,
            estimative_term=estimative_term,
        )

        intel_board_forecasts_due_item.additional_properties = d
        return intel_board_forecasts_due_item

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
