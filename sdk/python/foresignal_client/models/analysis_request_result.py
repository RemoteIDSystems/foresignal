from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalysisRequestResult")


@_attrs_define
class AnalysisRequestResult:
    """The outcome of an on-demand analysis request or callback (155).

    Attributes:
        correlation_id (str): The id sent to the provider and echoed in its callback.
        recorded (bool): True when a forecast was recorded at once (a synchronous provider, or a callback that
            resolved).
        forecast_id (str | Unset): The recorded forecast's id, present when recorded is true via the callback.
    """

    correlation_id: str
    recorded: bool
    forecast_id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        correlation_id = self.correlation_id

        recorded = self.recorded

        forecast_id = self.forecast_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "correlationId": correlation_id,
                "recorded": recorded,
            }
        )
        if forecast_id is not UNSET:
            field_dict["forecastId"] = forecast_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        correlation_id = d.pop("correlationId")

        recorded = d.pop("recorded")

        forecast_id = d.pop("forecastId", UNSET)

        analysis_request_result = cls(
            correlation_id=correlation_id,
            recorded=recorded,
            forecast_id=forecast_id,
        )

        analysis_request_result.additional_properties = d
        return analysis_request_result

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
