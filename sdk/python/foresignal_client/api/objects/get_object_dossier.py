import datetime
from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.dossier import Dossier
from ...models.get_object_dossier_include_item import GetObjectDossierIncludeItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    ref: str,
    *,
    include: list[GetObjectDossierIncludeItem] | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    facets: bool | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    params["facets"] = facets

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/objects/{ref}/dossier".format(
            ref=quote(str(ref), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Dossier | Problem | None:
    if response.status_code == 200:
        response_200 = Dossier.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | Dossier | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    include: list[GetObjectDossierIncludeItem] | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    facets: bool | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Dossier | Problem]:
    """Get an object's dossier

     The envelope plus bounded sections (overview, imagery, forecast, impacts, related, timeline,
    analysis), each with a `status`, in ≤ 64 KB. Forecasts carry their issuer and knowledge cutoff.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        include (list[GetObjectDossierIncludeItem] | Unset):
        as_of (datetime.datetime | Unset):
        facets (bool | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Dossier | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        include=include,
        as_of=as_of,
        facets=facets,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    include: list[GetObjectDossierIncludeItem] | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    facets: bool | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Dossier | Problem | None:
    """Get an object's dossier

     The envelope plus bounded sections (overview, imagery, forecast, impacts, related, timeline,
    analysis), each with a `status`, in ≤ 64 KB. Forecasts carry their issuer and knowledge cutoff.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        include (list[GetObjectDossierIncludeItem] | Unset):
        as_of (datetime.datetime | Unset):
        facets (bool | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Dossier | Problem
    """

    return sync_detailed(
        ref=ref,
        client=client,
        include=include,
        as_of=as_of,
        facets=facets,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    include: list[GetObjectDossierIncludeItem] | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    facets: bool | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Dossier | Problem]:
    """Get an object's dossier

     The envelope plus bounded sections (overview, imagery, forecast, impacts, related, timeline,
    analysis), each with a `status`, in ≤ 64 KB. Forecasts carry their issuer and knowledge cutoff.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        include (list[GetObjectDossierIncludeItem] | Unset):
        as_of (datetime.datetime | Unset):
        facets (bool | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Dossier | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        include=include,
        as_of=as_of,
        facets=facets,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    include: list[GetObjectDossierIncludeItem] | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    facets: bool | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Dossier | Problem | None:
    """Get an object's dossier

     The envelope plus bounded sections (overview, imagery, forecast, impacts, related, timeline,
    analysis), each with a `status`, in ≤ 64 KB. Forecasts carry their issuer and knowledge cutoff.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        include (list[GetObjectDossierIncludeItem] | Unset):
        as_of (datetime.datetime | Unset):
        facets (bool | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Dossier | Problem
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            include=include,
            as_of=as_of,
            facets=facets,
            if_none_match=if_none_match,
        )
    ).parsed
