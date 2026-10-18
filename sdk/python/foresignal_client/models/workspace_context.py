from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.time_range import TimeRange
    from ..models.workspace_context_filters import WorkspaceContextFilters
    from ..models.workspace_context_playback_type_0 import WorkspaceContextPlaybackType0


T = TypeVar("T", bound="WorkspaceContext")


@_attrs_define
class WorkspaceContext:
    """Shared, versioned workspace context (Client-Contract §5)

    Attributes:
        selected_refs (list[str] | Unset):
        focus_ref (None | str | Unset):
        time_range (None | TimeRange | Unset):
        as_of (datetime.datetime | None | Unset):
        playback (None | Unset | WorkspaceContextPlaybackType0):
        viewport (Any | None | Unset):
        active_situation_id (None | str | Unset):
        active_hypothesis_set_id (None | str | Unset):
        filters (WorkspaceContextFilters | Unset): Simple values the clients agree on; validated for shape and size only
        layers (list[str] | Unset): Opaque layer ids (source ids, kinds, `observability:{family}`)
    """

    selected_refs: list[str] | Unset = UNSET
    focus_ref: None | str | Unset = UNSET
    time_range: None | TimeRange | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    playback: None | Unset | WorkspaceContextPlaybackType0 = UNSET
    viewport: Any | None | Unset = UNSET
    active_situation_id: None | str | Unset = UNSET
    active_hypothesis_set_id: None | str | Unset = UNSET
    filters: WorkspaceContextFilters | Unset = UNSET
    layers: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.time_range import TimeRange
        from ..models.workspace_context_playback_type_0 import (
            WorkspaceContextPlaybackType0,
        )

        selected_refs: list[str] | Unset = UNSET
        if not isinstance(self.selected_refs, Unset):
            selected_refs = self.selected_refs

        focus_ref: None | str | Unset
        if isinstance(self.focus_ref, Unset):
            focus_ref = UNSET
        else:
            focus_ref = self.focus_ref

        time_range: dict[str, Any] | None | Unset
        if isinstance(self.time_range, Unset):
            time_range = UNSET
        elif isinstance(self.time_range, TimeRange):
            time_range = self.time_range.to_dict()
        else:
            time_range = self.time_range

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        playback: dict[str, Any] | None | Unset
        if isinstance(self.playback, Unset):
            playback = UNSET
        elif isinstance(self.playback, WorkspaceContextPlaybackType0):
            playback = self.playback.to_dict()
        else:
            playback = self.playback

        viewport: Any | None | Unset
        if isinstance(self.viewport, Unset):
            viewport = UNSET
        else:
            viewport = self.viewport

        active_situation_id: None | str | Unset
        if isinstance(self.active_situation_id, Unset):
            active_situation_id = UNSET
        else:
            active_situation_id = self.active_situation_id

        active_hypothesis_set_id: None | str | Unset
        if isinstance(self.active_hypothesis_set_id, Unset):
            active_hypothesis_set_id = UNSET
        else:
            active_hypothesis_set_id = self.active_hypothesis_set_id

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        layers: list[str] | Unset = UNSET
        if not isinstance(self.layers, Unset):
            layers = self.layers

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if selected_refs is not UNSET:
            field_dict["selectedRefs"] = selected_refs
        if focus_ref is not UNSET:
            field_dict["focusRef"] = focus_ref
        if time_range is not UNSET:
            field_dict["timeRange"] = time_range
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if playback is not UNSET:
            field_dict["playback"] = playback
        if viewport is not UNSET:
            field_dict["viewport"] = viewport
        if active_situation_id is not UNSET:
            field_dict["activeSituationId"] = active_situation_id
        if active_hypothesis_set_id is not UNSET:
            field_dict["activeHypothesisSetId"] = active_hypothesis_set_id
        if filters is not UNSET:
            field_dict["filters"] = filters
        if layers is not UNSET:
            field_dict["layers"] = layers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.time_range import TimeRange
        from ..models.workspace_context_filters import WorkspaceContextFilters
        from ..models.workspace_context_playback_type_0 import (
            WorkspaceContextPlaybackType0,
        )

        d = dict(src_dict)
        selected_refs = cast(list[str], d.pop("selectedRefs", UNSET))

        def _parse_focus_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        focus_ref = _parse_focus_ref(d.pop("focusRef", UNSET))

        def _parse_time_range(data: object) -> None | TimeRange | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                time_range_type_0 = TimeRange.from_dict(data)

                return time_range_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TimeRange | Unset, data)

        time_range = _parse_time_range(d.pop("timeRange", UNSET))

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        def _parse_playback(
            data: object,
        ) -> None | Unset | WorkspaceContextPlaybackType0:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                playback_type_0 = WorkspaceContextPlaybackType0.from_dict(data)

                return playback_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | WorkspaceContextPlaybackType0, data)

        playback = _parse_playback(d.pop("playback", UNSET))

        def _parse_viewport(data: object) -> Any | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(Any | None | Unset, data)

        viewport = _parse_viewport(d.pop("viewport", UNSET))

        def _parse_active_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        active_situation_id = _parse_active_situation_id(
            d.pop("activeSituationId", UNSET)
        )

        def _parse_active_hypothesis_set_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        active_hypothesis_set_id = _parse_active_hypothesis_set_id(
            d.pop("activeHypothesisSetId", UNSET)
        )

        _filters = d.pop("filters", UNSET)
        filters: WorkspaceContextFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = WorkspaceContextFilters.from_dict(_filters)

        layers = cast(list[str], d.pop("layers", UNSET))

        workspace_context = cls(
            selected_refs=selected_refs,
            focus_ref=focus_ref,
            time_range=time_range,
            as_of=as_of,
            playback=playback,
            viewport=viewport,
            active_situation_id=active_situation_id,
            active_hypothesis_set_id=active_hypothesis_set_id,
            filters=filters,
            layers=layers,
        )

        workspace_context.additional_properties = d
        return workspace_context

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
