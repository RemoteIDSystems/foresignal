from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_overview_status import DossierSectionsOverviewStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_overview_facets_type_0 import (
        DossierSectionsOverviewFacetsType0,
    )
    from ..models.dossier_sections_overview_fields import DossierSectionsOverviewFields
    from ..models.dossier_sections_overview_links_type_0 import (
        DossierSectionsOverviewLinksType0,
    )


T = TypeVar("T", bound="DossierSectionsOverview")


@_attrs_define
class DossierSectionsOverview:
    """Type-specific key fields

    Attributes:
        status (DossierSectionsOverviewStatus):
        message (None | str | Unset):
        links (DossierSectionsOverviewLinksType0 | None | Unset):
        facets (DossierSectionsOverviewFacetsType0 | None | Unset):
        truncated (bool | Unset):
        fields (DossierSectionsOverviewFields | Unset):
    """

    status: DossierSectionsOverviewStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsOverviewLinksType0 | None | Unset = UNSET
    facets: DossierSectionsOverviewFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    fields: DossierSectionsOverviewFields | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_overview_facets_type_0 import (
            DossierSectionsOverviewFacetsType0,
        )
        from ..models.dossier_sections_overview_links_type_0 import (
            DossierSectionsOverviewLinksType0,
        )

        status = self.status.value

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        links: dict[str, Any] | None | Unset
        if isinstance(self.links, Unset):
            links = UNSET
        elif isinstance(self.links, DossierSectionsOverviewLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsOverviewFacetsType0):
            facets = self.facets.to_dict()
        else:
            facets = self.facets

        truncated = self.truncated

        fields: dict[str, Any] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if links is not UNSET:
            field_dict["links"] = links
        if facets is not UNSET:
            field_dict["facets"] = facets
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if fields is not UNSET:
            field_dict["fields"] = fields

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_overview_facets_type_0 import (
            DossierSectionsOverviewFacetsType0,
        )
        from ..models.dossier_sections_overview_fields import (
            DossierSectionsOverviewFields,
        )
        from ..models.dossier_sections_overview_links_type_0 import (
            DossierSectionsOverviewLinksType0,
        )

        d = dict(src_dict)
        status = DossierSectionsOverviewStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsOverviewLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsOverviewLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsOverviewLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsOverviewFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsOverviewFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsOverviewFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _fields = d.pop("fields", UNSET)
        fields: DossierSectionsOverviewFields | Unset
        if isinstance(_fields, Unset):
            fields = UNSET
        else:
            fields = DossierSectionsOverviewFields.from_dict(_fields)

        dossier_sections_overview = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            fields=fields,
        )

        dossier_sections_overview.additional_properties = d
        return dossier_sections_overview

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
