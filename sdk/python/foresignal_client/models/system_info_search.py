from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_search_reindex_state import SystemInfoSearchReindexState
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoSearch")


@_attrs_define
class SystemInfoSearch:
    """Semantic search state (027)

    Attributes:
        semantic_enabled (bool | Unset):
        reason (None | str | Unset):
        documents (int | Unset):
        backlog (int | Unset):
        active_model (None | str | Unset):
        reindex_state (SystemInfoSearchReindexState | Unset):
    """

    semantic_enabled: bool | Unset = UNSET
    reason: None | str | Unset = UNSET
    documents: int | Unset = UNSET
    backlog: int | Unset = UNSET
    active_model: None | str | Unset = UNSET
    reindex_state: SystemInfoSearchReindexState | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        semantic_enabled = self.semantic_enabled

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        documents = self.documents

        backlog = self.backlog

        active_model: None | str | Unset
        if isinstance(self.active_model, Unset):
            active_model = UNSET
        else:
            active_model = self.active_model

        reindex_state: str | Unset = UNSET
        if not isinstance(self.reindex_state, Unset):
            reindex_state = self.reindex_state.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if semantic_enabled is not UNSET:
            field_dict["semanticEnabled"] = semantic_enabled
        if reason is not UNSET:
            field_dict["reason"] = reason
        if documents is not UNSET:
            field_dict["documents"] = documents
        if backlog is not UNSET:
            field_dict["backlog"] = backlog
        if active_model is not UNSET:
            field_dict["activeModel"] = active_model
        if reindex_state is not UNSET:
            field_dict["reindexState"] = reindex_state

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        semantic_enabled = d.pop("semanticEnabled", UNSET)

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        documents = d.pop("documents", UNSET)

        backlog = d.pop("backlog", UNSET)

        def _parse_active_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        active_model = _parse_active_model(d.pop("activeModel", UNSET))

        _reindex_state = d.pop("reindexState", UNSET)
        reindex_state: SystemInfoSearchReindexState | Unset
        if isinstance(_reindex_state, Unset):
            reindex_state = UNSET
        else:
            reindex_state = SystemInfoSearchReindexState(_reindex_state)

        system_info_search = cls(
            semantic_enabled=semantic_enabled,
            reason=reason,
            documents=documents,
            backlog=backlog,
            active_model=active_model,
            reindex_state=reindex_state,
        )

        system_info_search.additional_properties = d
        return system_info_search

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
