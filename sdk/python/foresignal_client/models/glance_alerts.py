from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.glance_alerts_latest_item import GlanceAlertsLatestItem
    from ..models.glance_alerts_open import GlanceAlertsOpen


T = TypeVar("T", bound="GlanceAlerts")


@_attrs_define
class GlanceAlerts:
    """Open alerts

    Attributes:
        open_ (GlanceAlertsOpen | Unset): Unresolved and unacknowledged per severity
        latest (list[GlanceAlertsLatestItem] | Unset):
    """

    open_: GlanceAlertsOpen | Unset = UNSET
    latest: list[GlanceAlertsLatestItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        open_: dict[str, Any] | Unset = UNSET
        if not isinstance(self.open_, Unset):
            open_ = self.open_.to_dict()

        latest: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.latest, Unset):
            latest = []
            for latest_item_data in self.latest:
                latest_item = latest_item_data.to_dict()
                latest.append(latest_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if open_ is not UNSET:
            field_dict["open"] = open_
        if latest is not UNSET:
            field_dict["latest"] = latest

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.glance_alerts_latest_item import GlanceAlertsLatestItem
        from ..models.glance_alerts_open import GlanceAlertsOpen

        d = dict(src_dict)
        _open_ = d.pop("open", UNSET)
        open_: GlanceAlertsOpen | Unset
        if isinstance(_open_, Unset):
            open_ = UNSET
        else:
            open_ = GlanceAlertsOpen.from_dict(_open_)

        _latest = d.pop("latest", UNSET)
        latest: list[GlanceAlertsLatestItem] | Unset = UNSET
        if _latest is not UNSET:
            latest = []
            for latest_item_data in _latest:
                latest_item = GlanceAlertsLatestItem.from_dict(latest_item_data)

                latest.append(latest_item)

        glance_alerts = cls(
            open_=open_,
            latest=latest,
        )

        glance_alerts.additional_properties = d
        return glance_alerts

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
