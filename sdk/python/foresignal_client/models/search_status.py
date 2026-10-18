from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.search_reindex_state import SearchReindexState


T = TypeVar("T", bound="SearchStatus")


@_attrs_define
class SearchStatus:
    """
    Attributes:
        semantic_enabled (bool):
        documents (int):
        reason (None | str | Unset):
        model (None | str | Unset):
        dimensions (int | None | Unset):
        backlog (int | Unset):
        reindexing (bool | Unset):
        active_model (None | str | Unset): Active model id `<providerId>:<model>:<dimensions>`
        reindex (SearchReindexState | Unset): Progress of a zero-downtime embedding reindex (027)
        degraded (bool | Unset): True while semantic search falls back to text (e.g. outgoing model no longer callable)
    """

    semantic_enabled: bool
    documents: int
    reason: None | str | Unset = UNSET
    model: None | str | Unset = UNSET
    dimensions: int | None | Unset = UNSET
    backlog: int | Unset = UNSET
    reindexing: bool | Unset = UNSET
    active_model: None | str | Unset = UNSET
    reindex: SearchReindexState | Unset = UNSET
    degraded: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        semantic_enabled = self.semantic_enabled

        documents = self.documents

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        dimensions: int | None | Unset
        if isinstance(self.dimensions, Unset):
            dimensions = UNSET
        else:
            dimensions = self.dimensions

        backlog = self.backlog

        reindexing = self.reindexing

        active_model: None | str | Unset
        if isinstance(self.active_model, Unset):
            active_model = UNSET
        else:
            active_model = self.active_model

        reindex: dict[str, Any] | Unset = UNSET
        if not isinstance(self.reindex, Unset):
            reindex = self.reindex.to_dict()

        degraded = self.degraded

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "semanticEnabled": semantic_enabled,
                "documents": documents,
            }
        )
        if reason is not UNSET:
            field_dict["reason"] = reason
        if model is not UNSET:
            field_dict["model"] = model
        if dimensions is not UNSET:
            field_dict["dimensions"] = dimensions
        if backlog is not UNSET:
            field_dict["backlog"] = backlog
        if reindexing is not UNSET:
            field_dict["reindexing"] = reindexing
        if active_model is not UNSET:
            field_dict["activeModel"] = active_model
        if reindex is not UNSET:
            field_dict["reindex"] = reindex
        if degraded is not UNSET:
            field_dict["degraded"] = degraded

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.search_reindex_state import SearchReindexState

        d = dict(src_dict)
        semantic_enabled = d.pop("semanticEnabled")

        documents = d.pop("documents")

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        def _parse_dimensions(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        dimensions = _parse_dimensions(d.pop("dimensions", UNSET))

        backlog = d.pop("backlog", UNSET)

        reindexing = d.pop("reindexing", UNSET)

        def _parse_active_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        active_model = _parse_active_model(d.pop("activeModel", UNSET))

        _reindex = d.pop("reindex", UNSET)
        reindex: SearchReindexState | Unset
        if isinstance(_reindex, Unset):
            reindex = UNSET
        else:
            reindex = SearchReindexState.from_dict(_reindex)

        degraded = d.pop("degraded", UNSET)

        search_status = cls(
            semantic_enabled=semantic_enabled,
            documents=documents,
            reason=reason,
            model=model,
            dimensions=dimensions,
            backlog=backlog,
            reindexing=reindexing,
            active_model=active_model,
            reindex=reindex,
            degraded=degraded,
        )

        search_status.additional_properties = d
        return search_status

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
