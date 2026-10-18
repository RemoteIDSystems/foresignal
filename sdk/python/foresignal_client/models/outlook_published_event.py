from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.outlook_window import OutlookWindow


T = TypeVar("T", bound="OutlookPublishedEvent")


@_attrs_define
class OutlookPublishedEvent:
    """SSE payload for `outlook.published` (topic `analytics`)

    Attributes:
        region (str):
        family (str):
        published_at (datetime.datetime):
        windows (list[OutlookWindow] | Unset):
        record_ref (None | str | Unset):
    """

    region: str
    family: str
    published_at: datetime.datetime
    windows: list[OutlookWindow] | Unset = UNSET
    record_ref: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        region = self.region

        family = self.family

        published_at = self.published_at.isoformat()

        windows: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.windows, Unset):
            windows = []
            for windows_item_data in self.windows:
                windows_item = windows_item_data.to_dict()
                windows.append(windows_item)

        record_ref: None | str | Unset
        if isinstance(self.record_ref, Unset):
            record_ref = UNSET
        else:
            record_ref = self.record_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "region": region,
                "family": family,
                "publishedAt": published_at,
            }
        )
        if windows is not UNSET:
            field_dict["windows"] = windows
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.outlook_window import OutlookWindow

        d = dict(src_dict)
        region = d.pop("region")

        family = d.pop("family")

        published_at = datetime.datetime.fromisoformat(d.pop("publishedAt"))

        _windows = d.pop("windows", UNSET)
        windows: list[OutlookWindow] | Unset = UNSET
        if _windows is not UNSET:
            windows = []
            for windows_item_data in _windows:
                windows_item = OutlookWindow.from_dict(windows_item_data)

                windows.append(windows_item)

        def _parse_record_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ref = _parse_record_ref(d.pop("recordRef", UNSET))

        outlook_published_event = cls(
            region=region,
            family=family,
            published_at=published_at,
            windows=windows,
            record_ref=record_ref,
        )

        outlook_published_event.additional_properties = d
        return outlook_published_event

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
