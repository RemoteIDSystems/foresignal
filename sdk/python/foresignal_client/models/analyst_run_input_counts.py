from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystRunInputCounts")


@_attrs_define
class AnalystRunInputCounts:
    """Inputs by type

    Attributes:
        events (int | Unset):
        anomalies (int | Unset):
        alerts (int | Unset):
    """

    events: int | Unset = UNSET
    anomalies: int | Unset = UNSET
    alerts: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        events = self.events

        anomalies = self.anomalies

        alerts = self.alerts

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if events is not UNSET:
            field_dict["events"] = events
        if anomalies is not UNSET:
            field_dict["anomalies"] = anomalies
        if alerts is not UNSET:
            field_dict["alerts"] = alerts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        events = d.pop("events", UNSET)

        anomalies = d.pop("anomalies", UNSET)

        alerts = d.pop("alerts", UNSET)

        analyst_run_input_counts = cls(
            events=events,
            anomalies=anomalies,
            alerts=alerts,
        )

        analyst_run_input_counts.additional_properties = d
        return analyst_run_input_counts

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
