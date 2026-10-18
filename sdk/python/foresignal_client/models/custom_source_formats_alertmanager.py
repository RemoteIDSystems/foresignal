from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.custom_source_formats_alertmanager_resolved import (
    CustomSourceFormatsAlertmanagerResolved,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="CustomSourceFormatsAlertmanager")


@_attrs_define
class CustomSourceFormatsAlertmanager:
    """Alertmanager v4 webhook options; `severity` and `alertname` labels are also copied to top-level payload fields (059)

    Attributes:
        kind_label (None | str | Unset): Label whose value becomes the kind; else the source's single kind
        resolved (CustomSourceFormatsAlertmanagerResolved | Unset):  Default:
            CustomSourceFormatsAlertmanagerResolved.TOMBSTONE.
        receiver_prefix (bool | Unset): externalId `{receiver}:{fingerprint}` instead of the fingerprint (059,
            alertmanager preset) Default: False.
        resolved_valid_for (None | str | Unset): With resolved=record: validUntil = endsAt + this duration, e.g. `1h`
            (059) Example: 1h.
        station (list[float] | None | Unset): [lon, lat] used as the geometry of alerts without location labels (059)
    """

    kind_label: None | str | Unset = UNSET
    resolved: CustomSourceFormatsAlertmanagerResolved | Unset = (
        CustomSourceFormatsAlertmanagerResolved.TOMBSTONE
    )
    receiver_prefix: bool | Unset = False
    resolved_valid_for: None | str | Unset = UNSET
    station: list[float] | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind_label: None | str | Unset
        if isinstance(self.kind_label, Unset):
            kind_label = UNSET
        else:
            kind_label = self.kind_label

        resolved: str | Unset = UNSET
        if not isinstance(self.resolved, Unset):
            resolved = self.resolved.value

        receiver_prefix = self.receiver_prefix

        resolved_valid_for: None | str | Unset
        if isinstance(self.resolved_valid_for, Unset):
            resolved_valid_for = UNSET
        else:
            resolved_valid_for = self.resolved_valid_for

        station: list[float] | None | Unset
        if isinstance(self.station, Unset):
            station = UNSET
        elif isinstance(self.station, list):
            station = self.station

        else:
            station = self.station

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if kind_label is not UNSET:
            field_dict["kindLabel"] = kind_label
        if resolved is not UNSET:
            field_dict["resolved"] = resolved
        if receiver_prefix is not UNSET:
            field_dict["receiverPrefix"] = receiver_prefix
        if resolved_valid_for is not UNSET:
            field_dict["resolvedValidFor"] = resolved_valid_for
        if station is not UNSET:
            field_dict["station"] = station

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_kind_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind_label = _parse_kind_label(d.pop("kindLabel", UNSET))

        _resolved = d.pop("resolved", UNSET)
        resolved: CustomSourceFormatsAlertmanagerResolved | Unset
        if isinstance(_resolved, Unset):
            resolved = UNSET
        else:
            resolved = CustomSourceFormatsAlertmanagerResolved(_resolved)

        receiver_prefix = d.pop("receiverPrefix", UNSET)

        def _parse_resolved_valid_for(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolved_valid_for = _parse_resolved_valid_for(d.pop("resolvedValidFor", UNSET))

        def _parse_station(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                station_type_0 = cast(list[float], data)

                return station_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        station = _parse_station(d.pop("station", UNSET))

        custom_source_formats_alertmanager = cls(
            kind_label=kind_label,
            resolved=resolved,
            receiver_prefix=receiver_prefix,
            resolved_valid_for=resolved_valid_for,
            station=station,
        )

        custom_source_formats_alertmanager.additional_properties = d
        return custom_source_formats_alertmanager

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
