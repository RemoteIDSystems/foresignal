from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_source_registered_via import ExternalSourceRegisteredVia
from ..models.external_source_state import ExternalSourceState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_item_rejection import EdpItemRejection
    from ..models.edp_manifest import EdpManifest
    from ..models.external_source_presentation import ExternalSourcePresentation
    from ..models.external_source_pull_type_0 import ExternalSourcePullType0
    from ..models.external_stream_stats import ExternalStreamStats
    from ..models.source_access import SourceAccess


T = TypeVar("T", bound="ExternalSource")


@_attrs_define
class ExternalSource:
    """An EDP provider registered on this instance (024)

    Attributes:
        source_id (str):  Example: ext-hormuz-transits.
        state (ExternalSourceState):
        manifest (EdpManifest): Self-description of an EDP provider (docs/External-Data-Protocol.md §2). Stream ids must
            be unique and each layer stream must carry feature or event items (checked at registration). Unknown fields are
            ignored at every level (forward compatibility within 1.x).
        created_at (datetime.datetime):
        registered_via (ExternalSourceRegisteredVia | Unset):
        manifest_sha_256 (str | Unset):
        access (SourceAccess | Unset): How a source reaches its data and what enabling it involves
        egress (str | Unset):
        approved_by (None | str | Unset):
        approved_at (datetime.datetime | None | Unset):
        last_item_at (datetime.datetime | None | Unset):
        pull (ExternalSourcePullType0 | None | Unset):
        streams (list[ExternalStreamStats] | Unset):
        last_validation_errors (list[EdpItemRejection] | Unset):
        health (str | Unset):
        allow_private_network (bool | Unset): Whether provider URLs may be on a private network (set at registration)
        presentation (ExternalSourcePresentation | Unset): Layer presentation hints of the layer streams, keyed by
            stream id; also in each kind's TileJSON as foresignal:presentation
        breaking_changes (list[str] | Unset): updateExternalSourceManifest only: the changes that returned the source to
            pending (empty for an additive update)
    """

    source_id: str
    state: ExternalSourceState
    manifest: EdpManifest
    created_at: datetime.datetime
    registered_via: ExternalSourceRegisteredVia | Unset = UNSET
    manifest_sha_256: str | Unset = UNSET
    access: SourceAccess | Unset = UNSET
    egress: str | Unset = UNSET
    approved_by: None | str | Unset = UNSET
    approved_at: datetime.datetime | None | Unset = UNSET
    last_item_at: datetime.datetime | None | Unset = UNSET
    pull: ExternalSourcePullType0 | None | Unset = UNSET
    streams: list[ExternalStreamStats] | Unset = UNSET
    last_validation_errors: list[EdpItemRejection] | Unset = UNSET
    health: str | Unset = UNSET
    allow_private_network: bool | Unset = UNSET
    presentation: ExternalSourcePresentation | Unset = UNSET
    breaking_changes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.external_source_pull_type_0 import ExternalSourcePullType0

        source_id = self.source_id

        state = self.state.value

        manifest = self.manifest.to_dict()

        created_at = self.created_at.isoformat()

        registered_via: str | Unset = UNSET
        if not isinstance(self.registered_via, Unset):
            registered_via = self.registered_via.value

        manifest_sha_256 = self.manifest_sha_256

        access: dict[str, Any] | Unset = UNSET
        if not isinstance(self.access, Unset):
            access = self.access.to_dict()

        egress = self.egress

        approved_by: None | str | Unset
        if isinstance(self.approved_by, Unset):
            approved_by = UNSET
        else:
            approved_by = self.approved_by

        approved_at: None | str | Unset
        if isinstance(self.approved_at, Unset):
            approved_at = UNSET
        elif isinstance(self.approved_at, datetime.datetime):
            approved_at = self.approved_at.isoformat()
        else:
            approved_at = self.approved_at

        last_item_at: None | str | Unset
        if isinstance(self.last_item_at, Unset):
            last_item_at = UNSET
        elif isinstance(self.last_item_at, datetime.datetime):
            last_item_at = self.last_item_at.isoformat()
        else:
            last_item_at = self.last_item_at

        pull: dict[str, Any] | None | Unset
        if isinstance(self.pull, Unset):
            pull = UNSET
        elif isinstance(self.pull, ExternalSourcePullType0):
            pull = self.pull.to_dict()
        else:
            pull = self.pull

        streams: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.streams, Unset):
            streams = []
            for streams_item_data in self.streams:
                streams_item = streams_item_data.to_dict()
                streams.append(streams_item)

        last_validation_errors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.last_validation_errors, Unset):
            last_validation_errors = []
            for last_validation_errors_item_data in self.last_validation_errors:
                last_validation_errors_item = last_validation_errors_item_data.to_dict()
                last_validation_errors.append(last_validation_errors_item)

        health = self.health

        allow_private_network = self.allow_private_network

        presentation: dict[str, Any] | Unset = UNSET
        if not isinstance(self.presentation, Unset):
            presentation = self.presentation.to_dict()

        breaking_changes: list[str] | Unset = UNSET
        if not isinstance(self.breaking_changes, Unset):
            breaking_changes = self.breaking_changes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "state": state,
                "manifest": manifest,
                "createdAt": created_at,
            }
        )
        if registered_via is not UNSET:
            field_dict["registeredVia"] = registered_via
        if manifest_sha_256 is not UNSET:
            field_dict["manifestSha256"] = manifest_sha_256
        if access is not UNSET:
            field_dict["access"] = access
        if egress is not UNSET:
            field_dict["egress"] = egress
        if approved_by is not UNSET:
            field_dict["approvedBy"] = approved_by
        if approved_at is not UNSET:
            field_dict["approvedAt"] = approved_at
        if last_item_at is not UNSET:
            field_dict["lastItemAt"] = last_item_at
        if pull is not UNSET:
            field_dict["pull"] = pull
        if streams is not UNSET:
            field_dict["streams"] = streams
        if last_validation_errors is not UNSET:
            field_dict["lastValidationErrors"] = last_validation_errors
        if health is not UNSET:
            field_dict["health"] = health
        if allow_private_network is not UNSET:
            field_dict["allowPrivateNetwork"] = allow_private_network
        if presentation is not UNSET:
            field_dict["presentation"] = presentation
        if breaking_changes is not UNSET:
            field_dict["breakingChanges"] = breaking_changes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_item_rejection import EdpItemRejection
        from ..models.edp_manifest import EdpManifest
        from ..models.external_source_presentation import ExternalSourcePresentation
        from ..models.external_source_pull_type_0 import ExternalSourcePullType0
        from ..models.external_stream_stats import ExternalStreamStats
        from ..models.source_access import SourceAccess

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        state = ExternalSourceState(d.pop("state"))

        manifest = EdpManifest.from_dict(d.pop("manifest"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _registered_via = d.pop("registeredVia", UNSET)
        registered_via: ExternalSourceRegisteredVia | Unset
        if isinstance(_registered_via, Unset):
            registered_via = UNSET
        else:
            registered_via = ExternalSourceRegisteredVia(_registered_via)

        manifest_sha_256 = d.pop("manifestSha256", UNSET)

        _access = d.pop("access", UNSET)
        access: SourceAccess | Unset
        if isinstance(_access, Unset):
            access = UNSET
        else:
            access = SourceAccess.from_dict(_access)

        egress = d.pop("egress", UNSET)

        def _parse_approved_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        approved_by = _parse_approved_by(d.pop("approvedBy", UNSET))

        def _parse_approved_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                approved_at_type_0 = datetime.datetime.fromisoformat(data)

                return approved_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        approved_at = _parse_approved_at(d.pop("approvedAt", UNSET))

        def _parse_last_item_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_item_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_item_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_item_at = _parse_last_item_at(d.pop("lastItemAt", UNSET))

        def _parse_pull(data: object) -> ExternalSourcePullType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                pull_type_0 = ExternalSourcePullType0.from_dict(data)

                return pull_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExternalSourcePullType0 | None | Unset, data)

        pull = _parse_pull(d.pop("pull", UNSET))

        _streams = d.pop("streams", UNSET)
        streams: list[ExternalStreamStats] | Unset = UNSET
        if _streams is not UNSET:
            streams = []
            for streams_item_data in _streams:
                streams_item = ExternalStreamStats.from_dict(streams_item_data)

                streams.append(streams_item)

        _last_validation_errors = d.pop("lastValidationErrors", UNSET)
        last_validation_errors: list[EdpItemRejection] | Unset = UNSET
        if _last_validation_errors is not UNSET:
            last_validation_errors = []
            for last_validation_errors_item_data in _last_validation_errors:
                last_validation_errors_item = EdpItemRejection.from_dict(
                    last_validation_errors_item_data
                )

                last_validation_errors.append(last_validation_errors_item)

        health = d.pop("health", UNSET)

        allow_private_network = d.pop("allowPrivateNetwork", UNSET)

        _presentation = d.pop("presentation", UNSET)
        presentation: ExternalSourcePresentation | Unset
        if isinstance(_presentation, Unset):
            presentation = UNSET
        else:
            presentation = ExternalSourcePresentation.from_dict(_presentation)

        breaking_changes = cast(list[str], d.pop("breakingChanges", UNSET))

        external_source = cls(
            source_id=source_id,
            state=state,
            manifest=manifest,
            created_at=created_at,
            registered_via=registered_via,
            manifest_sha_256=manifest_sha_256,
            access=access,
            egress=egress,
            approved_by=approved_by,
            approved_at=approved_at,
            last_item_at=last_item_at,
            pull=pull,
            streams=streams,
            last_validation_errors=last_validation_errors,
            health=health,
            allow_private_network=allow_private_network,
            presentation=presentation,
            breaking_changes=breaking_changes,
        )

        external_source.additional_properties = d
        return external_source

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
