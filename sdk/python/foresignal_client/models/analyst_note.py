from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_note_priority import AnalystNotePriority
from ..models.analyst_note_status import AnalystNoteStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_claim import AnalystClaim


T = TypeVar("T", bound="AnalystNote")


@_attrs_define
class AnalystNote:
    """An analyst note with typed, cited claims; carries the automated-analysis footer

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        profile_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        run_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        body (str): Markdown, ≤ 2,000 words
        priority (AnalystNotePriority):
        claims (list[AnalystClaim]):
        status (AnalystNoteStatus):
        created_at (datetime.datetime):
        citations (list[str] | Unset):
        private (bool | Unset): True when it cites anything private
        model (None | str | Unset):
        footer (str | Unset):  Example: Automated analysis by <model>. Evidence is cited; inference and speculation are
            labeled. Verify before acting..
    """

    id: str
    profile_id: str
    run_id: str
    title: str
    body: str
    priority: AnalystNotePriority
    claims: list[AnalystClaim]
    status: AnalystNoteStatus
    created_at: datetime.datetime
    citations: list[str] | Unset = UNSET
    private: bool | Unset = UNSET
    model: None | str | Unset = UNSET
    footer: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        profile_id = self.profile_id

        run_id = self.run_id

        title = self.title

        body = self.body

        priority = self.priority.value

        claims = []
        for claims_item_data in self.claims:
            claims_item = claims_item_data.to_dict()
            claims.append(claims_item)

        status = self.status.value

        created_at = self.created_at.isoformat()

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        private = self.private

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        footer = self.footer

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "profileId": profile_id,
                "runId": run_id,
                "title": title,
                "body": body,
                "priority": priority,
                "claims": claims,
                "status": status,
                "createdAt": created_at,
            }
        )
        if citations is not UNSET:
            field_dict["citations"] = citations
        if private is not UNSET:
            field_dict["private"] = private
        if model is not UNSET:
            field_dict["model"] = model
        if footer is not UNSET:
            field_dict["footer"] = footer

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_claim import AnalystClaim

        d = dict(src_dict)
        id = d.pop("id")

        profile_id = d.pop("profileId")

        run_id = d.pop("runId")

        title = d.pop("title")

        body = d.pop("body")

        priority = AnalystNotePriority(d.pop("priority"))

        claims = []
        _claims = d.pop("claims")
        for claims_item_data in _claims:
            claims_item = AnalystClaim.from_dict(claims_item_data)

            claims.append(claims_item)

        status = AnalystNoteStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        citations = cast(list[str], d.pop("citations", UNSET))

        private = d.pop("private", UNSET)

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        footer = d.pop("footer", UNSET)

        analyst_note = cls(
            id=id,
            profile_id=profile_id,
            run_id=run_id,
            title=title,
            body=body,
            priority=priority,
            claims=claims,
            status=status,
            created_at=created_at,
            citations=citations,
            private=private,
            model=model,
            footer=footer,
        )

        analyst_note.additional_properties = d
        return analyst_note

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
