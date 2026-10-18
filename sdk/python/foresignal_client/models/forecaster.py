from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecaster_type import ForecasterType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecaster_config import ForecasterConfig
    from ..models.forecaster_track_record_type_0 import ForecasterTrackRecordType0


T = TypeVar("T", bound="Forecaster")


@_attrs_define
class Forecaster:
    """A registered forecaster

    Attributes:
        id (str): Slug, e.g. `base-rate`, `ensemble`, `operator:jsmith`, `llm:daily-analyst`
        type_ (ForecasterType):
        name (str):
        version (str):
        active (bool):
        created_at (datetime.datetime):
        config (ForecasterConfig | Unset):
        track_record (ForecasterTrackRecordType0 | None | Unset):
    """

    id: str
    type_: ForecasterType
    name: str
    version: str
    active: bool
    created_at: datetime.datetime
    config: ForecasterConfig | Unset = UNSET
    track_record: ForecasterTrackRecordType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecaster_track_record_type_0 import ForecasterTrackRecordType0

        id = self.id

        type_ = self.type_.value

        name = self.name

        version = self.version

        active = self.active

        created_at = self.created_at.isoformat()

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        track_record: dict[str, Any] | None | Unset
        if isinstance(self.track_record, Unset):
            track_record = UNSET
        elif isinstance(self.track_record, ForecasterTrackRecordType0):
            track_record = self.track_record.to_dict()
        else:
            track_record = self.track_record

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "name": name,
                "version": version,
                "active": active,
                "createdAt": created_at,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config
        if track_record is not UNSET:
            field_dict["trackRecord"] = track_record

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecaster_config import ForecasterConfig
        from ..models.forecaster_track_record_type_0 import ForecasterTrackRecordType0

        d = dict(src_dict)
        id = d.pop("id")

        type_ = ForecasterType(d.pop("type"))

        name = d.pop("name")

        version = d.pop("version")

        active = d.pop("active")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _config = d.pop("config", UNSET)
        config: ForecasterConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = ForecasterConfig.from_dict(_config)

        def _parse_track_record(
            data: object,
        ) -> ForecasterTrackRecordType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                track_record_type_0 = ForecasterTrackRecordType0.from_dict(data)

                return track_record_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecasterTrackRecordType0 | None | Unset, data)

        track_record = _parse_track_record(d.pop("trackRecord", UNSET))

        forecaster = cls(
            id=id,
            type_=type_,
            name=name,
            version=version,
            active=active,
            created_at=created_at,
            config=config,
            track_record=track_record,
        )

        forecaster.additional_properties = d
        return forecaster

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
