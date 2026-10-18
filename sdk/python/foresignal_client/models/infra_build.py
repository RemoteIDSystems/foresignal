from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_build_status import InfraBuildStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_build_edges_by_label import InfraBuildEdgesByLabel
    from ..models.infra_build_edges_by_method import InfraBuildEdgesByMethod
    from ..models.infra_build_inputs import InfraBuildInputs


T = TypeVar("T", bound="InfraBuild")


@_attrs_define
class InfraBuild:
    """A graph build record

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (InfraBuildStatus):
        started_at (datetime.datetime):
        rules_version (str):
        finished_at (datetime.datetime | None | Unset):
        inputs (InfraBuildInputs | Unset): sourceId → last seq read
        nodes (int | Unset):
        edges_by_method (InfraBuildEdgesByMethod | Unset):
        edges_by_label (InfraBuildEdgesByLabel | Unset):
        warnings (list[str] | Unset):
    """

    id: str
    status: InfraBuildStatus
    started_at: datetime.datetime
    rules_version: str
    finished_at: datetime.datetime | None | Unset = UNSET
    inputs: InfraBuildInputs | Unset = UNSET
    nodes: int | Unset = UNSET
    edges_by_method: InfraBuildEdgesByMethod | Unset = UNSET
    edges_by_label: InfraBuildEdgesByLabel | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        status = self.status.value

        started_at = self.started_at.isoformat()

        rules_version = self.rules_version

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        inputs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.inputs, Unset):
            inputs = self.inputs.to_dict()

        nodes = self.nodes

        edges_by_method: dict[str, Any] | Unset = UNSET
        if not isinstance(self.edges_by_method, Unset):
            edges_by_method = self.edges_by_method.to_dict()

        edges_by_label: dict[str, Any] | Unset = UNSET
        if not isinstance(self.edges_by_label, Unset):
            edges_by_label = self.edges_by_label.to_dict()

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "status": status,
                "startedAt": started_at,
                "rulesVersion": rules_version,
            }
        )
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if inputs is not UNSET:
            field_dict["inputs"] = inputs
        if nodes is not UNSET:
            field_dict["nodes"] = nodes
        if edges_by_method is not UNSET:
            field_dict["edgesByMethod"] = edges_by_method
        if edges_by_label is not UNSET:
            field_dict["edgesByLabel"] = edges_by_label
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_build_edges_by_label import InfraBuildEdgesByLabel
        from ..models.infra_build_edges_by_method import InfraBuildEdgesByMethod
        from ..models.infra_build_inputs import InfraBuildInputs

        d = dict(src_dict)
        id = d.pop("id")

        status = InfraBuildStatus(d.pop("status"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        rules_version = d.pop("rulesVersion")

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        _inputs = d.pop("inputs", UNSET)
        inputs: InfraBuildInputs | Unset
        if isinstance(_inputs, Unset):
            inputs = UNSET
        else:
            inputs = InfraBuildInputs.from_dict(_inputs)

        nodes = d.pop("nodes", UNSET)

        _edges_by_method = d.pop("edgesByMethod", UNSET)
        edges_by_method: InfraBuildEdgesByMethod | Unset
        if isinstance(_edges_by_method, Unset):
            edges_by_method = UNSET
        else:
            edges_by_method = InfraBuildEdgesByMethod.from_dict(_edges_by_method)

        _edges_by_label = d.pop("edgesByLabel", UNSET)
        edges_by_label: InfraBuildEdgesByLabel | Unset
        if isinstance(_edges_by_label, Unset):
            edges_by_label = UNSET
        else:
            edges_by_label = InfraBuildEdgesByLabel.from_dict(_edges_by_label)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        infra_build = cls(
            id=id,
            status=status,
            started_at=started_at,
            rules_version=rules_version,
            finished_at=finished_at,
            inputs=inputs,
            nodes=nodes,
            edges_by_method=edges_by_method,
            edges_by_label=edges_by_label,
            warnings=warnings,
        )

        infra_build.additional_properties = d
        return infra_build

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
