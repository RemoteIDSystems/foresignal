from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.outlet_consensus_providers_item_provider import (
    OutletConsensusProvidersItemProvider,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="OutletConsensusProvidersItem")


@_attrs_define
class OutletConsensusProvidersItem:
    """Provider value

    Attributes:
        provider (OutletConsensusProvidersItemProvider):
        value_norm (float | None | Unset):
        label (None | str | Unset):
        rated_at (datetime.datetime | None | Unset):
        licence (str | Unset):
    """

    provider: OutletConsensusProvidersItemProvider
    value_norm: float | None | Unset = UNSET
    label: None | str | Unset = UNSET
    rated_at: datetime.datetime | None | Unset = UNSET
    licence: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider.value

        value_norm: float | None | Unset
        if isinstance(self.value_norm, Unset):
            value_norm = UNSET
        else:
            value_norm = self.value_norm

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        rated_at: None | str | Unset
        if isinstance(self.rated_at, Unset):
            rated_at = UNSET
        elif isinstance(self.rated_at, datetime.datetime):
            rated_at = self.rated_at.isoformat()
        else:
            rated_at = self.rated_at

        licence = self.licence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "provider": provider,
            }
        )
        if value_norm is not UNSET:
            field_dict["valueNorm"] = value_norm
        if label is not UNSET:
            field_dict["label"] = label
        if rated_at is not UNSET:
            field_dict["ratedAt"] = rated_at
        if licence is not UNSET:
            field_dict["licence"] = licence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        provider = OutletConsensusProvidersItemProvider(d.pop("provider"))

        def _parse_value_norm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value_norm = _parse_value_norm(d.pop("valueNorm", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        def _parse_rated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                rated_at_type_0 = datetime.datetime.fromisoformat(data)

                return rated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        rated_at = _parse_rated_at(d.pop("ratedAt", UNSET))

        licence = d.pop("licence", UNSET)

        outlet_consensus_providers_item = cls(
            provider=provider,
            value_norm=value_norm,
            label=label,
            rated_at=rated_at,
            licence=licence,
        )

        outlet_consensus_providers_item.additional_properties = d
        return outlet_consensus_providers_item

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
