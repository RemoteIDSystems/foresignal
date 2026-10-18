from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PurgeReportCitingDocuments")


@_attrs_define
class PurgeReportCitingDocuments:
    """Documents citing the entity (counted, not edited)

    Attributes:
        ask_messages (int | Unset):
        briefing_runs (int | Unset):
    """

    ask_messages: int | Unset = UNSET
    briefing_runs: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ask_messages = self.ask_messages

        briefing_runs = self.briefing_runs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if ask_messages is not UNSET:
            field_dict["askMessages"] = ask_messages
        if briefing_runs is not UNSET:
            field_dict["briefingRuns"] = briefing_runs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ask_messages = d.pop("askMessages", UNSET)

        briefing_runs = d.pop("briefingRuns", UNSET)

        purge_report_citing_documents = cls(
            ask_messages=ask_messages,
            briefing_runs=briefing_runs,
        )

        purge_report_citing_documents.additional_properties = d
        return purge_report_citing_documents

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
