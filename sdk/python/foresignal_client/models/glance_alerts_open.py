from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GlanceAlertsOpen")


@_attrs_define
class GlanceAlertsOpen:
    """Unresolved and unacknowledged per severity

    Attributes:
        critical (int | Unset):
        warning (int | Unset):
        info (int | Unset):
    """

    critical: int | Unset = UNSET
    warning: int | Unset = UNSET
    info: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        critical = self.critical

        warning = self.warning

        info = self.info

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if critical is not UNSET:
            field_dict["critical"] = critical
        if warning is not UNSET:
            field_dict["warning"] = warning
        if info is not UNSET:
            field_dict["info"] = info

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        critical = d.pop("critical", UNSET)

        warning = d.pop("warning", UNSET)

        info = d.pop("info", UNSET)

        glance_alerts_open = cls(
            critical=critical,
            warning=warning,
            info=info,
        )

        glance_alerts_open.additional_properties = d
        return glance_alerts_open

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
