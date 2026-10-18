from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.update_token_body_licence_policy_type_0_item import (
    UpdateTokenBodyLicencePolicyType0Item,
)

T = TypeVar("T", bound="UpdateTokenBody")


@_attrs_define
class UpdateTokenBody:
    """
    Attributes:
        licence_policy (list[UpdateTokenBodyLicencePolicyType0Item] | None): Licence policy flags (109); null or empty
            clears the floor
    """

    licence_policy: list[UpdateTokenBodyLicencePolicyType0Item] | None

    def to_dict(self) -> dict[str, Any]:
        licence_policy: list[str] | None
        if isinstance(self.licence_policy, list):
            licence_policy = []
            for licence_policy_type_0_item_data in self.licence_policy:
                licence_policy_type_0_item = licence_policy_type_0_item_data.value
                licence_policy.append(licence_policy_type_0_item)

        else:
            licence_policy = self.licence_policy

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "licencePolicy": licence_policy,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_licence_policy(
            data: object,
        ) -> list[UpdateTokenBodyLicencePolicyType0Item] | None:
            if data is None:
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                licence_policy_type_0 = []
                _licence_policy_type_0 = data
                for licence_policy_type_0_item_data in _licence_policy_type_0:
                    licence_policy_type_0_item = UpdateTokenBodyLicencePolicyType0Item(
                        licence_policy_type_0_item_data
                    )

                    licence_policy_type_0.append(licence_policy_type_0_item)

                return licence_policy_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[UpdateTokenBodyLicencePolicyType0Item] | None, data)

        licence_policy = _parse_licence_policy(d.pop("licencePolicy"))

        update_token_body = cls(
            licence_policy=licence_policy,
        )

        return update_token_body
