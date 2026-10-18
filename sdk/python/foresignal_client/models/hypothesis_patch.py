from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_patch_prior_basis import HypothesisPatchPriorBasis
    from ..models.hypothesis_patch_probability_override_type_0 import (
        HypothesisPatchProbabilityOverrideType0,
    )


T = TypeVar("T", bound="HypothesisPatch")


@_attrs_define
class HypothesisPatch:
    """JSON merge patch for a hypothesis; probability overrides are logged as revisions

    Attributes:
        label (str | Unset):
        statement (str | Unset):
        prior_basis (HypothesisPatchPriorBasis | Unset): Outside-view basis of a prior
        ledger_outcome (None | str | Unset):
        alternatives (list[str] | Unset):
        probability_override (HypothesisPatchProbabilityOverrideType0 | None | Unset):
        version (int | Unset): Optimistic lock (409 on mismatch)
    """

    label: str | Unset = UNSET
    statement: str | Unset = UNSET
    prior_basis: HypothesisPatchPriorBasis | Unset = UNSET
    ledger_outcome: None | str | Unset = UNSET
    alternatives: list[str] | Unset = UNSET
    probability_override: HypothesisPatchProbabilityOverrideType0 | None | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.hypothesis_patch_probability_override_type_0 import (
            HypothesisPatchProbabilityOverrideType0,
        )

        label = self.label

        statement = self.statement

        prior_basis: dict[str, Any] | Unset = UNSET
        if not isinstance(self.prior_basis, Unset):
            prior_basis = self.prior_basis.to_dict()

        ledger_outcome: None | str | Unset
        if isinstance(self.ledger_outcome, Unset):
            ledger_outcome = UNSET
        else:
            ledger_outcome = self.ledger_outcome

        alternatives: list[str] | Unset = UNSET
        if not isinstance(self.alternatives, Unset):
            alternatives = self.alternatives

        probability_override: dict[str, Any] | None | Unset
        if isinstance(self.probability_override, Unset):
            probability_override = UNSET
        elif isinstance(
            self.probability_override, HypothesisPatchProbabilityOverrideType0
        ):
            probability_override = self.probability_override.to_dict()
        else:
            probability_override = self.probability_override

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if label is not UNSET:
            field_dict["label"] = label
        if statement is not UNSET:
            field_dict["statement"] = statement
        if prior_basis is not UNSET:
            field_dict["priorBasis"] = prior_basis
        if ledger_outcome is not UNSET:
            field_dict["ledgerOutcome"] = ledger_outcome
        if alternatives is not UNSET:
            field_dict["alternatives"] = alternatives
        if probability_override is not UNSET:
            field_dict["probabilityOverride"] = probability_override
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_patch_prior_basis import HypothesisPatchPriorBasis
        from ..models.hypothesis_patch_probability_override_type_0 import (
            HypothesisPatchProbabilityOverrideType0,
        )

        d = dict(src_dict)
        label = d.pop("label", UNSET)

        statement = d.pop("statement", UNSET)

        _prior_basis = d.pop("priorBasis", UNSET)
        prior_basis: HypothesisPatchPriorBasis | Unset
        if isinstance(_prior_basis, Unset):
            prior_basis = UNSET
        else:
            prior_basis = HypothesisPatchPriorBasis.from_dict(_prior_basis)

        def _parse_ledger_outcome(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ledger_outcome = _parse_ledger_outcome(d.pop("ledgerOutcome", UNSET))

        alternatives = cast(list[str], d.pop("alternatives", UNSET))

        def _parse_probability_override(
            data: object,
        ) -> HypothesisPatchProbabilityOverrideType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                probability_override_type_0 = (
                    HypothesisPatchProbabilityOverrideType0.from_dict(data)
                )

                return probability_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisPatchProbabilityOverrideType0 | None | Unset, data)

        probability_override = _parse_probability_override(
            d.pop("probabilityOverride", UNSET)
        )

        version = d.pop("version", UNSET)

        hypothesis_patch = cls(
            label=label,
            statement=statement,
            prior_basis=prior_basis,
            ledger_outcome=ledger_outcome,
            alternatives=alternatives,
            probability_override=probability_override,
            version=version,
        )

        hypothesis_patch.additional_properties = d
        return hypothesis_patch

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
