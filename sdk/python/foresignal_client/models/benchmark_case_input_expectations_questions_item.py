from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_case_input_expectations_questions_item_type import (
    BenchmarkCaseInputExpectationsQuestionsItemType,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkCaseInputExpectationsQuestionsItem")


@_attrs_define
class BenchmarkCaseInputExpectationsQuestionsItem:
    """Question with a known resolution

    Attributes:
        text (str):
        type_ (BenchmarkCaseInputExpectationsQuestionsItemType):
        outcome (bool | float | str):
        resolved_at (datetime.datetime | None | Unset):
    """

    text: str
    type_: BenchmarkCaseInputExpectationsQuestionsItemType
    outcome: bool | float | str
    resolved_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        text = self.text

        type_ = self.type_.value

        outcome: bool | float | str
        outcome = self.outcome

        resolved_at: None | str | Unset
        if isinstance(self.resolved_at, Unset):
            resolved_at = UNSET
        elif isinstance(self.resolved_at, datetime.datetime):
            resolved_at = self.resolved_at.isoformat()
        else:
            resolved_at = self.resolved_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "text": text,
                "type": type_,
                "outcome": outcome,
            }
        )
        if resolved_at is not UNSET:
            field_dict["resolvedAt"] = resolved_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        text = d.pop("text")

        type_ = BenchmarkCaseInputExpectationsQuestionsItemType(d.pop("type"))

        def _parse_outcome(data: object) -> bool | float | str:
            return cast(bool | float | str, data)

        outcome = _parse_outcome(d.pop("outcome"))

        def _parse_resolved_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                resolved_at_type_0 = datetime.datetime.fromisoformat(data)

                return resolved_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        resolved_at = _parse_resolved_at(d.pop("resolvedAt", UNSET))

        benchmark_case_input_expectations_questions_item = cls(
            text=text,
            type_=type_,
            outcome=outcome,
            resolved_at=resolved_at,
        )

        benchmark_case_input_expectations_questions_item.additional_properties = d
        return benchmark_case_input_expectations_questions_item

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
