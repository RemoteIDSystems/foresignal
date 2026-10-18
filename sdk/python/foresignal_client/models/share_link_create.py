from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.share_link_create_target_type import ShareLinkCreateTargetType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ShareLinkCreate")


@_attrs_define
class ShareLinkCreate:
    """Create a path-bound share link (112). Personal and private sources are masked by default

    Attributes:
        target_type (ShareLinkCreateTargetType):
        target_ref (str): Target id: record `{sourceId}/{externalId}`, alert id, view id, track
            `{sourceId}/{externalId}`, `{briefingId}/{runId}`, or export job id
        field_mask (list[str] | None | Unset): Payload allowlist applied after licence filtering
        precision_m (int | None | Unset): Coordinate precision cap in metres (default 1,000 for personal or private
            sources)
        expires_at (datetime.datetime | None | Unset): Default now + 7 d, maximum now + 30 d
        max_uses (int | None | Unset):
    """

    target_type: ShareLinkCreateTargetType
    target_ref: str
    field_mask: list[str] | None | Unset = UNSET
    precision_m: int | None | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    max_uses: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        target_type = self.target_type.value

        target_ref = self.target_ref

        field_mask: list[str] | None | Unset
        if isinstance(self.field_mask, Unset):
            field_mask = UNSET
        elif isinstance(self.field_mask, list):
            field_mask = self.field_mask

        else:
            field_mask = self.field_mask

        precision_m: int | None | Unset
        if isinstance(self.precision_m, Unset):
            precision_m = UNSET
        else:
            precision_m = self.precision_m

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        max_uses: int | None | Unset
        if isinstance(self.max_uses, Unset):
            max_uses = UNSET
        else:
            max_uses = self.max_uses

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "targetType": target_type,
                "targetRef": target_ref,
            }
        )
        if field_mask is not UNSET:
            field_dict["fieldMask"] = field_mask
        if precision_m is not UNSET:
            field_dict["precisionM"] = precision_m
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if max_uses is not UNSET:
            field_dict["maxUses"] = max_uses

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        target_type = ShareLinkCreateTargetType(d.pop("targetType"))

        target_ref = d.pop("targetRef")

        def _parse_field_mask(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                field_mask_type_0 = cast(list[str], data)

                return field_mask_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        field_mask = _parse_field_mask(d.pop("fieldMask", UNSET))

        def _parse_precision_m(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        precision_m = _parse_precision_m(d.pop("precisionM", UNSET))

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        def _parse_max_uses(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_uses = _parse_max_uses(d.pop("maxUses", UNSET))

        share_link_create = cls(
            target_type=target_type,
            target_ref=target_ref,
            field_mask=field_mask,
            precision_m=precision_m,
            expires_at=expires_at,
            max_uses=max_uses,
        )

        share_link_create.additional_properties = d
        return share_link_create

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
