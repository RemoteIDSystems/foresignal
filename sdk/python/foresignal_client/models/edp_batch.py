from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_delete_item import EdpDeleteItem
    from ..models.edp_document_item import EdpDocumentItem
    from ..models.edp_event_item import EdpEventItem
    from ..models.edp_feature_item import EdpFeatureItem
    from ..models.edp_forecast_item import EdpForecastItem
    from ..models.edp_observation_item import EdpObservationItem
    from ..models.edp_snapshot import EdpSnapshot
    from ..models.edp_status_item import EdpStatusItem


T = TypeVar("T", bound="EdpBatch")


@_attrs_define
class EdpBatch:
    """A batch of EDP items (push body or pull response)

    Attributes:
        protocol (str): foresignal-edp/1.N; N must not exceed the highest minor the instance supports (GET
            /v1/external/protocol), else PROTOCOL_UNSUPPORTED
        items (list[EdpDeleteItem | EdpDocumentItem | EdpEventItem | EdpFeatureItem | EdpForecastItem |
            EdpObservationItem | EdpStatusItem]):
        source_id (str | Unset): Optional. The source is identified by the push URL and key (or the pull registration);
            when present this must equal the registered source id (400 SOURCE_MISMATCH).
        sent_at (datetime.datetime | Unset):
        next_cursor (None | str | Unset):
        snapshot (EdpSnapshot | None | Unset):
    """

    protocol: str
    items: list[
        EdpDeleteItem
        | EdpDocumentItem
        | EdpEventItem
        | EdpFeatureItem
        | EdpForecastItem
        | EdpObservationItem
        | EdpStatusItem
    ]
    source_id: str | Unset = UNSET
    sent_at: datetime.datetime | Unset = UNSET
    next_cursor: None | str | Unset = UNSET
    snapshot: EdpSnapshot | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_delete_item import EdpDeleteItem
        from ..models.edp_document_item import EdpDocumentItem
        from ..models.edp_event_item import EdpEventItem
        from ..models.edp_feature_item import EdpFeatureItem
        from ..models.edp_forecast_item import EdpForecastItem
        from ..models.edp_observation_item import EdpObservationItem
        from ..models.edp_snapshot import EdpSnapshot

        protocol = self.protocol

        items = []
        for items_item_data in self.items:
            items_item: dict[str, Any]
            if (
                isinstance(items_item_data, EdpFeatureItem)
                or isinstance(items_item_data, EdpEventItem)
                or isinstance(items_item_data, EdpObservationItem)
                or isinstance(items_item_data, EdpDocumentItem)
                or isinstance(items_item_data, EdpForecastItem)
                or isinstance(items_item_data, EdpDeleteItem)
            ):
                items_item = items_item_data.to_dict()
            else:
                items_item = items_item_data.to_dict()

            items.append(items_item)

        source_id = self.source_id

        sent_at: str | Unset = UNSET
        if not isinstance(self.sent_at, Unset):
            sent_at = self.sent_at.isoformat()

        next_cursor: None | str | Unset
        if isinstance(self.next_cursor, Unset):
            next_cursor = UNSET
        else:
            next_cursor = self.next_cursor

        snapshot: dict[str, Any] | None | Unset
        if isinstance(self.snapshot, Unset):
            snapshot = UNSET
        elif isinstance(self.snapshot, EdpSnapshot):
            snapshot = self.snapshot.to_dict()
        else:
            snapshot = self.snapshot

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "protocol": protocol,
                "items": items,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if sent_at is not UNSET:
            field_dict["sentAt"] = sent_at
        if next_cursor is not UNSET:
            field_dict["nextCursor"] = next_cursor
        if snapshot is not UNSET:
            field_dict["snapshot"] = snapshot

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_delete_item import EdpDeleteItem
        from ..models.edp_document_item import EdpDocumentItem
        from ..models.edp_event_item import EdpEventItem
        from ..models.edp_feature_item import EdpFeatureItem
        from ..models.edp_forecast_item import EdpForecastItem
        from ..models.edp_observation_item import EdpObservationItem
        from ..models.edp_snapshot import EdpSnapshot
        from ..models.edp_status_item import EdpStatusItem

        d = dict(src_dict)
        protocol = d.pop("protocol")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:

            def _parse_items_item(
                data: object,
            ) -> (
                EdpDeleteItem
                | EdpDocumentItem
                | EdpEventItem
                | EdpFeatureItem
                | EdpForecastItem
                | EdpObservationItem
                | EdpStatusItem
            ):
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_0 = EdpFeatureItem.from_dict(data)

                    return componentsschemas_edp_item_type_0
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_1 = EdpEventItem.from_dict(data)

                    return componentsschemas_edp_item_type_1
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_2 = EdpObservationItem.from_dict(
                        data
                    )

                    return componentsschemas_edp_item_type_2
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_3 = EdpDocumentItem.from_dict(data)

                    return componentsschemas_edp_item_type_3
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_4 = EdpForecastItem.from_dict(data)

                    return componentsschemas_edp_item_type_4
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_edp_item_type_5 = EdpDeleteItem.from_dict(data)

                    return componentsschemas_edp_item_type_5
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_edp_item_type_6 = EdpStatusItem.from_dict(data)

                return componentsschemas_edp_item_type_6

            items_item = _parse_items_item(items_item_data)

            items.append(items_item)

        source_id = d.pop("sourceId", UNSET)

        _sent_at = d.pop("sentAt", UNSET)
        sent_at: datetime.datetime | Unset
        if isinstance(_sent_at, Unset):
            sent_at = UNSET
        else:
            sent_at = datetime.datetime.fromisoformat(_sent_at)

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        def _parse_snapshot(data: object) -> EdpSnapshot | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                snapshot_type_0 = EdpSnapshot.from_dict(data)

                return snapshot_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpSnapshot | None | Unset, data)

        snapshot = _parse_snapshot(d.pop("snapshot", UNSET))

        edp_batch = cls(
            protocol=protocol,
            items=items,
            source_id=source_id,
            sent_at=sent_at,
            next_cursor=next_cursor,
            snapshot=snapshot,
        )

        edp_batch.additional_properties = d
        return edp_batch

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
