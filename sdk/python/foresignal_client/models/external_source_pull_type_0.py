from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalSourcePullType0")


@_attrs_define
class ExternalSourcePullType0:
    """
    Attributes:
        cursor (None | str | Unset):
        last_poll_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
    """

    cursor: None | str | Unset = UNSET
    last_poll_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cursor: None | str | Unset
        if isinstance(self.cursor, Unset):
            cursor = UNSET
        else:
            cursor = self.cursor

        last_poll_at: None | str | Unset
        if isinstance(self.last_poll_at, Unset):
            last_poll_at = UNSET
        elif isinstance(self.last_poll_at, datetime.datetime):
            last_poll_at = self.last_poll_at.isoformat()
        else:
            last_poll_at = self.last_poll_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cursor is not UNSET:
            field_dict["cursor"] = cursor
        if last_poll_at is not UNSET:
            field_dict["lastPollAt"] = last_poll_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cursor = _parse_cursor(d.pop("cursor", UNSET))

        def _parse_last_poll_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_poll_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_poll_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_poll_at = _parse_last_poll_at(d.pop("lastPollAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        external_source_pull_type_0 = cls(
            cursor=cursor,
            last_poll_at=last_poll_at,
            last_error=last_error,
        )

        external_source_pull_type_0.additional_properties = d
        return external_source_pull_type_0

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
