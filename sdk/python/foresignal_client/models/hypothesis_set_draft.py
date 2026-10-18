from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_set_draft_effort import HypothesisSetDraftEffort
from ..models.hypothesis_set_draft_text_origin import HypothesisSetDraftTextOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_set_draft_usage import HypothesisSetDraftUsage
    from ..models.hypothesis_set_draft_warnings_item import (
        HypothesisSetDraftWarningsItem,
    )
    from ..models.hypothesis_set_input import HypothesisSetInput


T = TypeVar("T", bound="HypothesisSetDraft")


@_attrs_define
class HypothesisSetDraft:
    """LLM-drafted set returned for confirmation; nothing is created

    Attributes:
        set_ (HypothesisSetInput): A hypothesis set to create, with hypotheses, indicators and assumptions
        valid (bool): Structure, lint and rule specs passed
        warnings (list[HypothesisSetDraftWarningsItem]):
        tool_calls (list[str] | Unset): Tools called, in order (must start with `get_base_rate`, `find_analogs`)
        model (None | str | Unset):
        effort (HypothesisSetDraftEffort | Unset): LLM effort
        usage (HypothesisSetDraftUsage | Unset): Token usage
        manifest_id (None | str | Unset):
        text_origin (HypothesisSetDraftTextOrigin | Unset):
    """

    set_: HypothesisSetInput
    valid: bool
    warnings: list[HypothesisSetDraftWarningsItem]
    tool_calls: list[str] | Unset = UNSET
    model: None | str | Unset = UNSET
    effort: HypothesisSetDraftEffort | Unset = UNSET
    usage: HypothesisSetDraftUsage | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    text_origin: HypothesisSetDraftTextOrigin | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        set_ = self.set_.to_dict()

        valid = self.valid

        warnings = []
        for warnings_item_data in self.warnings:
            warnings_item = warnings_item_data.to_dict()
            warnings.append(warnings_item)

        tool_calls: list[str] | Unset = UNSET
        if not isinstance(self.tool_calls, Unset):
            tool_calls = self.tool_calls

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        effort: str | Unset = UNSET
        if not isinstance(self.effort, Unset):
            effort = self.effort.value

        usage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.usage, Unset):
            usage = self.usage.to_dict()

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        text_origin: str | Unset = UNSET
        if not isinstance(self.text_origin, Unset):
            text_origin = self.text_origin.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "set": set_,
                "valid": valid,
                "warnings": warnings,
            }
        )
        if tool_calls is not UNSET:
            field_dict["toolCalls"] = tool_calls
        if model is not UNSET:
            field_dict["model"] = model
        if effort is not UNSET:
            field_dict["effort"] = effort
        if usage is not UNSET:
            field_dict["usage"] = usage
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_set_draft_usage import HypothesisSetDraftUsage
        from ..models.hypothesis_set_draft_warnings_item import (
            HypothesisSetDraftWarningsItem,
        )
        from ..models.hypothesis_set_input import HypothesisSetInput

        d = dict(src_dict)
        set_ = HypothesisSetInput.from_dict(d.pop("set"))

        valid = d.pop("valid")

        warnings = []
        _warnings = d.pop("warnings")
        for warnings_item_data in _warnings:
            warnings_item = HypothesisSetDraftWarningsItem.from_dict(warnings_item_data)

            warnings.append(warnings_item)

        tool_calls = cast(list[str], d.pop("toolCalls", UNSET))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        _effort = d.pop("effort", UNSET)
        effort: HypothesisSetDraftEffort | Unset
        if isinstance(_effort, Unset):
            effort = UNSET
        else:
            effort = HypothesisSetDraftEffort(_effort)

        _usage = d.pop("usage", UNSET)
        usage: HypothesisSetDraftUsage | Unset
        if isinstance(_usage, Unset):
            usage = UNSET
        else:
            usage = HypothesisSetDraftUsage.from_dict(_usage)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        _text_origin = d.pop("textOrigin", UNSET)
        text_origin: HypothesisSetDraftTextOrigin | Unset
        if isinstance(_text_origin, Unset):
            text_origin = UNSET
        else:
            text_origin = HypothesisSetDraftTextOrigin(_text_origin)

        hypothesis_set_draft = cls(
            set_=set_,
            valid=valid,
            warnings=warnings,
            tool_calls=tool_calls,
            model=model,
            effort=effort,
            usage=usage,
            manifest_id=manifest_id,
            text_origin=text_origin,
        )

        hypothesis_set_draft.additional_properties = d
        return hypothesis_set_draft

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
