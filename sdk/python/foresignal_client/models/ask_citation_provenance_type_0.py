from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="AskCitationProvenanceType0")


@_attrs_define
class AskCitationProvenanceType0:
    """Briefing-run citations of a briefing with `includeProvenance` only: why the item is in the edition (NP-007); absent
    elsewhere

        Attributes:
            refs (list[str]): The cross-linked evidence and objects that put the item in scope
            reason (None | str | Unset): A one-line "why included"
    """

    refs: list[str]
    reason: None | str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        refs = self.refs

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "refs": refs,
            }
        )
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        refs = cast(list[str], d.pop("refs"))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        ask_citation_provenance_type_0 = cls(
            refs=refs,
            reason=reason,
        )

        return ask_citation_provenance_type_0
