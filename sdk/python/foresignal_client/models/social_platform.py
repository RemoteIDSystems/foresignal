from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_platform_id import SocialPlatformId
from ..models.social_platform_state import SocialPlatformState
from ..models.social_platform_type import SocialPlatformType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_collection_path import SocialCollectionPath
    from ..models.social_platform_credentials import SocialPlatformCredentials
    from ..models.social_platform_deletion_sync import SocialPlatformDeletionSync
    from ..models.social_platform_settings import SocialPlatformSettings
    from ..models.social_platform_stream_type_0 import SocialPlatformStreamType0
    from ..models.social_platform_terms import SocialPlatformTerms
    from ..models.social_quota import SocialQuota
    from ..models.social_spend import SocialSpend
    from ..models.source_health import SourceHealth


T = TypeVar("T", bound="SocialPlatform")


@_attrs_define
class SocialPlatform:
    """A social platform adapter with state, collection paths, health, quota and spend (135)

    Attributes:
        id (SocialPlatformId): Social platform adapter (a 004 source with descriptor, budget, health and credentials).
            Only `bluesky-jetstream` is on by default (135)
        network (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        name (str):
        state (SocialPlatformState): Adapter state; changes emit `social.platform.state`
        paths (list[SocialCollectionPath]):
        credentials (SocialPlatformCredentials): Credential status (values never returned)
        settings (SocialPlatformSettings): Operator settings of one social platform adapter; replace semantics (135).
            Credentials use the existing source credential endpoints (013)
        state_reason (None | str | Unset):
        default_enabled (bool | Unset):
        type_ (SocialPlatformType | Unset): Source type
        mode (None | str | Unset):
        health (None | SourceHealth | Unset):
        stream (None | SocialPlatformStreamType0 | Unset):
        quotas (list[SocialQuota] | Unset):
        spend (None | SocialSpend | Unset):
        deletion_sync (SocialPlatformDeletionSync | Unset): Deletion sync state
        terms (SocialPlatformTerms | Unset): Terms and cost summary from docs/Source-Catalog.md (the operator decides)
        updated_at (datetime.datetime | Unset):
    """

    id: SocialPlatformId
    network: str
    name: str
    state: SocialPlatformState
    paths: list[SocialCollectionPath]
    credentials: SocialPlatformCredentials
    settings: SocialPlatformSettings
    state_reason: None | str | Unset = UNSET
    default_enabled: bool | Unset = UNSET
    type_: SocialPlatformType | Unset = UNSET
    mode: None | str | Unset = UNSET
    health: None | SourceHealth | Unset = UNSET
    stream: None | SocialPlatformStreamType0 | Unset = UNSET
    quotas: list[SocialQuota] | Unset = UNSET
    spend: None | SocialSpend | Unset = UNSET
    deletion_sync: SocialPlatformDeletionSync | Unset = UNSET
    terms: SocialPlatformTerms | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_platform_stream_type_0 import SocialPlatformStreamType0
        from ..models.social_spend import SocialSpend
        from ..models.source_health import SourceHealth

        id = self.id.value

        network = self.network

        name = self.name

        state = self.state.value

        paths = []
        for paths_item_data in self.paths:
            paths_item = paths_item_data.to_dict()
            paths.append(paths_item)

        credentials = self.credentials.to_dict()

        settings = self.settings.to_dict()

        state_reason: None | str | Unset
        if isinstance(self.state_reason, Unset):
            state_reason = UNSET
        else:
            state_reason = self.state_reason

        default_enabled = self.default_enabled

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

        mode: None | str | Unset
        if isinstance(self.mode, Unset):
            mode = UNSET
        else:
            mode = self.mode

        health: dict[str, Any] | None | Unset
        if isinstance(self.health, Unset):
            health = UNSET
        elif isinstance(self.health, SourceHealth):
            health = self.health.to_dict()
        else:
            health = self.health

        stream: dict[str, Any] | None | Unset
        if isinstance(self.stream, Unset):
            stream = UNSET
        elif isinstance(self.stream, SocialPlatformStreamType0):
            stream = self.stream.to_dict()
        else:
            stream = self.stream

        quotas: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.quotas, Unset):
            quotas = []
            for quotas_item_data in self.quotas:
                quotas_item = quotas_item_data.to_dict()
                quotas.append(quotas_item)

        spend: dict[str, Any] | None | Unset
        if isinstance(self.spend, Unset):
            spend = UNSET
        elif isinstance(self.spend, SocialSpend):
            spend = self.spend.to_dict()
        else:
            spend = self.spend

        deletion_sync: dict[str, Any] | Unset = UNSET
        if not isinstance(self.deletion_sync, Unset):
            deletion_sync = self.deletion_sync.to_dict()

        terms: dict[str, Any] | Unset = UNSET
        if not isinstance(self.terms, Unset):
            terms = self.terms.to_dict()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "network": network,
                "name": name,
                "state": state,
                "paths": paths,
                "credentials": credentials,
                "settings": settings,
            }
        )
        if state_reason is not UNSET:
            field_dict["stateReason"] = state_reason
        if default_enabled is not UNSET:
            field_dict["defaultEnabled"] = default_enabled
        if type_ is not UNSET:
            field_dict["type"] = type_
        if mode is not UNSET:
            field_dict["mode"] = mode
        if health is not UNSET:
            field_dict["health"] = health
        if stream is not UNSET:
            field_dict["stream"] = stream
        if quotas is not UNSET:
            field_dict["quotas"] = quotas
        if spend is not UNSET:
            field_dict["spend"] = spend
        if deletion_sync is not UNSET:
            field_dict["deletionSync"] = deletion_sync
        if terms is not UNSET:
            field_dict["terms"] = terms
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_collection_path import SocialCollectionPath
        from ..models.social_platform_credentials import SocialPlatformCredentials
        from ..models.social_platform_deletion_sync import SocialPlatformDeletionSync
        from ..models.social_platform_settings import SocialPlatformSettings
        from ..models.social_platform_stream_type_0 import SocialPlatformStreamType0
        from ..models.social_platform_terms import SocialPlatformTerms
        from ..models.social_quota import SocialQuota
        from ..models.social_spend import SocialSpend
        from ..models.source_health import SourceHealth

        d = dict(src_dict)
        id = SocialPlatformId(d.pop("id"))

        network = d.pop("network")

        name = d.pop("name")

        state = SocialPlatformState(d.pop("state"))

        paths = []
        _paths = d.pop("paths")
        for paths_item_data in _paths:
            paths_item = SocialCollectionPath.from_dict(paths_item_data)

            paths.append(paths_item)

        credentials = SocialPlatformCredentials.from_dict(d.pop("credentials"))

        settings = SocialPlatformSettings.from_dict(d.pop("settings"))

        def _parse_state_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        state_reason = _parse_state_reason(d.pop("stateReason", UNSET))

        default_enabled = d.pop("defaultEnabled", UNSET)

        _type_ = d.pop("type", UNSET)
        type_: SocialPlatformType | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = SocialPlatformType(_type_)

        def _parse_mode(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        mode = _parse_mode(d.pop("mode", UNSET))

        def _parse_health(data: object) -> None | SourceHealth | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                health_type_0 = SourceHealth.from_dict(data)

                return health_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SourceHealth | Unset, data)

        health = _parse_health(d.pop("health", UNSET))

        def _parse_stream(data: object) -> None | SocialPlatformStreamType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                stream_type_0 = SocialPlatformStreamType0.from_dict(data)

                return stream_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPlatformStreamType0 | Unset, data)

        stream = _parse_stream(d.pop("stream", UNSET))

        _quotas = d.pop("quotas", UNSET)
        quotas: list[SocialQuota] | Unset = UNSET
        if _quotas is not UNSET:
            quotas = []
            for quotas_item_data in _quotas:
                quotas_item = SocialQuota.from_dict(quotas_item_data)

                quotas.append(quotas_item)

        def _parse_spend(data: object) -> None | SocialSpend | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spend_type_0 = SocialSpend.from_dict(data)

                return spend_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialSpend | Unset, data)

        spend = _parse_spend(d.pop("spend", UNSET))

        _deletion_sync = d.pop("deletionSync", UNSET)
        deletion_sync: SocialPlatformDeletionSync | Unset
        if isinstance(_deletion_sync, Unset):
            deletion_sync = UNSET
        else:
            deletion_sync = SocialPlatformDeletionSync.from_dict(_deletion_sync)

        _terms = d.pop("terms", UNSET)
        terms: SocialPlatformTerms | Unset
        if isinstance(_terms, Unset):
            terms = UNSET
        else:
            terms = SocialPlatformTerms.from_dict(_terms)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        social_platform = cls(
            id=id,
            network=network,
            name=name,
            state=state,
            paths=paths,
            credentials=credentials,
            settings=settings,
            state_reason=state_reason,
            default_enabled=default_enabled,
            type_=type_,
            mode=mode,
            health=health,
            stream=stream,
            quotas=quotas,
            spend=spend,
            deletion_sync=deletion_sync,
            terms=terms,
            updated_at=updated_at,
        )

        social_platform.additional_properties = d
        return social_platform

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
