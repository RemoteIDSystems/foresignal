from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_prior_basis_type import HypothesisPriorBasisType
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisPriorBasis")


@_attrs_define
class HypothesisPriorBasis:
    """Outside-view basis of a prior

    Attributes:
        type_ (HypothesisPriorBasisType):
        ref (None | str | Unset): 121 base-rate id, analog set reference, or null for an operator judgment
        rationale (None | str | Unset):
        value (float | None | Unset): For a base-rate basis: the rate's pAtLeastOne when the prior was set (filled by
            the server); a later change of more than 20% relative records a base-rate-refresh revision
        refreshed_at (datetime.datetime | None | Unset): When a base-rate refresh last moved the prior
    """

    type_: HypothesisPriorBasisType
    ref: None | str | Unset = UNSET
    rationale: None | str | Unset = UNSET
    value: float | None | Unset = UNSET
    refreshed_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        refreshed_at: None | str | Unset
        if isinstance(self.refreshed_at, Unset):
            refreshed_at = UNSET
        elif isinstance(self.refreshed_at, datetime.datetime):
            refreshed_at = self.refreshed_at.isoformat()
        else:
            refreshed_at = self.refreshed_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if value is not UNSET:
            field_dict["value"] = value
        if refreshed_at is not UNSET:
            field_dict["refreshedAt"] = refreshed_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = HypothesisPriorBasisType(d.pop("type"))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_refreshed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                refreshed_at_type_0 = datetime.datetime.fromisoformat(data)

                return refreshed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        refreshed_at = _parse_refreshed_at(d.pop("refreshedAt", UNSET))

        hypothesis_prior_basis = cls(
            type_=type_,
            ref=ref,
            rationale=rationale,
            value=value,
            refreshed_at=refreshed_at,
        )

        hypothesis_prior_basis.additional_properties = d
        return hypothesis_prior_basis

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
