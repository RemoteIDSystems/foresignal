from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.index_weights_input_source import IndexWeightsInputSource
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.index_weights_input_weights import IndexWeightsInputWeights


T = TypeVar("T", bound="IndexWeightsInput")


@_attrs_define
class IndexWeightsInput:
    """Apply fitted, operator or default weights (logged)

    Attributes:
        source (IndexWeightsInputSource):
        version (int | Unset): Fitted weights version (required for `fitted`)
        weights (IndexWeightsInputWeights | Unset): Required for `operator`
        rationale (None | str | Unset):
    """

    source: IndexWeightsInputSource
    version: int | Unset = UNSET
    weights: IndexWeightsInputWeights | Unset = UNSET
    rationale: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source.value

        version = self.version

        weights: dict[str, Any] | Unset = UNSET
        if not isinstance(self.weights, Unset):
            weights = self.weights.to_dict()

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
            }
        )
        if version is not UNSET:
            field_dict["version"] = version
        if weights is not UNSET:
            field_dict["weights"] = weights
        if rationale is not UNSET:
            field_dict["rationale"] = rationale

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.index_weights_input_weights import IndexWeightsInputWeights

        d = dict(src_dict)
        source = IndexWeightsInputSource(d.pop("source"))

        version = d.pop("version", UNSET)

        _weights = d.pop("weights", UNSET)
        weights: IndexWeightsInputWeights | Unset
        if isinstance(_weights, Unset):
            weights = UNSET
        else:
            weights = IndexWeightsInputWeights.from_dict(_weights)

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        index_weights_input = cls(
            source=source,
            version=version,
            weights=weights,
            rationale=rationale,
        )

        index_weights_input.additional_properties = d
        return index_weights_input

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
