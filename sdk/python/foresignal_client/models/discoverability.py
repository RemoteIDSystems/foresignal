from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.discoverability_discoverability import DiscoverabilityDiscoverability
from ..models.discoverability_max_allowed import DiscoverabilityMaxAllowed
from ..models.tlp import Tlp
from ..types import UNSET, Unset

T = TypeVar("T", bound="Discoverability")


@_attrs_define
class Discoverability:
    """A chronicle's discoverability setting (134); TLP caps it (RED → none; AMBER+STRICT → none | instance)

    Attributes:
        discoverability (DiscoverabilityDiscoverability): The effective setting (the owner's choice capped by the TLP
            label) in responses; the requested one in requests
        reveal_title (bool | Unset):  Default: False.
        reveal_owner (bool | Unset):  Default: False.
        max_allowed (DiscoverabilityMaxAllowed | Unset): Highest setting the chronicle's TLP allows
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        updated_at (datetime.datetime | None | Unset):
    """

    discoverability: DiscoverabilityDiscoverability
    reveal_title: bool | Unset = False
    reveal_owner: bool | Unset = False
    max_allowed: DiscoverabilityMaxAllowed | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        discoverability = self.discoverability.value

        reveal_title = self.reveal_title

        reveal_owner = self.reveal_owner

        max_allowed: str | Unset = UNSET
        if not isinstance(self.max_allowed, Unset):
            max_allowed = self.max_allowed.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "discoverability": discoverability,
            }
        )
        if reveal_title is not UNSET:
            field_dict["revealTitle"] = reveal_title
        if reveal_owner is not UNSET:
            field_dict["revealOwner"] = reveal_owner
        if max_allowed is not UNSET:
            field_dict["maxAllowed"] = max_allowed
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        discoverability = DiscoverabilityDiscoverability(d.pop("discoverability"))

        reveal_title = d.pop("revealTitle", UNSET)

        reveal_owner = d.pop("revealOwner", UNSET)

        _max_allowed = d.pop("maxAllowed", UNSET)
        max_allowed: DiscoverabilityMaxAllowed | Unset
        if isinstance(_max_allowed, Unset):
            max_allowed = UNSET
        else:
            max_allowed = DiscoverabilityMaxAllowed(_max_allowed)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        discoverability = cls(
            discoverability=discoverability,
            reveal_title=reveal_title,
            reveal_owner=reveal_owner,
            max_allowed=max_allowed,
            tlp=tlp,
            updated_at=updated_at,
        )

        discoverability.additional_properties = d
        return discoverability

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
