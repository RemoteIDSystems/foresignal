from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_related_status import DossierSectionsRelatedStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_related_facets_type_0 import (
        DossierSectionsRelatedFacetsType0,
    )
    from ..models.dossier_sections_related_links_type_0 import (
        DossierSectionsRelatedLinksType0,
    )
    from ..models.related_object import RelatedObject


T = TypeVar("T", bound="DossierSectionsRelated")


@_attrs_define
class DossierSectionsRelated:
    """Relations grouped for panels

    Attributes:
        status (DossierSectionsRelatedStatus):
        message (None | str | Unset):
        links (DossierSectionsRelatedLinksType0 | None | Unset):
        facets (DossierSectionsRelatedFacetsType0 | None | Unset):
        truncated (bool | Unset):
        nearby (list[RelatedObject] | Unset):
        operational (list[RelatedObject] | Unset):
        infrastructure (list[RelatedObject] | Unset):
        historical (list[RelatedObject] | Unset):
    """

    status: DossierSectionsRelatedStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsRelatedLinksType0 | None | Unset = UNSET
    facets: DossierSectionsRelatedFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    nearby: list[RelatedObject] | Unset = UNSET
    operational: list[RelatedObject] | Unset = UNSET
    infrastructure: list[RelatedObject] | Unset = UNSET
    historical: list[RelatedObject] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_related_facets_type_0 import (
            DossierSectionsRelatedFacetsType0,
        )
        from ..models.dossier_sections_related_links_type_0 import (
            DossierSectionsRelatedLinksType0,
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
        elif isinstance(self.links, DossierSectionsRelatedLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsRelatedFacetsType0):
            facets = self.facets.to_dict()
        else:
            facets = self.facets

        truncated = self.truncated

        nearby: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.nearby, Unset):
            nearby = []
            for nearby_item_data in self.nearby:
                nearby_item = nearby_item_data.to_dict()
                nearby.append(nearby_item)

        operational: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.operational, Unset):
            operational = []
            for operational_item_data in self.operational:
                operational_item = operational_item_data.to_dict()
                operational.append(operational_item)

        infrastructure: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.infrastructure, Unset):
            infrastructure = []
            for infrastructure_item_data in self.infrastructure:
                infrastructure_item = infrastructure_item_data.to_dict()
                infrastructure.append(infrastructure_item)

        historical: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.historical, Unset):
            historical = []
            for historical_item_data in self.historical:
                historical_item = historical_item_data.to_dict()
                historical.append(historical_item)

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
        if nearby is not UNSET:
            field_dict["nearby"] = nearby
        if operational is not UNSET:
            field_dict["operational"] = operational
        if infrastructure is not UNSET:
            field_dict["infrastructure"] = infrastructure
        if historical is not UNSET:
            field_dict["historical"] = historical

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_related_facets_type_0 import (
            DossierSectionsRelatedFacetsType0,
        )
        from ..models.dossier_sections_related_links_type_0 import (
            DossierSectionsRelatedLinksType0,
        )
        from ..models.related_object import RelatedObject

        d = dict(src_dict)
        status = DossierSectionsRelatedStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsRelatedLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsRelatedLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsRelatedLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsRelatedFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsRelatedFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsRelatedFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _nearby = d.pop("nearby", UNSET)
        nearby: list[RelatedObject] | Unset = UNSET
        if _nearby is not UNSET:
            nearby = []
            for nearby_item_data in _nearby:
                nearby_item = RelatedObject.from_dict(nearby_item_data)

                nearby.append(nearby_item)

        _operational = d.pop("operational", UNSET)
        operational: list[RelatedObject] | Unset = UNSET
        if _operational is not UNSET:
            operational = []
            for operational_item_data in _operational:
                operational_item = RelatedObject.from_dict(operational_item_data)

                operational.append(operational_item)

        _infrastructure = d.pop("infrastructure", UNSET)
        infrastructure: list[RelatedObject] | Unset = UNSET
        if _infrastructure is not UNSET:
            infrastructure = []
            for infrastructure_item_data in _infrastructure:
                infrastructure_item = RelatedObject.from_dict(infrastructure_item_data)

                infrastructure.append(infrastructure_item)

        _historical = d.pop("historical", UNSET)
        historical: list[RelatedObject] | Unset = UNSET
        if _historical is not UNSET:
            historical = []
            for historical_item_data in _historical:
                historical_item = RelatedObject.from_dict(historical_item_data)

                historical.append(historical_item)

        dossier_sections_related = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            nearby=nearby,
            operational=operational,
            infrastructure=infrastructure,
            historical=historical,
        )

        dossier_sections_related.additional_properties = d
        return dossier_sections_related

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
