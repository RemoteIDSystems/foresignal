from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.access_request_grant_type_0_delivered_as import (
    AccessRequestGrantType0DeliveredAs,
)
from ..models.access_request_grant_type_0_role_type_1 import (
    AccessRequestGrantType0RoleType1,
)
from ..models.access_request_grant_type_0_role_type_2_type_1 import (
    AccessRequestGrantType0RoleType2Type1,
)
from ..models.access_request_grant_type_0_role_type_3_type_1 import (
    AccessRequestGrantType0RoleType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="AccessRequestGrantType0")


@_attrs_define
class AccessRequestGrantType0:
    """How access was granted

    Attributes:
        delivered_as (AccessRequestGrantType0DeliveredAs):
        role (AccessRequestGrantType0RoleType1 | AccessRequestGrantType0RoleType2Type1 |
            AccessRequestGrantType0RoleType3Type1 | None | Unset):
        package_id (None | str | Unset):
    """

    delivered_as: AccessRequestGrantType0DeliveredAs
    role: (
        AccessRequestGrantType0RoleType1
        | AccessRequestGrantType0RoleType2Type1
        | AccessRequestGrantType0RoleType3Type1
        | None
        | Unset
    ) = UNSET
    package_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        delivered_as = self.delivered_as.value

        role: None | str | Unset
        if isinstance(self.role, Unset):
            role = UNSET
        elif (
            isinstance(self.role, AccessRequestGrantType0RoleType1)
            or isinstance(self.role, AccessRequestGrantType0RoleType2Type1)
            or isinstance(self.role, AccessRequestGrantType0RoleType3Type1)
        ):
            role = self.role.value
        else:
            role = self.role

        package_id: None | str | Unset
        if isinstance(self.package_id, Unset):
            package_id = UNSET
        else:
            package_id = self.package_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "deliveredAs": delivered_as,
            }
        )
        if role is not UNSET:
            field_dict["role"] = role
        if package_id is not UNSET:
            field_dict["packageId"] = package_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        delivered_as = AccessRequestGrantType0DeliveredAs(d.pop("deliveredAs"))

        def _parse_role(
            data: object,
        ) -> (
            AccessRequestGrantType0RoleType1
            | AccessRequestGrantType0RoleType2Type1
            | AccessRequestGrantType0RoleType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                role_type_1 = AccessRequestGrantType0RoleType1(data)

                return role_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                role_type_2_type_1 = AccessRequestGrantType0RoleType2Type1(data)

                return role_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                role_type_3_type_1 = AccessRequestGrantType0RoleType3Type1(data)

                return role_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AccessRequestGrantType0RoleType1
                | AccessRequestGrantType0RoleType2Type1
                | AccessRequestGrantType0RoleType3Type1
                | None
                | Unset,
                data,
            )

        role = _parse_role(d.pop("role", UNSET))

        def _parse_package_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        package_id = _parse_package_id(d.pop("packageId", UNSET))

        access_request_grant_type_0 = cls(
            delivered_as=delivered_as,
            role=role,
            package_id=package_id,
        )

        access_request_grant_type_0.additional_properties = d
        return access_request_grant_type_0

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
