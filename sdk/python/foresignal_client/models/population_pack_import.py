from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.population_pack_import_cells import PopulationPackImportCells


T = TypeVar("T", bound="PopulationPackImport")


@_attrs_define
class PopulationPackImport:
    """Result of a population pack import

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        file_sha_256 (str):
        epoch (int):
        licence (Literal['CC-BY-4.0']):
        attribution (Literal['© European Union, GHSL']):
        imported_at (datetime.datetime):
        path (str | Unset):
        cells (PopulationPackImportCells | Unset): Imported H3 cells
        duration_ms (int | Unset):
    """

    id: str
    file_sha_256: str
    epoch: int
    licence: Literal["CC-BY-4.0"]
    attribution: Literal["© European Union, GHSL"]
    imported_at: datetime.datetime
    path: str | Unset = UNSET
    cells: PopulationPackImportCells | Unset = UNSET
    duration_ms: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        file_sha_256 = self.file_sha_256

        epoch = self.epoch

        licence = self.licence

        attribution = self.attribution

        imported_at = self.imported_at.isoformat()

        path = self.path

        cells: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cells, Unset):
            cells = self.cells.to_dict()

        duration_ms = self.duration_ms

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "fileSha256": file_sha_256,
                "epoch": epoch,
                "licence": licence,
                "attribution": attribution,
                "importedAt": imported_at,
            }
        )
        if path is not UNSET:
            field_dict["path"] = path
        if cells is not UNSET:
            field_dict["cells"] = cells
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.population_pack_import_cells import PopulationPackImportCells

        d = dict(src_dict)
        id = d.pop("id")

        file_sha_256 = d.pop("fileSha256")

        epoch = d.pop("epoch")

        licence = cast(Literal["CC-BY-4.0"], d.pop("licence"))
        if licence != "CC-BY-4.0":
            raise ValueError(f"licence must match const 'CC-BY-4.0', got '{licence}'")

        attribution = cast(Literal["© European Union, GHSL"], d.pop("attribution"))
        if attribution != "© European Union, GHSL":
            raise ValueError(
                f"attribution must match const '© European Union, GHSL', got '{attribution}'"
            )

        imported_at = datetime.datetime.fromisoformat(d.pop("importedAt"))

        path = d.pop("path", UNSET)

        _cells = d.pop("cells", UNSET)
        cells: PopulationPackImportCells | Unset
        if isinstance(_cells, Unset):
            cells = UNSET
        else:
            cells = PopulationPackImportCells.from_dict(_cells)

        duration_ms = d.pop("durationMs", UNSET)

        population_pack_import = cls(
            id=id,
            file_sha_256=file_sha_256,
            epoch=epoch,
            licence=licence,
            attribution=attribution,
            imported_at=imported_at,
            path=path,
            cells=cells,
            duration_ms=duration_ms,
        )

        population_pack_import.additional_properties = d
        return population_pack_import

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
