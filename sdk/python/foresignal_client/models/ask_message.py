from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ask_message_role import AskMessageRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_citation import AskCitation


T = TypeVar("T", bound="AskMessage")


@_attrs_define
class AskMessage:
    """
    Attributes:
        role (AskMessageRole):
        content (str):
        created_at (datetime.datetime):
        citations (list[AskCitation] | Unset):
        warnings (list[str] | Unset): Dropped citations and uncited-claim warnings, persisted with the message
    """

    role: AskMessageRole
    content: str
    created_at: datetime.datetime
    citations: list[AskCitation] | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        role = self.role.value

        content = self.content

        created_at = self.created_at.isoformat()

        citations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = []
            for citations_item_data in self.citations:
                citations_item = citations_item_data.to_dict()
                citations.append(citations_item)

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "role": role,
                "content": content,
                "createdAt": created_at,
            }
        )
        if citations is not UNSET:
            field_dict["citations"] = citations
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_citation import AskCitation

        d = dict(src_dict)
        role = AskMessageRole(d.pop("role"))

        content = d.pop("content")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _citations = d.pop("citations", UNSET)
        citations: list[AskCitation] | Unset = UNSET
        if _citations is not UNSET:
            citations = []
            for citations_item_data in _citations:
                citations_item = AskCitation.from_dict(citations_item_data)

                citations.append(citations_item)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        ask_message = cls(
            role=role,
            content=content,
            created_at=created_at,
            citations=citations,
            warnings=warnings,
        )

        ask_message.additional_properties = d
        return ask_message

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
