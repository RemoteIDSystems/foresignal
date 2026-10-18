from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_link_decision_action import SituationLinkDecisionAction
from ..models.situation_link_decision_member_type import SituationLinkDecisionMemberType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_join_reason import SituationJoinReason
    from ..models.situation_link_decision_candidates_item import (
        SituationLinkDecisionCandidatesItem,
    )


T = TypeVar("T", bound="SituationLinkDecision")


@_attrs_define
class SituationLinkDecision:
    """What the linker would do with one input

    Attributes:
        seq (int):
        input_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        action (SituationLinkDecisionAction):
        member_type (SituationLinkDecisionMemberType | Unset):
        signal_family (None | str | Unset):
        situation_id (None | str | Unset): Joined situation, or the would-be id of a seeded one
        seed_title (None | str | Unset): Deterministic title of a seeded situation
        score (float | None | Unset):
        join_reason (None | SituationJoinReason | Unset):
        candidates (list[SituationLinkDecisionCandidatesItem] | Unset):
    """

    seq: int
    input_ref: str
    action: SituationLinkDecisionAction
    member_type: SituationLinkDecisionMemberType | Unset = UNSET
    signal_family: None | str | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    seed_title: None | str | Unset = UNSET
    score: float | None | Unset = UNSET
    join_reason: None | SituationJoinReason | Unset = UNSET
    candidates: list[SituationLinkDecisionCandidatesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.situation_join_reason import SituationJoinReason

        seq = self.seq

        input_ref = self.input_ref

        action = self.action.value

        member_type: str | Unset = UNSET
        if not isinstance(self.member_type, Unset):
            member_type = self.member_type.value

        signal_family: None | str | Unset
        if isinstance(self.signal_family, Unset):
            signal_family = UNSET
        else:
            signal_family = self.signal_family

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        seed_title: None | str | Unset
        if isinstance(self.seed_title, Unset):
            seed_title = UNSET
        else:
            seed_title = self.seed_title

        score: float | None | Unset
        if isinstance(self.score, Unset):
            score = UNSET
        else:
            score = self.score

        join_reason: dict[str, Any] | None | Unset
        if isinstance(self.join_reason, Unset):
            join_reason = UNSET
        elif isinstance(self.join_reason, SituationJoinReason):
            join_reason = self.join_reason.to_dict()
        else:
            join_reason = self.join_reason

        candidates: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.candidates, Unset):
            candidates = []
            for candidates_item_data in self.candidates:
                candidates_item = candidates_item_data.to_dict()
                candidates.append(candidates_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "seq": seq,
                "inputRef": input_ref,
                "action": action,
            }
        )
        if member_type is not UNSET:
            field_dict["memberType"] = member_type
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if seed_title is not UNSET:
            field_dict["seedTitle"] = seed_title
        if score is not UNSET:
            field_dict["score"] = score
        if join_reason is not UNSET:
            field_dict["joinReason"] = join_reason
        if candidates is not UNSET:
            field_dict["candidates"] = candidates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_join_reason import SituationJoinReason
        from ..models.situation_link_decision_candidates_item import (
            SituationLinkDecisionCandidatesItem,
        )

        d = dict(src_dict)
        seq = d.pop("seq")

        input_ref = d.pop("inputRef")

        action = SituationLinkDecisionAction(d.pop("action"))

        _member_type = d.pop("memberType", UNSET)
        member_type: SituationLinkDecisionMemberType | Unset
        if isinstance(_member_type, Unset):
            member_type = UNSET
        else:
            member_type = SituationLinkDecisionMemberType(_member_type)

        def _parse_signal_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signal_family = _parse_signal_family(d.pop("signalFamily", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_seed_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        seed_title = _parse_seed_title(d.pop("seedTitle", UNSET))

        def _parse_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        score = _parse_score(d.pop("score", UNSET))

        def _parse_join_reason(data: object) -> None | SituationJoinReason | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                join_reason_type_0 = SituationJoinReason.from_dict(data)

                return join_reason_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationJoinReason | Unset, data)

        join_reason = _parse_join_reason(d.pop("joinReason", UNSET))

        _candidates = d.pop("candidates", UNSET)
        candidates: list[SituationLinkDecisionCandidatesItem] | Unset = UNSET
        if _candidates is not UNSET:
            candidates = []
            for candidates_item_data in _candidates:
                candidates_item = SituationLinkDecisionCandidatesItem.from_dict(
                    candidates_item_data
                )

                candidates.append(candidates_item)

        situation_link_decision = cls(
            seq=seq,
            input_ref=input_ref,
            action=action,
            member_type=member_type,
            signal_family=signal_family,
            situation_id=situation_id,
            seed_title=seed_title,
            score=score,
            join_reason=join_reason,
            candidates=candidates,
        )

        situation_link_decision.additional_properties = d
        return situation_link_decision

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
