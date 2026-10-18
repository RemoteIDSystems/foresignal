from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_proposal_status import AnalystProposalStatus
from ..models.analyst_proposal_type import AnalystProposalType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_proposal_payload import AnalystProposalPayload
    from ..models.chronicle_input import ChronicleInput


T = TypeVar("T", bound="AnalystProposal")


@_attrs_define
class AnalystProposal:
    """A proposed action from an analyst run; executes only on confirmation or under allowActions with an agent-scoped
    owner

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            run_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            profile_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            type_ (AnalystProposalType): Extensible enum (128 adds types)
            payload (AnalystProposalPayload): Action payload (rule input, event input, investigation item, annotation)
            status (AnalystProposalStatus):
            created_at (datetime.datetime):
            expires_at (datetime.datetime):
            rationale (str | Unset):
            citations (list[str] | Unset):
            auto_executed (bool | Unset):
            executed_ref (None | str | Unset): Reference of the created object
            decided_by (None | str | Unset):
            decided_at (datetime.datetime | None | Unset):
            reason (None | str | Unset):
            chronicle_draft (ChronicleInput | None | Unset): For `propose_chronicle`
    """

    id: str
    run_id: str
    profile_id: str
    type_: AnalystProposalType
    payload: AnalystProposalPayload
    status: AnalystProposalStatus
    created_at: datetime.datetime
    expires_at: datetime.datetime
    rationale: str | Unset = UNSET
    citations: list[str] | Unset = UNSET
    auto_executed: bool | Unset = UNSET
    executed_ref: None | str | Unset = UNSET
    decided_by: None | str | Unset = UNSET
    decided_at: datetime.datetime | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    chronicle_draft: ChronicleInput | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_input import ChronicleInput

        id = self.id

        run_id = self.run_id

        profile_id = self.profile_id

        type_ = self.type_.value

        payload = self.payload.to_dict()

        status = self.status.value

        created_at = self.created_at.isoformat()

        expires_at = self.expires_at.isoformat()

        rationale = self.rationale

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        auto_executed = self.auto_executed

        executed_ref: None | str | Unset
        if isinstance(self.executed_ref, Unset):
            executed_ref = UNSET
        else:
            executed_ref = self.executed_ref

        decided_by: None | str | Unset
        if isinstance(self.decided_by, Unset):
            decided_by = UNSET
        else:
            decided_by = self.decided_by

        decided_at: None | str | Unset
        if isinstance(self.decided_at, Unset):
            decided_at = UNSET
        elif isinstance(self.decided_at, datetime.datetime):
            decided_at = self.decided_at.isoformat()
        else:
            decided_at = self.decided_at

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        chronicle_draft: dict[str, Any] | None | Unset
        if isinstance(self.chronicle_draft, Unset):
            chronicle_draft = UNSET
        elif isinstance(self.chronicle_draft, ChronicleInput):
            chronicle_draft = self.chronicle_draft.to_dict()
        else:
            chronicle_draft = self.chronicle_draft

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "runId": run_id,
                "profileId": profile_id,
                "type": type_,
                "payload": payload,
                "status": status,
                "createdAt": created_at,
                "expiresAt": expires_at,
            }
        )
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if citations is not UNSET:
            field_dict["citations"] = citations
        if auto_executed is not UNSET:
            field_dict["autoExecuted"] = auto_executed
        if executed_ref is not UNSET:
            field_dict["executedRef"] = executed_ref
        if decided_by is not UNSET:
            field_dict["decidedBy"] = decided_by
        if decided_at is not UNSET:
            field_dict["decidedAt"] = decided_at
        if reason is not UNSET:
            field_dict["reason"] = reason
        if chronicle_draft is not UNSET:
            field_dict["chronicleDraft"] = chronicle_draft

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_proposal_payload import AnalystProposalPayload
        from ..models.chronicle_input import ChronicleInput

        d = dict(src_dict)
        id = d.pop("id")

        run_id = d.pop("runId")

        profile_id = d.pop("profileId")

        type_ = AnalystProposalType(d.pop("type"))

        payload = AnalystProposalPayload.from_dict(d.pop("payload"))

        status = AnalystProposalStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        rationale = d.pop("rationale", UNSET)

        citations = cast(list[str], d.pop("citations", UNSET))

        auto_executed = d.pop("autoExecuted", UNSET)

        def _parse_executed_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        executed_ref = _parse_executed_ref(d.pop("executedRef", UNSET))

        def _parse_decided_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        decided_by = _parse_decided_by(d.pop("decidedBy", UNSET))

        def _parse_decided_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                decided_at_type_0 = datetime.datetime.fromisoformat(data)

                return decided_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        decided_at = _parse_decided_at(d.pop("decidedAt", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_chronicle_draft(data: object) -> ChronicleInput | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                chronicle_draft_type_0 = ChronicleInput.from_dict(data)

                return chronicle_draft_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleInput | None | Unset, data)

        chronicle_draft = _parse_chronicle_draft(d.pop("chronicleDraft", UNSET))

        analyst_proposal = cls(
            id=id,
            run_id=run_id,
            profile_id=profile_id,
            type_=type_,
            payload=payload,
            status=status,
            created_at=created_at,
            expires_at=expires_at,
            rationale=rationale,
            citations=citations,
            auto_executed=auto_executed,
            executed_ref=executed_ref,
            decided_by=decided_by,
            decided_at=decided_at,
            reason=reason,
            chronicle_draft=chronicle_draft,
        )

        analyst_proposal.additional_properties = d
        return analyst_proposal

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
