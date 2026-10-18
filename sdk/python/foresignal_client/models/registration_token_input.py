from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RegistrationTokenInput")


@_attrs_define
class RegistrationTokenInput:
    """Create a registration token

    Attributes:
        name (str):
        expires_at (datetime.datetime | None | Unset):
        max_uses (int | Unset):  Default: 1.
        source_id_prefix (None | str | Unset): Only manifests whose source.id starts with this prefix
    """

    name: str
    expires_at: datetime.datetime | None | Unset = UNSET
    max_uses: int | Unset = 1
    source_id_prefix: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        max_uses = self.max_uses

        source_id_prefix: None | str | Unset
        if isinstance(self.source_id_prefix, Unset):
            source_id_prefix = UNSET
        else:
            source_id_prefix = self.source_id_prefix

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
            }
        )
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if max_uses is not UNSET:
            field_dict["maxUses"] = max_uses
        if source_id_prefix is not UNSET:
            field_dict["sourceIdPrefix"] = source_id_prefix

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

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

        max_uses = d.pop("maxUses", UNSET)

        def _parse_source_id_prefix(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id_prefix = _parse_source_id_prefix(d.pop("sourceIdPrefix", UNSET))

        registration_token_input = cls(
            name=name,
            expires_at=expires_at,
            max_uses=max_uses,
            source_id_prefix=source_id_prefix,
        )

        registration_token_input.additional_properties = d
        return registration_token_input

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
