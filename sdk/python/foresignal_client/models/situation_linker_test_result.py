from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_link_decision import SituationLinkDecision


T = TypeVar("T", bound="SituationLinkerTestResult")


@_attrs_define
class SituationLinkerTestResult:
    """Would-be links from a linker dry run; nothing is stored

    Attributes:
        from_seq (int):
        to_seq (int):
        linker_version (str):
        decisions (list[SituationLinkDecision]):
        inputs_considered (int | Unset):
        truncated (bool | Unset): True when the decision list was capped
    """

    from_seq: int
    to_seq: int
    linker_version: str
    decisions: list[SituationLinkDecision]
    inputs_considered: int | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_seq = self.from_seq

        to_seq = self.to_seq

        linker_version = self.linker_version

        decisions = []
        for decisions_item_data in self.decisions:
            decisions_item = decisions_item_data.to_dict()
            decisions.append(decisions_item)

        inputs_considered = self.inputs_considered

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fromSeq": from_seq,
                "toSeq": to_seq,
                "linkerVersion": linker_version,
                "decisions": decisions,
            }
        )
        if inputs_considered is not UNSET:
            field_dict["inputsConsidered"] = inputs_considered
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_link_decision import SituationLinkDecision

        d = dict(src_dict)
        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        linker_version = d.pop("linkerVersion")

        decisions = []
        _decisions = d.pop("decisions")
        for decisions_item_data in _decisions:
            decisions_item = SituationLinkDecision.from_dict(decisions_item_data)

            decisions.append(decisions_item)

        inputs_considered = d.pop("inputsConsidered", UNSET)

        truncated = d.pop("truncated", UNSET)

        situation_linker_test_result = cls(
            from_seq=from_seq,
            to_seq=to_seq,
            linker_version=linker_version,
            decisions=decisions,
            inputs_considered=inputs_considered,
            truncated=truncated,
        )

        situation_linker_test_result.additional_properties = d
        return situation_linker_test_result

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
