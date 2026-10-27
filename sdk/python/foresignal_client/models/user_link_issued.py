from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="UserLinkIssued")


@_attrs_define
class UserLinkIssued:
    """A one-time activation or reset link issued for a user (R2-005). When the email was not sent (no SMTP, or
    `sendInvite: false`) the plaintext `token` is returned here once for the admin to pass on; when it was emailed,
    `token` is null. Only the token's hash is stored.

        Attributes:
            kind (str):
            emailed (bool): True when the email left; the token is then withheld
            token_prefix (str): The token's first eight characters
            expires_at (datetime.datetime):
            token (None | str | Unset): The one-time token (`fsv_…` activation, `fsw_…` reset), only when not emailed
    """

    kind: str
    emailed: bool
    token_prefix: str
    expires_at: datetime.datetime
    token: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind

        emailed = self.emailed

        token_prefix = self.token_prefix

        expires_at = self.expires_at.isoformat()

        token: None | str | Unset
        if isinstance(self.token, Unset):
            token = UNSET
        else:
            token = self.token

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "emailed": emailed,
                "tokenPrefix": token_prefix,
                "expiresAt": expires_at,
            }
        )
        if token is not UNSET:
            field_dict["token"] = token

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = d.pop("kind")

        emailed = d.pop("emailed")

        token_prefix = d.pop("tokenPrefix")

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        def _parse_token(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        token = _parse_token(d.pop("token", UNSET))

        user_link_issued = cls(
            kind=kind,
            emailed=emailed,
            token_prefix=token_prefix,
            expires_at=expires_at,
            token=token,
        )

        user_link_issued.additional_properties = d
        return user_link_issued

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
