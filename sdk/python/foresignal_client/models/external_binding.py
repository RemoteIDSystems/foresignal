from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_binding_input_target import ExternalBindingInputTarget
from ..models.external_binding_state import ExternalBindingState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.external_binding_input_config import ExternalBindingInputConfig
    from ..models.external_binding_input_filter_type_0_item import (
        ExternalBindingInputFilterType0Item,
    )


T = TypeVar("T", bound="ExternalBinding")


@_attrs_define
class ExternalBinding:
    """A binding with its state (138)

    Attributes:
        stream_id (str):
        target (ExternalBindingInputTarget):
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        state (ExternalBindingState):
        created_at (datetime.datetime):
        config (ExternalBindingInputConfig | Unset): Target configuration (docs/External-Data-Protocol.md §5)
        filter_ (list[ExternalBindingInputFilterType0Item] | None | Unset): 017 conditions on item properties
        enabled (bool | Unset):  Default: True.
        backfill (bool | Unset):  Default: True.
        manifest_sha_256 (str | Unset):
        applied (int | Unset):
        last_error (None | str | Unset):
        created_by (str | Unset):
        target_ref (None | str | Unset): What the binding set up once (the chronicle layer of a chronicle binding,
            chronicle:{id}/layers/{layerId})
        updated_at (datetime.datetime | Unset):
        last_applied_at (datetime.datetime | None | Unset):
    """

    stream_id: str
    target: ExternalBindingInputTarget
    id: str
    source_id: str
    state: ExternalBindingState
    created_at: datetime.datetime
    config: ExternalBindingInputConfig | Unset = UNSET
    filter_: list[ExternalBindingInputFilterType0Item] | None | Unset = UNSET
    enabled: bool | Unset = True
    backfill: bool | Unset = True
    manifest_sha_256: str | Unset = UNSET
    applied: int | Unset = UNSET
    last_error: None | str | Unset = UNSET
    created_by: str | Unset = UNSET
    target_ref: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    last_applied_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        target = self.target.value

        id = self.id

        source_id = self.source_id

        state = self.state.value

        created_at = self.created_at.isoformat()

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        filter_: list[dict[str, Any]] | None | Unset
        if isinstance(self.filter_, Unset):
            filter_ = UNSET
        elif isinstance(self.filter_, list):
            filter_ = []
            for filter_type_0_item_data in self.filter_:
                filter_type_0_item = filter_type_0_item_data.to_dict()
                filter_.append(filter_type_0_item)

        else:
            filter_ = self.filter_

        enabled = self.enabled

        backfill = self.backfill

        manifest_sha_256 = self.manifest_sha_256

        applied = self.applied

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        created_by = self.created_by

        target_ref: None | str | Unset
        if isinstance(self.target_ref, Unset):
            target_ref = UNSET
        else:
            target_ref = self.target_ref

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        last_applied_at: None | str | Unset
        if isinstance(self.last_applied_at, Unset):
            last_applied_at = UNSET
        elif isinstance(self.last_applied_at, datetime.datetime):
            last_applied_at = self.last_applied_at.isoformat()
        else:
            last_applied_at = self.last_applied_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "streamId": stream_id,
                "target": target,
                "id": id,
                "sourceId": source_id,
                "state": state,
                "createdAt": created_at,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config
        if filter_ is not UNSET:
            field_dict["filter"] = filter_
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if backfill is not UNSET:
            field_dict["backfill"] = backfill
        if manifest_sha_256 is not UNSET:
            field_dict["manifestSha256"] = manifest_sha_256
        if applied is not UNSET:
            field_dict["applied"] = applied
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if target_ref is not UNSET:
            field_dict["targetRef"] = target_ref
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if last_applied_at is not UNSET:
            field_dict["lastAppliedAt"] = last_applied_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.external_binding_input_config import ExternalBindingInputConfig
        from ..models.external_binding_input_filter_type_0_item import (
            ExternalBindingInputFilterType0Item,
        )

        d = dict(src_dict)
        stream_id = d.pop("streamId")

        target = ExternalBindingInputTarget(d.pop("target"))

        id = d.pop("id")

        source_id = d.pop("sourceId")

        state = ExternalBindingState(d.pop("state"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _config = d.pop("config", UNSET)
        config: ExternalBindingInputConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = ExternalBindingInputConfig.from_dict(_config)

        def _parse_filter_(
            data: object,
        ) -> list[ExternalBindingInputFilterType0Item] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                filter_type_0 = []
                _filter_type_0 = data
                for filter_type_0_item_data in _filter_type_0:
                    filter_type_0_item = ExternalBindingInputFilterType0Item.from_dict(
                        filter_type_0_item_data
                    )

                    filter_type_0.append(filter_type_0_item)

                return filter_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[ExternalBindingInputFilterType0Item] | None | Unset, data)

        filter_ = _parse_filter_(d.pop("filter", UNSET))

        enabled = d.pop("enabled", UNSET)

        backfill = d.pop("backfill", UNSET)

        manifest_sha_256 = d.pop("manifestSha256", UNSET)

        applied = d.pop("applied", UNSET)

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        created_by = d.pop("createdBy", UNSET)

        def _parse_target_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_ref = _parse_target_ref(d.pop("targetRef", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_last_applied_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_applied_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_applied_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_applied_at = _parse_last_applied_at(d.pop("lastAppliedAt", UNSET))

        external_binding = cls(
            stream_id=stream_id,
            target=target,
            id=id,
            source_id=source_id,
            state=state,
            created_at=created_at,
            config=config,
            filter_=filter_,
            enabled=enabled,
            backfill=backfill,
            manifest_sha_256=manifest_sha_256,
            applied=applied,
            last_error=last_error,
            created_by=created_by,
            target_ref=target_ref,
            updated_at=updated_at,
            last_applied_at=last_applied_at,
        )

        external_binding.additional_properties = d
        return external_binding

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
