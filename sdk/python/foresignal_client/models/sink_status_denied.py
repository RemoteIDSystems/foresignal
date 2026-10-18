from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_status_denied_by_reason import SinkStatusDeniedByReason
    from ..models.sink_status_denied_by_source import SinkStatusDeniedBySource


T = TypeVar("T", bound="SinkStatusDenied")


@_attrs_define
class SinkStatusDenied:
    """Records refused by the egress gate

    Attributes:
        total (int):
        by_reason (SinkStatusDeniedByReason | Unset): e.g. licence-forbidden, licence-local-only, private
        by_source (SinkStatusDeniedBySource | Unset):
    """

    total: int
    by_reason: SinkStatusDeniedByReason | Unset = UNSET
    by_source: SinkStatusDeniedBySource | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        total = self.total

        by_reason: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_reason, Unset):
            by_reason = self.by_reason.to_dict()

        by_source: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_source, Unset):
            by_source = self.by_source.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "total": total,
            }
        )
        if by_reason is not UNSET:
            field_dict["byReason"] = by_reason
        if by_source is not UNSET:
            field_dict["bySource"] = by_source

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_status_denied_by_reason import SinkStatusDeniedByReason
        from ..models.sink_status_denied_by_source import SinkStatusDeniedBySource

        d = dict(src_dict)
        total = d.pop("total")

        _by_reason = d.pop("byReason", UNSET)
        by_reason: SinkStatusDeniedByReason | Unset
        if isinstance(_by_reason, Unset):
            by_reason = UNSET
        else:
            by_reason = SinkStatusDeniedByReason.from_dict(_by_reason)

        _by_source = d.pop("bySource", UNSET)
        by_source: SinkStatusDeniedBySource | Unset
        if isinstance(_by_source, Unset):
            by_source = UNSET
        else:
            by_source = SinkStatusDeniedBySource.from_dict(_by_source)

        sink_status_denied = cls(
            total=total,
            by_reason=by_reason,
            by_source=by_source,
        )

        sink_status_denied.additional_properties = d
        return sink_status_denied

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
