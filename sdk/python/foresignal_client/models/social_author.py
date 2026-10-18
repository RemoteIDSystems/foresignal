from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialAuthor")


@_attrs_define
class SocialAuthor:
    """Post author as the platform returns it; accounts are reporting sources, never profiled (135)

    Attributes:
        handle (str):
        account_id (str): Platform account id
        display_name (None | str | Unset):
        account_created_at (datetime.datetime | None | Unset):
        followers (int | None | Unset):
        verified (bool | None | Unset):
    """

    handle: str
    account_id: str
    display_name: None | str | Unset = UNSET
    account_created_at: datetime.datetime | None | Unset = UNSET
    followers: int | None | Unset = UNSET
    verified: bool | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        handle = self.handle

        account_id = self.account_id

        display_name: None | str | Unset
        if isinstance(self.display_name, Unset):
            display_name = UNSET
        else:
            display_name = self.display_name

        account_created_at: None | str | Unset
        if isinstance(self.account_created_at, Unset):
            account_created_at = UNSET
        elif isinstance(self.account_created_at, datetime.datetime):
            account_created_at = self.account_created_at.isoformat()
        else:
            account_created_at = self.account_created_at

        followers: int | None | Unset
        if isinstance(self.followers, Unset):
            followers = UNSET
        else:
            followers = self.followers

        verified: bool | None | Unset
        if isinstance(self.verified, Unset):
            verified = UNSET
        else:
            verified = self.verified

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "handle": handle,
                "accountId": account_id,
            }
        )
        if display_name is not UNSET:
            field_dict["displayName"] = display_name
        if account_created_at is not UNSET:
            field_dict["accountCreatedAt"] = account_created_at
        if followers is not UNSET:
            field_dict["followers"] = followers
        if verified is not UNSET:
            field_dict["verified"] = verified

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        handle = d.pop("handle")

        account_id = d.pop("accountId")

        def _parse_display_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        display_name = _parse_display_name(d.pop("displayName", UNSET))

        def _parse_account_created_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                account_created_at_type_0 = datetime.datetime.fromisoformat(data)

                return account_created_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        account_created_at = _parse_account_created_at(d.pop("accountCreatedAt", UNSET))

        def _parse_followers(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        followers = _parse_followers(d.pop("followers", UNSET))

        def _parse_verified(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        verified = _parse_verified(d.pop("verified", UNSET))

        social_author = cls(
            handle=handle,
            account_id=account_id,
            display_name=display_name,
            account_created_at=account_created_at,
            followers=followers,
            verified=verified,
        )

        social_author.additional_properties = d
        return social_author

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
