from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ogc_feature_collection import OgcFeatureCollection
from ...models.ogc_items_bbox_crs import OgcItemsBboxCrs
from ...models.ogc_items_f import OgcItemsF
from ...models.ogc_items_filter_crs import OgcItemsFilterCrs
from ...models.ogc_items_filter_lang import OgcItemsFilterLang
from ...models.ogc_items_licence_policy_item import OgcItemsLicencePolicyItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    collection_id: str,
    *,
    limit: int | Unset = 100,
    bbox: str | Unset = UNSET,
    bbox_crs: OgcItemsBboxCrs | Unset = UNSET,
    datetime_: str | Unset = UNSET,
    filter_: str | Unset = UNSET,
    filter_lang: OgcItemsFilterLang | Unset = OgcItemsFilterLang.CQL2_TEXT,
    filter_crs: OgcItemsFilterCrs | Unset = UNSET,
    count: bool | Unset = False,
    cursor: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    licence_policy: list[OgcItemsLicencePolicyItem] | Unset = UNSET,
    f: OgcItemsF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    params["limit"] = limit

    params["bbox"] = bbox

    json_bbox_crs: str | Unset = UNSET
    if not isinstance(bbox_crs, Unset):
        json_bbox_crs = bbox_crs.value

    params["bbox-crs"] = json_bbox_crs

    params["datetime"] = datetime_

    params["filter"] = filter_

    json_filter_lang: str | Unset = UNSET
    if not isinstance(filter_lang, Unset):
        json_filter_lang = filter_lang.value

    params["filter-lang"] = json_filter_lang

    json_filter_crs: str | Unset = UNSET
    if not isinstance(filter_crs, Unset):
        json_filter_crs = filter_crs.value

    params["filter-crs"] = json_filter_crs

    params["count"] = count

    params["cursor"] = cursor

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    json_f: str | Unset = UNSET
    if not isinstance(f, Unset):
        json_f = f.value

    params["f"] = json_f

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/collections/{collection_id}/items".format(
            collection_id=quote(str(collection_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> OgcFeatureCollection | Problem | None:
    if response.status_code == 200:
        response_200 = OgcFeatureCollection.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[OgcFeatureCollection | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 100,
    bbox: str | Unset = UNSET,
    bbox_crs: OgcItemsBboxCrs | Unset = UNSET,
    datetime_: str | Unset = UNSET,
    filter_: str | Unset = UNSET,
    filter_lang: OgcItemsFilterLang | Unset = OgcItemsFilterLang.CQL2_TEXT,
    filter_crs: OgcItemsFilterCrs | Unset = UNSET,
    count: bool | Unset = False,
    cursor: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    licence_policy: list[OgcItemsLicencePolicyItem] | Unset = UNSET,
    f: OgcItemsF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[OgcFeatureCollection | Problem]:
    """Query OGC collection items

     GeoJSON features with `next` links (opaque keyset cursor). `datetime` matches effective time for
    current collections and observed time for history. `filter` accepts basic CQL2 (comparisons, IS
    NULL, AND/OR/NOT, literals, S_INTERSECTS) in cql2-text or cql2-json, depth ≤ 20 and ≤ 200 nodes.
    Unknown parameters and non-CRS84 CRSs → 400. Errors: `400 FILTER_INVALID` (with the position).
    Licence policy and token restrictions apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        limit (int | Unset):  Default: 100.
        bbox (str | Unset):
        bbox_crs (OgcItemsBboxCrs | Unset):
        datetime_ (str | Unset):
        filter_ (str | Unset):
        filter_lang (OgcItemsFilterLang | Unset):  Default: OgcItemsFilterLang.CQL2_TEXT.
        filter_crs (OgcItemsFilterCrs | Unset):
        count (bool | Unset):  Default: False.
        cursor (str | Unset):
        source (list[str] | Unset):
        licence_policy (list[OgcItemsLicencePolicyItem] | Unset):
        f (OgcItemsF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcFeatureCollection | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        limit=limit,
        bbox=bbox,
        bbox_crs=bbox_crs,
        datetime_=datetime_,
        filter_=filter_,
        filter_lang=filter_lang,
        filter_crs=filter_crs,
        count=count,
        cursor=cursor,
        source=source,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 100,
    bbox: str | Unset = UNSET,
    bbox_crs: OgcItemsBboxCrs | Unset = UNSET,
    datetime_: str | Unset = UNSET,
    filter_: str | Unset = UNSET,
    filter_lang: OgcItemsFilterLang | Unset = OgcItemsFilterLang.CQL2_TEXT,
    filter_crs: OgcItemsFilterCrs | Unset = UNSET,
    count: bool | Unset = False,
    cursor: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    licence_policy: list[OgcItemsLicencePolicyItem] | Unset = UNSET,
    f: OgcItemsF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> OgcFeatureCollection | Problem | None:
    """Query OGC collection items

     GeoJSON features with `next` links (opaque keyset cursor). `datetime` matches effective time for
    current collections and observed time for history. `filter` accepts basic CQL2 (comparisons, IS
    NULL, AND/OR/NOT, literals, S_INTERSECTS) in cql2-text or cql2-json, depth ≤ 20 and ≤ 200 nodes.
    Unknown parameters and non-CRS84 CRSs → 400. Errors: `400 FILTER_INVALID` (with the position).
    Licence policy and token restrictions apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        limit (int | Unset):  Default: 100.
        bbox (str | Unset):
        bbox_crs (OgcItemsBboxCrs | Unset):
        datetime_ (str | Unset):
        filter_ (str | Unset):
        filter_lang (OgcItemsFilterLang | Unset):  Default: OgcItemsFilterLang.CQL2_TEXT.
        filter_crs (OgcItemsFilterCrs | Unset):
        count (bool | Unset):  Default: False.
        cursor (str | Unset):
        source (list[str] | Unset):
        licence_policy (list[OgcItemsLicencePolicyItem] | Unset):
        f (OgcItemsF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcFeatureCollection | Problem
    """

    return sync_detailed(
        collection_id=collection_id,
        client=client,
        limit=limit,
        bbox=bbox,
        bbox_crs=bbox_crs,
        datetime_=datetime_,
        filter_=filter_,
        filter_lang=filter_lang,
        filter_crs=filter_crs,
        count=count,
        cursor=cursor,
        source=source,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 100,
    bbox: str | Unset = UNSET,
    bbox_crs: OgcItemsBboxCrs | Unset = UNSET,
    datetime_: str | Unset = UNSET,
    filter_: str | Unset = UNSET,
    filter_lang: OgcItemsFilterLang | Unset = OgcItemsFilterLang.CQL2_TEXT,
    filter_crs: OgcItemsFilterCrs | Unset = UNSET,
    count: bool | Unset = False,
    cursor: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    licence_policy: list[OgcItemsLicencePolicyItem] | Unset = UNSET,
    f: OgcItemsF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[OgcFeatureCollection | Problem]:
    """Query OGC collection items

     GeoJSON features with `next` links (opaque keyset cursor). `datetime` matches effective time for
    current collections and observed time for history. `filter` accepts basic CQL2 (comparisons, IS
    NULL, AND/OR/NOT, literals, S_INTERSECTS) in cql2-text or cql2-json, depth ≤ 20 and ≤ 200 nodes.
    Unknown parameters and non-CRS84 CRSs → 400. Errors: `400 FILTER_INVALID` (with the position).
    Licence policy and token restrictions apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        limit (int | Unset):  Default: 100.
        bbox (str | Unset):
        bbox_crs (OgcItemsBboxCrs | Unset):
        datetime_ (str | Unset):
        filter_ (str | Unset):
        filter_lang (OgcItemsFilterLang | Unset):  Default: OgcItemsFilterLang.CQL2_TEXT.
        filter_crs (OgcItemsFilterCrs | Unset):
        count (bool | Unset):  Default: False.
        cursor (str | Unset):
        source (list[str] | Unset):
        licence_policy (list[OgcItemsLicencePolicyItem] | Unset):
        f (OgcItemsF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcFeatureCollection | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        limit=limit,
        bbox=bbox,
        bbox_crs=bbox_crs,
        datetime_=datetime_,
        filter_=filter_,
        filter_lang=filter_lang,
        filter_crs=filter_crs,
        count=count,
        cursor=cursor,
        source=source,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 100,
    bbox: str | Unset = UNSET,
    bbox_crs: OgcItemsBboxCrs | Unset = UNSET,
    datetime_: str | Unset = UNSET,
    filter_: str | Unset = UNSET,
    filter_lang: OgcItemsFilterLang | Unset = OgcItemsFilterLang.CQL2_TEXT,
    filter_crs: OgcItemsFilterCrs | Unset = UNSET,
    count: bool | Unset = False,
    cursor: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    licence_policy: list[OgcItemsLicencePolicyItem] | Unset = UNSET,
    f: OgcItemsF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> OgcFeatureCollection | Problem | None:
    """Query OGC collection items

     GeoJSON features with `next` links (opaque keyset cursor). `datetime` matches effective time for
    current collections and observed time for history. `filter` accepts basic CQL2 (comparisons, IS
    NULL, AND/OR/NOT, literals, S_INTERSECTS) in cql2-text or cql2-json, depth ≤ 20 and ≤ 200 nodes.
    Unknown parameters and non-CRS84 CRSs → 400. Errors: `400 FILTER_INVALID` (with the position).
    Licence policy and token restrictions apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        limit (int | Unset):  Default: 100.
        bbox (str | Unset):
        bbox_crs (OgcItemsBboxCrs | Unset):
        datetime_ (str | Unset):
        filter_ (str | Unset):
        filter_lang (OgcItemsFilterLang | Unset):  Default: OgcItemsFilterLang.CQL2_TEXT.
        filter_crs (OgcItemsFilterCrs | Unset):
        count (bool | Unset):  Default: False.
        cursor (str | Unset):
        source (list[str] | Unset):
        licence_policy (list[OgcItemsLicencePolicyItem] | Unset):
        f (OgcItemsF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcFeatureCollection | Problem
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            client=client,
            limit=limit,
            bbox=bbox,
            bbox_crs=bbox_crs,
            datetime_=datetime_,
            filter_=filter_,
            filter_lang=filter_lang,
            filter_crs=filter_crs,
            count=count,
            cursor=cursor,
            source=source,
            licence_policy=licence_policy,
            f=f,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
