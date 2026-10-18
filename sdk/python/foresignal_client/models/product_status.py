from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.health_status import HealthStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductStatus")


@_attrs_define
class ProductStatus:
    """Current product health

    Attributes:
        health (HealthStatus):
        stale (bool): Newest frame older than `maxGap`
        last_frame_at (datetime.datetime | None | Unset):
        reason (None | str | Unset): Health reason, e.g. a silent format change or a disabled decoder
    """

    health: HealthStatus
    stale: bool
    last_frame_at: datetime.datetime | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        health = self.health.value

        stale = self.stale

        last_frame_at: None | str | Unset
        if isinstance(self.last_frame_at, Unset):
            last_frame_at = UNSET
        elif isinstance(self.last_frame_at, datetime.datetime):
            last_frame_at = self.last_frame_at.isoformat()
        else:
            last_frame_at = self.last_frame_at

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "health": health,
                "stale": stale,
            }
        )
        if last_frame_at is not UNSET:
            field_dict["lastFrameAt"] = last_frame_at
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        health = HealthStatus(d.pop("health"))

        stale = d.pop("stale")

        def _parse_last_frame_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_frame_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_frame_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_frame_at = _parse_last_frame_at(d.pop("lastFrameAt", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        product_status = cls(
            health=health,
            stale=stale,
            last_frame_at=last_frame_at,
            reason=reason,
        )

        product_status.additional_properties = d
        return product_status

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
