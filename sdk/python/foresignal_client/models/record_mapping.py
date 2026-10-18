from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.record_mapping_version import RecordMappingVersion
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_field import MappingField
    from ..models.mapping_time_spec_type_0 import MappingTimeSpecType0
    from ..models.mapping_time_spec_type_1 import MappingTimeSpecType1
    from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
    from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
    from ..models.mapping_value_spec_type_2 import MappingValueSpecType2
    from ..models.record_mapping_filter_item import RecordMappingFilterItem
    from ..models.record_mapping_geometry_type_0 import RecordMappingGeometryType0
    from ..models.record_mapping_geometry_type_1 import RecordMappingGeometryType1
    from ..models.record_mapping_geometry_type_2 import RecordMappingGeometryType2
    from ..models.record_mapping_pairs_type_0 import RecordMappingPairsType0
    from ..models.record_mapping_quality_type_0 import RecordMappingQualityType0
    from ..models.record_mapping_spread_type_0 import RecordMappingSpreadType0
    from ..models.record_mapping_tombstone_type_0 import RecordMappingTombstoneType0


T = TypeVar("T", bound="RecordMapping")


@_attrs_define
class RecordMapping:
    """Declarative mapping from an inbound JSON, form or query-string payload to normalized records (059). Data, not
    script: no expression language. JSONPath is the Jayway dialect (`$` is the document, or the current item under
    `select`; `['a-b']` quotes a member name); deep scan (`..`), function calls and regex filters (`=~`) are rejected.
    Unknown members are rejected.

        Attributes:
            version (RecordMappingVersion):
            external_id (MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2): One mapping value: a Jayway
                JSONPath (item-relative under `select`; `$pairs.<key>` reads a flattened pair), a substitution-only template, or
                a constant (059)
            observed_at (MappingTimeSpecType0 | MappingTimeSpecType1): Time value: a path with a format and zone, or the
                receive time. Must agree with the source's `observedAtMode` (059)
            select (None | str | Unset): Optional JSONPath producing an array; one record per item, other paths item-
                relative Example: $.msgs[*].
            kind (MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2 | None | Unset): Required unless the
                source has exactly one kind
            event_time (MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset):
            valid_until (MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset):
            geometry (None | RecordMappingGeometryType0 | RecordMappingGeometryType1 | RecordMappingGeometryType2 | Unset):
                Point from lon/lat specs, a GeoJSON geometry at a path, a constant point, or null (omitted). A coordinate
                outside the valid range (an AIS sentinel such as lat 91 or lon 181) leaves the geometry out with a
                GEOMETRY_OUT_OF_RANGE warning. `fallback` (059) is the point used when the mapped values give none, such as a
                LoRaWAN device's configured position.
            tombstone (None | RecordMappingTombstoneType0 | Unset): A record whose value at `path` equals `equals` (or is in
                `in`) is a tombstone
            quality (None | RecordMappingQualityType0 | Unset):
            fields (list[MappingField] | Unset): Payload fields, in order. A name may repeat: the first entry with a value
                wins (several sensors feeding one field, e.g. airrohr `pm25` from SDS011, PMS or SPS30). A value that cannot be
                coerced to `type`, or is NaN or infinite, leaves that field out with a FIELD_REJECTED warning.
            pairs (None | RecordMappingPairsType0 | Unset): Flattens name/value arrays (e.g. airrohr `sensordatavalues`);
                fields then reference `$pairs.<key>`
            drop (list[str] | Unset): Input keys never stored (secrets, device identifiers)
            filter_ (list[RecordMappingFilterItem] | Unset): Records failing any condition are skipped and counted
            spread (None | RecordMappingSpreadType0 | Unset): Copies the scalar members of one object into top-level fields
                (ChirpStack `object`, TTS `decoded_payload`). Members in `drop`, members whose name is not a valid field name,
                and names a field already produced are skipped (059)
    """

    version: RecordMappingVersion
    external_id: MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2
    observed_at: MappingTimeSpecType0 | MappingTimeSpecType1
    select: None | str | Unset = UNSET
    kind: (
        MappingValueSpecType0
        | MappingValueSpecType1
        | MappingValueSpecType2
        | None
        | Unset
    ) = UNSET
    event_time: MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset = UNSET
    valid_until: MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset = UNSET
    geometry: (
        None
        | RecordMappingGeometryType0
        | RecordMappingGeometryType1
        | RecordMappingGeometryType2
        | Unset
    ) = UNSET
    tombstone: None | RecordMappingTombstoneType0 | Unset = UNSET
    quality: None | RecordMappingQualityType0 | Unset = UNSET
    fields: list[MappingField] | Unset = UNSET
    pairs: None | RecordMappingPairsType0 | Unset = UNSET
    drop: list[str] | Unset = UNSET
    filter_: list[RecordMappingFilterItem] | Unset = UNSET
    spread: None | RecordMappingSpreadType0 | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.mapping_time_spec_type_0 import MappingTimeSpecType0
        from ..models.mapping_time_spec_type_1 import MappingTimeSpecType1
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
        from ..models.mapping_value_spec_type_2 import MappingValueSpecType2
        from ..models.record_mapping_geometry_type_0 import RecordMappingGeometryType0
        from ..models.record_mapping_geometry_type_1 import RecordMappingGeometryType1
        from ..models.record_mapping_geometry_type_2 import RecordMappingGeometryType2
        from ..models.record_mapping_pairs_type_0 import RecordMappingPairsType0
        from ..models.record_mapping_quality_type_0 import RecordMappingQualityType0
        from ..models.record_mapping_spread_type_0 import RecordMappingSpreadType0
        from ..models.record_mapping_tombstone_type_0 import RecordMappingTombstoneType0

        version = self.version.value

        external_id: dict[str, Any]
        if isinstance(self.external_id, MappingValueSpecType0) or isinstance(
            self.external_id, MappingValueSpecType1
        ):
            external_id = self.external_id.to_dict()
        else:
            external_id = self.external_id.to_dict()

        observed_at: dict[str, Any]
        if isinstance(self.observed_at, MappingTimeSpecType0):
            observed_at = self.observed_at.to_dict()
        else:
            observed_at = self.observed_at.to_dict()

        select: None | str | Unset
        if isinstance(self.select, Unset):
            select = UNSET
        else:
            select = self.select

        kind: dict[str, Any] | None | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        elif (
            isinstance(self.kind, MappingValueSpecType0)
            or isinstance(self.kind, MappingValueSpecType1)
            or isinstance(self.kind, MappingValueSpecType2)
        ):
            kind = self.kind.to_dict()
        else:
            kind = self.kind

        event_time: dict[str, Any] | None | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, MappingTimeSpecType0) or isinstance(
            self.event_time, MappingTimeSpecType1
        ):
            event_time = self.event_time.to_dict()
        else:
            event_time = self.event_time

        valid_until: dict[str, Any] | None | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, MappingTimeSpecType0) or isinstance(
            self.valid_until, MappingTimeSpecType1
        ):
            valid_until = self.valid_until.to_dict()
        else:
            valid_until = self.valid_until

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif (
            isinstance(self.geometry, RecordMappingGeometryType0)
            or isinstance(self.geometry, RecordMappingGeometryType1)
            or isinstance(self.geometry, RecordMappingGeometryType2)
        ):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        tombstone: dict[str, Any] | None | Unset
        if isinstance(self.tombstone, Unset):
            tombstone = UNSET
        elif isinstance(self.tombstone, RecordMappingTombstoneType0):
            tombstone = self.tombstone.to_dict()
        else:
            tombstone = self.tombstone

        quality: dict[str, Any] | None | Unset
        if isinstance(self.quality, Unset):
            quality = UNSET
        elif isinstance(self.quality, RecordMappingQualityType0):
            quality = self.quality.to_dict()
        else:
            quality = self.quality

        fields: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = []
            for fields_item_data in self.fields:
                fields_item = fields_item_data.to_dict()
                fields.append(fields_item)

        pairs: dict[str, Any] | None | Unset
        if isinstance(self.pairs, Unset):
            pairs = UNSET
        elif isinstance(self.pairs, RecordMappingPairsType0):
            pairs = self.pairs.to_dict()
        else:
            pairs = self.pairs

        drop: list[str] | Unset = UNSET
        if not isinstance(self.drop, Unset):
            drop = self.drop

        filter_: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.filter_, Unset):
            filter_ = []
            for filter_item_data in self.filter_:
                filter_item = filter_item_data.to_dict()
                filter_.append(filter_item)

        spread: dict[str, Any] | None | Unset
        if isinstance(self.spread, Unset):
            spread = UNSET
        elif isinstance(self.spread, RecordMappingSpreadType0):
            spread = self.spread.to_dict()
        else:
            spread = self.spread

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "version": version,
                "externalId": external_id,
                "observedAt": observed_at,
            }
        )
        if select is not UNSET:
            field_dict["select"] = select
        if kind is not UNSET:
            field_dict["kind"] = kind
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if tombstone is not UNSET:
            field_dict["tombstone"] = tombstone
        if quality is not UNSET:
            field_dict["quality"] = quality
        if fields is not UNSET:
            field_dict["fields"] = fields
        if pairs is not UNSET:
            field_dict["pairs"] = pairs
        if drop is not UNSET:
            field_dict["drop"] = drop
        if filter_ is not UNSET:
            field_dict["filter"] = filter_
        if spread is not UNSET:
            field_dict["spread"] = spread

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_field import MappingField
        from ..models.mapping_time_spec_type_0 import MappingTimeSpecType0
        from ..models.mapping_time_spec_type_1 import MappingTimeSpecType1
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
        from ..models.mapping_value_spec_type_2 import MappingValueSpecType2
        from ..models.record_mapping_filter_item import RecordMappingFilterItem
        from ..models.record_mapping_geometry_type_0 import RecordMappingGeometryType0
        from ..models.record_mapping_geometry_type_1 import RecordMappingGeometryType1
        from ..models.record_mapping_geometry_type_2 import RecordMappingGeometryType2
        from ..models.record_mapping_pairs_type_0 import RecordMappingPairsType0
        from ..models.record_mapping_quality_type_0 import RecordMappingQualityType0
        from ..models.record_mapping_spread_type_0 import RecordMappingSpreadType0
        from ..models.record_mapping_tombstone_type_0 import RecordMappingTombstoneType0

        d = dict(src_dict)
        version = RecordMappingVersion(d.pop("version"))

        def _parse_external_id(
            data: object,
        ) -> MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_0 = (
                    MappingValueSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_1 = (
                    MappingValueSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_mapping_value_spec_type_2 = (
                MappingValueSpecType2.from_dict(data)
            )

            return componentsschemas_mapping_value_spec_type_2

        external_id = _parse_external_id(d.pop("externalId"))

        def _parse_observed_at(
            data: object,
        ) -> MappingTimeSpecType0 | MappingTimeSpecType1:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_time_spec_type_0 = (
                    MappingTimeSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_time_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_mapping_time_spec_type_1 = MappingTimeSpecType1.from_dict(
                data
            )

            return componentsschemas_mapping_time_spec_type_1

        observed_at = _parse_observed_at(d.pop("observedAt"))

        def _parse_select(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        select = _parse_select(d.pop("select", UNSET))

        def _parse_kind(
            data: object,
        ) -> (
            MappingValueSpecType0
            | MappingValueSpecType1
            | MappingValueSpecType2
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_0 = (
                    MappingValueSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_1 = (
                    MappingValueSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_2 = (
                    MappingValueSpecType2.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                MappingValueSpecType0
                | MappingValueSpecType1
                | MappingValueSpecType2
                | None
                | Unset,
                data,
            )

        kind = _parse_kind(d.pop("kind", UNSET))

        def _parse_event_time(
            data: object,
        ) -> MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_time_spec_type_0 = (
                    MappingTimeSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_time_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_time_spec_type_1 = (
                    MappingTimeSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_time_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset, data
            )

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

        def _parse_valid_until(
            data: object,
        ) -> MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_time_spec_type_0 = (
                    MappingTimeSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_time_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_time_spec_type_1 = (
                    MappingTimeSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_time_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                MappingTimeSpecType0 | MappingTimeSpecType1 | None | Unset, data
            )

        valid_until = _parse_valid_until(d.pop("validUntil", UNSET))

        def _parse_geometry(
            data: object,
        ) -> (
            None
            | RecordMappingGeometryType0
            | RecordMappingGeometryType1
            | RecordMappingGeometryType2
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = RecordMappingGeometryType0.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_1 = RecordMappingGeometryType1.from_dict(data)

                return geometry_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_2 = RecordMappingGeometryType2.from_dict(data)

                return geometry_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | RecordMappingGeometryType0
                | RecordMappingGeometryType1
                | RecordMappingGeometryType2
                | Unset,
                data,
            )

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_tombstone(
            data: object,
        ) -> None | RecordMappingTombstoneType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                tombstone_type_0 = RecordMappingTombstoneType0.from_dict(data)

                return tombstone_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMappingTombstoneType0 | Unset, data)

        tombstone = _parse_tombstone(d.pop("tombstone", UNSET))

        def _parse_quality(data: object) -> None | RecordMappingQualityType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quality_type_0 = RecordMappingQualityType0.from_dict(data)

                return quality_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMappingQualityType0 | Unset, data)

        quality = _parse_quality(d.pop("quality", UNSET))

        _fields = d.pop("fields", UNSET)
        fields: list[MappingField] | Unset = UNSET
        if _fields is not UNSET:
            fields = []
            for fields_item_data in _fields:
                fields_item = MappingField.from_dict(fields_item_data)

                fields.append(fields_item)

        def _parse_pairs(data: object) -> None | RecordMappingPairsType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                pairs_type_0 = RecordMappingPairsType0.from_dict(data)

                return pairs_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMappingPairsType0 | Unset, data)

        pairs = _parse_pairs(d.pop("pairs", UNSET))

        drop = cast(list[str], d.pop("drop", UNSET))

        _filter_ = d.pop("filter", UNSET)
        filter_: list[RecordMappingFilterItem] | Unset = UNSET
        if _filter_ is not UNSET:
            filter_ = []
            for filter_item_data in _filter_:
                filter_item = RecordMappingFilterItem.from_dict(filter_item_data)

                filter_.append(filter_item)

        def _parse_spread(data: object) -> None | RecordMappingSpreadType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spread_type_0 = RecordMappingSpreadType0.from_dict(data)

                return spread_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMappingSpreadType0 | Unset, data)

        spread = _parse_spread(d.pop("spread", UNSET))

        record_mapping = cls(
            version=version,
            external_id=external_id,
            observed_at=observed_at,
            select=select,
            kind=kind,
            event_time=event_time,
            valid_until=valid_until,
            geometry=geometry,
            tombstone=tombstone,
            quality=quality,
            fields=fields,
            pairs=pairs,
            drop=drop,
            filter_=filter_,
            spread=spread,
        )

        return record_mapping
