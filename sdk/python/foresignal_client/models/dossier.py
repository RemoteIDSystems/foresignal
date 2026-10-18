from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_coverage_type_0 import DossierCoverageType0
    from ..models.dossier_sections import DossierSections
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="Dossier")


@_attrs_define
class Dossier:
    """Everything a selected-object panel needs in one bounded response (≤ 64 KB)

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        envelope (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        generated_at (datetime.datetime):
        sections (DossierSections): Requested sections; each renders independently by `status`
        as_of (datetime.datetime | None | Unset):
        truncated (bool | Unset):
        coverage (DossierCoverageType0 | None | Unset):
    """

    ref: str
    envelope: ObjectEnvelope
    generated_at: datetime.datetime
    sections: DossierSections
    as_of: datetime.datetime | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    coverage: DossierCoverageType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_coverage_type_0 import DossierCoverageType0

        ref = self.ref

        envelope = self.envelope.to_dict()

        generated_at = self.generated_at.isoformat()

        sections = self.sections.to_dict()

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        truncated = self.truncated

        coverage: dict[str, Any] | None | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        elif isinstance(self.coverage, DossierCoverageType0):
            coverage = self.coverage.to_dict()
        else:
            coverage = self.coverage

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "envelope": envelope,
                "generatedAt": generated_at,
                "sections": sections,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if coverage is not UNSET:
            field_dict["coverage"] = coverage

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_coverage_type_0 import DossierCoverageType0
        from ..models.dossier_sections import DossierSections
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        ref = d.pop("ref")

        envelope = ObjectEnvelope.from_dict(d.pop("envelope"))

        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        sections = DossierSections.from_dict(d.pop("sections"))

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

        truncated = d.pop("truncated", UNSET)

        def _parse_coverage(data: object) -> DossierCoverageType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coverage_type_0 = DossierCoverageType0.from_dict(data)

                return coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierCoverageType0 | None | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        dossier = cls(
            ref=ref,
            envelope=envelope,
            generated_at=generated_at,
            sections=sections,
            as_of=as_of,
            truncated=truncated,
            coverage=coverage,
        )

        dossier.additional_properties = d
        return dossier

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
