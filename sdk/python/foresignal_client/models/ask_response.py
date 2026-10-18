from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ask_response_mode import AskResponseMode
from ..models.ask_response_stop_reason import AskResponseStopReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_action import AskAction
    from ..models.ask_citation import AskCitation
    from ..models.ask_response_usage import AskResponseUsage


T = TypeVar("T", bound="AskResponse")


@_attrs_define
class AskResponse:
    """
    Attributes:
        thread_id (UUID):
        answer (str): Markdown
        citations (list[AskCitation]):
        proposed_actions (list[AskAction]):
        warnings (list[str] | Unset):
        mode (AskResponseMode | Unset):
        usage (AskResponseUsage | Unset):
        stop_reason (AskResponseStopReason | Unset):
        refusal_category (None | str | Unset): Set when the model declined (stopReason=refusal)
    """

    thread_id: UUID
    answer: str
    citations: list[AskCitation]
    proposed_actions: list[AskAction]
    warnings: list[str] | Unset = UNSET
    mode: AskResponseMode | Unset = UNSET
    usage: AskResponseUsage | Unset = UNSET
    stop_reason: AskResponseStopReason | Unset = UNSET
    refusal_category: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        thread_id = str(self.thread_id)

        answer = self.answer

        citations = []
        for citations_item_data in self.citations:
            citations_item = citations_item_data.to_dict()
            citations.append(citations_item)

        proposed_actions = []
        for proposed_actions_item_data in self.proposed_actions:
            proposed_actions_item = proposed_actions_item_data.to_dict()
            proposed_actions.append(proposed_actions_item)

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        usage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.usage, Unset):
            usage = self.usage.to_dict()

        stop_reason: str | Unset = UNSET
        if not isinstance(self.stop_reason, Unset):
            stop_reason = self.stop_reason.value

        refusal_category: None | str | Unset
        if isinstance(self.refusal_category, Unset):
            refusal_category = UNSET
        else:
            refusal_category = self.refusal_category

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "threadId": thread_id,
                "answer": answer,
                "citations": citations,
                "proposedActions": proposed_actions,
            }
        )
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if mode is not UNSET:
            field_dict["mode"] = mode
        if usage is not UNSET:
            field_dict["usage"] = usage
        if stop_reason is not UNSET:
            field_dict["stopReason"] = stop_reason
        if refusal_category is not UNSET:
            field_dict["refusalCategory"] = refusal_category

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_action import AskAction
        from ..models.ask_citation import AskCitation
        from ..models.ask_response_usage import AskResponseUsage

        d = dict(src_dict)
        thread_id = UUID(d.pop("threadId"))

        answer = d.pop("answer")

        citations = []
        _citations = d.pop("citations")
        for citations_item_data in _citations:
            citations_item = AskCitation.from_dict(citations_item_data)

            citations.append(citations_item)

        proposed_actions = []
        _proposed_actions = d.pop("proposedActions")
        for proposed_actions_item_data in _proposed_actions:
            proposed_actions_item = AskAction.from_dict(proposed_actions_item_data)

            proposed_actions.append(proposed_actions_item)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        _mode = d.pop("mode", UNSET)
        mode: AskResponseMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = AskResponseMode(_mode)

        _usage = d.pop("usage", UNSET)
        usage: AskResponseUsage | Unset
        if isinstance(_usage, Unset):
            usage = UNSET
        else:
            usage = AskResponseUsage.from_dict(_usage)

        _stop_reason = d.pop("stopReason", UNSET)
        stop_reason: AskResponseStopReason | Unset
        if isinstance(_stop_reason, Unset):
            stop_reason = UNSET
        else:
            stop_reason = AskResponseStopReason(_stop_reason)

        def _parse_refusal_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        refusal_category = _parse_refusal_category(d.pop("refusalCategory", UNSET))

        ask_response = cls(
            thread_id=thread_id,
            answer=answer,
            citations=citations,
            proposed_actions=proposed_actions,
            warnings=warnings,
            mode=mode,
            usage=usage,
            stop_reason=stop_reason,
            refusal_category=refusal_category,
        )

        ask_response.additional_properties = d
        return ask_response

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
