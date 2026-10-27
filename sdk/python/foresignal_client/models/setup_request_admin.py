from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="SetupRequestAdmin")


@_attrs_define
class SetupRequestAdmin:
    """The first admin to create

    Attributes:
        email (str):
        full_name (str):
        password (str):
    """

    email: str
    full_name: str
    password: str

    def to_dict(self) -> dict[str, Any]:
        email = self.email

        full_name = self.full_name

        password = self.password

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "email": email,
                "fullName": full_name,
                "password": password,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        email = d.pop("email")

        full_name = d.pop("fullName")

        password = d.pop("password")

        setup_request_admin = cls(
            email=email,
            full_name=full_name,
            password=password,
        )

        return setup_request_admin
