from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpForecastItemQuestionType0")


@_attrs_define
class EdpForecastItemQuestionType0:
    """
    Attributes:
        external_id (str):
        title (str):
        close_time (datetime.datetime | None | Unset):
        resolution_criteria (None | str | Unset):
    """

    external_id: str
    title: str
    close_time: datetime.datetime | None | Unset = UNSET
    resolution_criteria: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        external_id = self.external_id

        title = self.title

        close_time: None | str | Unset
        if isinstance(self.close_time, Unset):
            close_time = UNSET
        elif isinstance(self.close_time, datetime.datetime):
            close_time = self.close_time.isoformat()
        else:
            close_time = self.close_time

        resolution_criteria: None | str | Unset
        if isinstance(self.resolution_criteria, Unset):
            resolution_criteria = UNSET
        else:
            resolution_criteria = self.resolution_criteria

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "externalId": external_id,
                "title": title,
            }
        )
        if close_time is not UNSET:
            field_dict["closeTime"] = close_time
        if resolution_criteria is not UNSET:
            field_dict["resolutionCriteria"] = resolution_criteria

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        external_id = d.pop("externalId")

        title = d.pop("title")

        def _parse_close_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                close_time_type_0 = datetime.datetime.fromisoformat(data)

                return close_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        close_time = _parse_close_time(d.pop("closeTime", UNSET))

        def _parse_resolution_criteria(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolution_criteria = _parse_resolution_criteria(
            d.pop("resolutionCriteria", UNSET)
        )

        edp_forecast_item_question_type_0 = cls(
            external_id=external_id,
            title=title,
            close_time=close_time,
            resolution_criteria=resolution_criteria,
        )

        edp_forecast_item_question_type_0.additional_properties = d
        return edp_forecast_item_question_type_0

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
