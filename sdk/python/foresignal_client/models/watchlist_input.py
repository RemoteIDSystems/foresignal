from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.watchlist_input_identity import WatchlistInputIdentity
from ..models.watchlist_input_normalize import WatchlistInputNormalize
from ..types import UNSET, Unset

T = TypeVar("T", bound="WatchlistInput")


@_attrs_define
class WatchlistInput:
    """Create or replace a watchlist (036)

    Attributes:
        name (str):
        identity (WatchlistInputIdentity):
        normalize (WatchlistInputNormalize | Unset): Defaults: icao24/registration/callsign upper, mmsi/imo digits,
            entity/custom none
        description (None | str | Unset):
    """

    name: str
    identity: WatchlistInputIdentity
    normalize: WatchlistInputNormalize | Unset = UNSET
    description: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        identity = self.identity.value

        normalize: str | Unset = UNSET
        if not isinstance(self.normalize, Unset):
            normalize = self.normalize.value

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "identity": identity,
            }
        )
        if normalize is not UNSET:
            field_dict["normalize"] = normalize
        if description is not UNSET:
            field_dict["description"] = description

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        identity = WatchlistInputIdentity(d.pop("identity"))

        _normalize = d.pop("normalize", UNSET)
        normalize: WatchlistInputNormalize | Unset
        if isinstance(_normalize, Unset):
            normalize = UNSET
        else:
            normalize = WatchlistInputNormalize(_normalize)

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        watchlist_input = cls(
            name=name,
            identity=identity,
            normalize=normalize,
            description=description,
        )

        watchlist_input.additional_properties = d
        return watchlist_input

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
