from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_quota_unit import SocialQuotaUnit
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialQuota")


@_attrs_define
class SocialQuota:
    """A platform quota window and its use

    Attributes:
        unit (SocialQuotaUnit):
        window (str): ISO-8601 duration of the window (rolling or calendar) Example: P1D.
        used (int):
        limit (int | None | Unset):
        rolling (bool | Unset):
        remaining (int | None | Unset):
        resets_at (datetime.datetime | None | Unset):
        description (None | str | Unset):
    """

    unit: SocialQuotaUnit
    window: str
    used: int
    limit: int | None | Unset = UNSET
    rolling: bool | Unset = UNSET
    remaining: int | None | Unset = UNSET
    resets_at: datetime.datetime | None | Unset = UNSET
    description: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        unit = self.unit.value

        window = self.window

        used = self.used

        limit: int | None | Unset
        if isinstance(self.limit, Unset):
            limit = UNSET
        else:
            limit = self.limit

        rolling = self.rolling

        remaining: int | None | Unset
        if isinstance(self.remaining, Unset):
            remaining = UNSET
        else:
            remaining = self.remaining

        resets_at: None | str | Unset
        if isinstance(self.resets_at, Unset):
            resets_at = UNSET
        elif isinstance(self.resets_at, datetime.datetime):
            resets_at = self.resets_at.isoformat()
        else:
            resets_at = self.resets_at

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "unit": unit,
                "window": window,
                "used": used,
            }
        )
        if limit is not UNSET:
            field_dict["limit"] = limit
        if rolling is not UNSET:
            field_dict["rolling"] = rolling
        if remaining is not UNSET:
            field_dict["remaining"] = remaining
        if resets_at is not UNSET:
            field_dict["resetsAt"] = resets_at
        if description is not UNSET:
            field_dict["description"] = description

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        unit = SocialQuotaUnit(d.pop("unit"))

        window = d.pop("window")

        used = d.pop("used")

        def _parse_limit(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        limit = _parse_limit(d.pop("limit", UNSET))

        rolling = d.pop("rolling", UNSET)

        def _parse_remaining(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        remaining = _parse_remaining(d.pop("remaining", UNSET))

        def _parse_resets_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                resets_at_type_0 = datetime.datetime.fromisoformat(data)

                return resets_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        resets_at = _parse_resets_at(d.pop("resetsAt", UNSET))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        social_quota = cls(
            unit=unit,
            window=window,
            used=used,
            limit=limit,
            rolling=rolling,
            remaining=remaining,
            resets_at=resets_at,
            description=description,
        )

        social_quota.additional_properties = d
        return social_quota

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
