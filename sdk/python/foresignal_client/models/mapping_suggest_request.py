from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_sample import MappingSample
    from ..models.mapping_suggest_request_hints import MappingSuggestRequestHints


T = TypeVar("T", bound="MappingSuggestRequest")


@_attrs_define
class MappingSuggestRequest:
    """Request for an LLM-drafted mapping (059)

    Attributes:
        sample (MappingSample): A raw inbound sample for preview or suggestion; form and query inputs are converted to a
            flat JSON object of strings before mapping (059)
        hints (MappingSuggestRequestHints | Unset):
        confirm_egress (bool | Unset): Required true when the chat provider is remote (035 classification) Default:
            False.
    """

    sample: MappingSample
    hints: MappingSuggestRequestHints | Unset = UNSET
    confirm_egress: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sample = self.sample.to_dict()

        hints: dict[str, Any] | Unset = UNSET
        if not isinstance(self.hints, Unset):
            hints = self.hints.to_dict()

        confirm_egress = self.confirm_egress

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sample": sample,
            }
        )
        if hints is not UNSET:
            field_dict["hints"] = hints
        if confirm_egress is not UNSET:
            field_dict["confirmEgress"] = confirm_egress

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_sample import MappingSample
        from ..models.mapping_suggest_request_hints import MappingSuggestRequestHints

        d = dict(src_dict)
        sample = MappingSample.from_dict(d.pop("sample"))

        _hints = d.pop("hints", UNSET)
        hints: MappingSuggestRequestHints | Unset
        if isinstance(_hints, Unset):
            hints = UNSET
        else:
            hints = MappingSuggestRequestHints.from_dict(_hints)

        confirm_egress = d.pop("confirmEgress", UNSET)

        mapping_suggest_request = cls(
            sample=sample,
            hints=hints,
            confirm_egress=confirm_egress,
        )

        mapping_suggest_request.additional_properties = d
        return mapping_suggest_request

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
