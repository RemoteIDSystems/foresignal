from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystCursor")


@_attrs_define
class AnalystCursor:
    """Change-detection cursor of a profile

    Attributes:
        seq (int | None | Unset):
        alert_id (int | None | Unset):
        event_updated_at (datetime.datetime | None | Unset):
        anomaly_id (int | None | Unset):
    """

    seq: int | None | Unset = UNSET
    alert_id: int | None | Unset = UNSET
    event_updated_at: datetime.datetime | None | Unset = UNSET
    anomaly_id: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        event_updated_at: None | str | Unset
        if isinstance(self.event_updated_at, Unset):
            event_updated_at = UNSET
        elif isinstance(self.event_updated_at, datetime.datetime):
            event_updated_at = self.event_updated_at.isoformat()
        else:
            event_updated_at = self.event_updated_at

        anomaly_id: int | None | Unset
        if isinstance(self.anomaly_id, Unset):
            anomaly_id = UNSET
        else:
            anomaly_id = self.anomaly_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if seq is not UNSET:
            field_dict["seq"] = seq
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if event_updated_at is not UNSET:
            field_dict["eventUpdatedAt"] = event_updated_at
        if anomaly_id is not UNSET:
            field_dict["anomalyId"] = anomaly_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_event_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                event_updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return event_updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        event_updated_at = _parse_event_updated_at(d.pop("eventUpdatedAt", UNSET))

        def _parse_anomaly_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        anomaly_id = _parse_anomaly_id(d.pop("anomalyId", UNSET))

        analyst_cursor = cls(
            seq=seq,
            alert_id=alert_id,
            event_updated_at=event_updated_at,
            anomaly_id=anomaly_id,
        )

        analyst_cursor.additional_properties = d
        return analyst_cursor

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
