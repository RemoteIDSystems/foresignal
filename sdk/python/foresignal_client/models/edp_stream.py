from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_stream_document_type_type_1 import EdpStreamDocumentTypeType1
from ..models.edp_stream_document_type_type_2_type_1 import (
    EdpStreamDocumentTypeType2Type1,
)
from ..models.edp_stream_document_type_type_3_type_1 import (
    EdpStreamDocumentTypeType3Type1,
)
from ..models.edp_stream_geometry import EdpStreamGeometry
from ..models.edp_stream_item_type import EdpStreamItemType
from ..models.edp_stream_mode import EdpStreamMode
from ..models.edp_stream_retention_class import EdpStreamRetentionClass
from ..models.edp_stream_update import EdpStreamUpdate
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_coverage import EdpCoverage
    from ..models.edp_presentation import EdpPresentation
    from ..models.edp_stream_fields import EdpStreamFields
    from ..models.edp_stream_payload_schema import EdpStreamPayloadSchema
    from ..models.edp_suggested_binding import EdpSuggestedBinding


T = TypeVar("T", bound="EdpStream")


@_attrs_define
class EdpStream:
    """One typed stream of items

    Attributes:
        id (str):
        mode (EdpStreamMode):
        item_type (EdpStreamItemType):
        kind (str):
        title (None | str | Unset):
        description (None | str | Unset):
        geometry (EdpStreamGeometry | Unset):  Default: EdpStreamGeometry.ANY.
        update (EdpStreamUpdate | Unset):  Default: EdpStreamUpdate.INCREMENTAL.
        payload_schema (EdpStreamPayloadSchema | Unset): JSON Schema 2020-12 for item properties
        fields (EdpStreamFields | Unset): Field roles, as in custom sources (024)
        ttl (None | str | Unset):
        retention_class (EdpStreamRetentionClass | Unset):  Default: EdpStreamRetentionClass.EVENT.
        unit (None | str | Unset):
        document_type (EdpStreamDocumentTypeType1 | EdpStreamDocumentTypeType2Type1 | EdpStreamDocumentTypeType3Type1 |
            None | Unset):
        presentation (EdpPresentation | Unset): Layer hints for clients and styles
        coverage (EdpCoverage | Unset): Declared footprint and detection probability (low-confidence observability)
        complete (bool | Unset): The stream is a complete record of its scope (social volume baselines may use it)
            Default: False.
        suggested_bindings (list[EdpSuggestedBinding] | Unset):
        snapshot_scope (None | str | Unset): For update: snapshot. Null = the whole stream is one scope; otherwise the
            item property whose value names the scope (e.g. a region code), so a snapshot only removes items of its own
            scope.
    """

    id: str
    mode: EdpStreamMode
    item_type: EdpStreamItemType
    kind: str
    title: None | str | Unset = UNSET
    description: None | str | Unset = UNSET
    geometry: EdpStreamGeometry | Unset = EdpStreamGeometry.ANY
    update: EdpStreamUpdate | Unset = EdpStreamUpdate.INCREMENTAL
    payload_schema: EdpStreamPayloadSchema | Unset = UNSET
    fields: EdpStreamFields | Unset = UNSET
    ttl: None | str | Unset = UNSET
    retention_class: EdpStreamRetentionClass | Unset = EdpStreamRetentionClass.EVENT
    unit: None | str | Unset = UNSET
    document_type: (
        EdpStreamDocumentTypeType1
        | EdpStreamDocumentTypeType2Type1
        | EdpStreamDocumentTypeType3Type1
        | None
        | Unset
    ) = UNSET
    presentation: EdpPresentation | Unset = UNSET
    coverage: EdpCoverage | Unset = UNSET
    complete: bool | Unset = False
    suggested_bindings: list[EdpSuggestedBinding] | Unset = UNSET
    snapshot_scope: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        mode = self.mode.value

        item_type = self.item_type.value

        kind = self.kind

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        geometry: str | Unset = UNSET
        if not isinstance(self.geometry, Unset):
            geometry = self.geometry.value

        update: str | Unset = UNSET
        if not isinstance(self.update, Unset):
            update = self.update.value

        payload_schema: dict[str, Any] | Unset = UNSET
        if not isinstance(self.payload_schema, Unset):
            payload_schema = self.payload_schema.to_dict()

        fields: dict[str, Any] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields.to_dict()

        ttl: None | str | Unset
        if isinstance(self.ttl, Unset):
            ttl = UNSET
        else:
            ttl = self.ttl

        retention_class: str | Unset = UNSET
        if not isinstance(self.retention_class, Unset):
            retention_class = self.retention_class.value

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        document_type: None | str | Unset
        if isinstance(self.document_type, Unset):
            document_type = UNSET
        elif (
            isinstance(self.document_type, EdpStreamDocumentTypeType1)
            or isinstance(self.document_type, EdpStreamDocumentTypeType2Type1)
            or isinstance(self.document_type, EdpStreamDocumentTypeType3Type1)
        ):
            document_type = self.document_type.value
        else:
            document_type = self.document_type

        presentation: dict[str, Any] | Unset = UNSET
        if not isinstance(self.presentation, Unset):
            presentation = self.presentation.to_dict()

        coverage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.coverage, Unset):
            coverage = self.coverage.to_dict()

        complete = self.complete

        suggested_bindings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.suggested_bindings, Unset):
            suggested_bindings = []
            for suggested_bindings_item_data in self.suggested_bindings:
                suggested_bindings_item = suggested_bindings_item_data.to_dict()
                suggested_bindings.append(suggested_bindings_item)

        snapshot_scope: None | str | Unset
        if isinstance(self.snapshot_scope, Unset):
            snapshot_scope = UNSET
        else:
            snapshot_scope = self.snapshot_scope

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "mode": mode,
                "itemType": item_type,
                "kind": kind,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if description is not UNSET:
            field_dict["description"] = description
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if update is not UNSET:
            field_dict["update"] = update
        if payload_schema is not UNSET:
            field_dict["payloadSchema"] = payload_schema
        if fields is not UNSET:
            field_dict["fields"] = fields
        if ttl is not UNSET:
            field_dict["ttl"] = ttl
        if retention_class is not UNSET:
            field_dict["retentionClass"] = retention_class
        if unit is not UNSET:
            field_dict["unit"] = unit
        if document_type is not UNSET:
            field_dict["documentType"] = document_type
        if presentation is not UNSET:
            field_dict["presentation"] = presentation
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if complete is not UNSET:
            field_dict["complete"] = complete
        if suggested_bindings is not UNSET:
            field_dict["suggestedBindings"] = suggested_bindings
        if snapshot_scope is not UNSET:
            field_dict["snapshotScope"] = snapshot_scope

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_coverage import EdpCoverage
        from ..models.edp_presentation import EdpPresentation
        from ..models.edp_stream_fields import EdpStreamFields
        from ..models.edp_stream_payload_schema import EdpStreamPayloadSchema
        from ..models.edp_suggested_binding import EdpSuggestedBinding

        d = dict(src_dict)
        id = d.pop("id")

        mode = EdpStreamMode(d.pop("mode"))

        item_type = EdpStreamItemType(d.pop("itemType"))

        kind = d.pop("kind")

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _geometry = d.pop("geometry", UNSET)
        geometry: EdpStreamGeometry | Unset
        if isinstance(_geometry, Unset):
            geometry = UNSET
        else:
            geometry = EdpStreamGeometry(_geometry)

        _update = d.pop("update", UNSET)
        update: EdpStreamUpdate | Unset
        if isinstance(_update, Unset):
            update = UNSET
        else:
            update = EdpStreamUpdate(_update)

        _payload_schema = d.pop("payloadSchema", UNSET)
        payload_schema: EdpStreamPayloadSchema | Unset
        if isinstance(_payload_schema, Unset):
            payload_schema = UNSET
        else:
            payload_schema = EdpStreamPayloadSchema.from_dict(_payload_schema)

        _fields = d.pop("fields", UNSET)
        fields: EdpStreamFields | Unset
        if isinstance(_fields, Unset):
            fields = UNSET
        else:
            fields = EdpStreamFields.from_dict(_fields)

        def _parse_ttl(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ttl = _parse_ttl(d.pop("ttl", UNSET))

        _retention_class = d.pop("retentionClass", UNSET)
        retention_class: EdpStreamRetentionClass | Unset
        if isinstance(_retention_class, Unset):
            retention_class = UNSET
        else:
            retention_class = EdpStreamRetentionClass(_retention_class)

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        def _parse_document_type(
            data: object,
        ) -> (
            EdpStreamDocumentTypeType1
            | EdpStreamDocumentTypeType2Type1
            | EdpStreamDocumentTypeType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                document_type_type_1 = EdpStreamDocumentTypeType1(data)

                return document_type_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                document_type_type_2_type_1 = EdpStreamDocumentTypeType2Type1(data)

                return document_type_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                document_type_type_3_type_1 = EdpStreamDocumentTypeType3Type1(data)

                return document_type_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                EdpStreamDocumentTypeType1
                | EdpStreamDocumentTypeType2Type1
                | EdpStreamDocumentTypeType3Type1
                | None
                | Unset,
                data,
            )

        document_type = _parse_document_type(d.pop("documentType", UNSET))

        _presentation = d.pop("presentation", UNSET)
        presentation: EdpPresentation | Unset
        if isinstance(_presentation, Unset):
            presentation = UNSET
        else:
            presentation = EdpPresentation.from_dict(_presentation)

        _coverage = d.pop("coverage", UNSET)
        coverage: EdpCoverage | Unset
        if isinstance(_coverage, Unset):
            coverage = UNSET
        else:
            coverage = EdpCoverage.from_dict(_coverage)

        complete = d.pop("complete", UNSET)

        _suggested_bindings = d.pop("suggestedBindings", UNSET)
        suggested_bindings: list[EdpSuggestedBinding] | Unset = UNSET
        if _suggested_bindings is not UNSET:
            suggested_bindings = []
            for suggested_bindings_item_data in _suggested_bindings:
                suggested_bindings_item = EdpSuggestedBinding.from_dict(
                    suggested_bindings_item_data
                )

                suggested_bindings.append(suggested_bindings_item)

        def _parse_snapshot_scope(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        snapshot_scope = _parse_snapshot_scope(d.pop("snapshotScope", UNSET))

        edp_stream = cls(
            id=id,
            mode=mode,
            item_type=item_type,
            kind=kind,
            title=title,
            description=description,
            geometry=geometry,
            update=update,
            payload_schema=payload_schema,
            fields=fields,
            ttl=ttl,
            retention_class=retention_class,
            unit=unit,
            document_type=document_type,
            presentation=presentation,
            coverage=coverage,
            complete=complete,
            suggested_bindings=suggested_bindings,
            snapshot_scope=snapshot_scope,
        )

        edp_stream.additional_properties = d
        return edp_stream

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
