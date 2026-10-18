from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_timeline_status import DossierSectionsTimelineStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_timeline_facets_type_0 import (
        DossierSectionsTimelineFacetsType0,
    )
    from ..models.dossier_sections_timeline_items_item import (
        DossierSectionsTimelineItemsItem,
    )
    from ..models.dossier_sections_timeline_links_type_0 import (
        DossierSectionsTimelineLinksType0,
    )


T = TypeVar("T", bound="DossierSectionsTimeline")


@_attrs_define
class DossierSectionsTimeline:
    """History newest first (≤ 50)

    Attributes:
        status (DossierSectionsTimelineStatus):
        message (None | str | Unset):
        links (DossierSectionsTimelineLinksType0 | None | Unset):
        facets (DossierSectionsTimelineFacetsType0 | None | Unset):
        truncated (bool | Unset):
        items (list[DossierSectionsTimelineItemsItem] | Unset):
    """

    status: DossierSectionsTimelineStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsTimelineLinksType0 | None | Unset = UNSET
    facets: DossierSectionsTimelineFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    items: list[DossierSectionsTimelineItemsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_timeline_facets_type_0 import (
            DossierSectionsTimelineFacetsType0,
        )
        from ..models.dossier_sections_timeline_links_type_0 import (
            DossierSectionsTimelineLinksType0,
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
        elif isinstance(self.links, DossierSectionsTimelineLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsTimelineFacetsType0):
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
        from ..models.dossier_sections_timeline_facets_type_0 import (
            DossierSectionsTimelineFacetsType0,
        )
        from ..models.dossier_sections_timeline_items_item import (
            DossierSectionsTimelineItemsItem,
        )
        from ..models.dossier_sections_timeline_links_type_0 import (
            DossierSectionsTimelineLinksType0,
        )

        d = dict(src_dict)
        status = DossierSectionsTimelineStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsTimelineLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsTimelineLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsTimelineLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsTimelineFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsTimelineFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsTimelineFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _items = d.pop("items", UNSET)
        items: list[DossierSectionsTimelineItemsItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = DossierSectionsTimelineItemsItem.from_dict(items_item_data)

                items.append(items_item)

        dossier_sections_timeline = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            items=items,
        )

        dossier_sections_timeline.additional_properties = d
        return dossier_sections_timeline

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
