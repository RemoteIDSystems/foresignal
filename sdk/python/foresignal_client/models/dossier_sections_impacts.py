from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_impacts_status import DossierSectionsImpactsStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_impacts_facets_type_0 import (
        DossierSectionsImpactsFacetsType0,
    )
    from ..models.dossier_sections_impacts_items_item import (
        DossierSectionsImpactsItemsItem,
    )
    from ..models.dossier_sections_impacts_links_type_0 import (
        DossierSectionsImpactsLinksType0,
    )


T = TypeVar("T", bound="DossierSectionsImpacts")


@_attrs_define
class DossierSectionsImpacts:
    """127 projected impacts for the object

    Attributes:
        status (DossierSectionsImpactsStatus):
        message (None | str | Unset):
        links (DossierSectionsImpactsLinksType0 | None | Unset):
        facets (DossierSectionsImpactsFacetsType0 | None | Unset):
        truncated (bool | Unset):
        items (list[DossierSectionsImpactsItemsItem] | Unset):
    """

    status: DossierSectionsImpactsStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsImpactsLinksType0 | None | Unset = UNSET
    facets: DossierSectionsImpactsFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    items: list[DossierSectionsImpactsItemsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_impacts_facets_type_0 import (
            DossierSectionsImpactsFacetsType0,
        )
        from ..models.dossier_sections_impacts_links_type_0 import (
            DossierSectionsImpactsLinksType0,
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
        elif isinstance(self.links, DossierSectionsImpactsLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsImpactsFacetsType0):
            facets = self.facets.to_dict()
        else:
            facets = self.facets

        truncated = self.truncated

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

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
        if items is not UNSET:
            field_dict["items"] = items

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_impacts_facets_type_0 import (
            DossierSectionsImpactsFacetsType0,
        )
        from ..models.dossier_sections_impacts_items_item import (
            DossierSectionsImpactsItemsItem,
        )
        from ..models.dossier_sections_impacts_links_type_0 import (
            DossierSectionsImpactsLinksType0,
        )

        d = dict(src_dict)
        status = DossierSectionsImpactsStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsImpactsLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsImpactsLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsImpactsLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsImpactsFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsImpactsFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsImpactsFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _items = d.pop("items", UNSET)
        items: list[DossierSectionsImpactsItemsItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = DossierSectionsImpactsItemsItem.from_dict(items_item_data)

                items.append(items_item)

        dossier_sections_impacts = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            items=items,
        )

        dossier_sections_impacts.additional_properties = d
        return dossier_sections_impacts

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
