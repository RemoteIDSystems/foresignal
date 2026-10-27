import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_related_objects_direction import ListRelatedObjectsDirection
from ...models.list_related_objects_types_item import ListRelatedObjectsTypesItem
from ...models.problem import Problem
from ...models.related_objects import RelatedObjects
from ...types import UNSET, Response, Unset


def _get_kwargs(
    ref: str,
    *,
    types: list[ListRelatedObjectsTypesItem] | Unset = UNSET,
    direction: ListRelatedObjectsDirection | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_types: list[str] | Unset = UNSET
    if not isinstance(types, Unset):
        json_types = []
        for types_item_data in types:
            types_item = types_item_data.value
            json_types.append(types_item)

    params["types"] = json_types

    json_direction: str | Unset = UNSET
    if not isinstance(direction, Unset):
        json_direction = direction.value

    params["direction"] = json_direction

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    params["minConfidence"] = min_confidence

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/objects/{ref}/related".format(
            ref=quote(str(ref), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RelatedObjects | None:
    if response.status_code == 200:
        response_200 = RelatedObjects.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[Problem | RelatedObjects]:
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
    types: list[ListRelatedObjectsTypesItem] | Unset = UNSET,
    direction: ListRelatedObjectsDirection | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | RelatedObjects]:
    """List an object's related objects

     Neighbourhood through the relation graph, filtered by type, direction, validity at `asOf` and
    confidence.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        types (list[ListRelatedObjectsTypesItem] | Unset):
        direction (ListRelatedObjectsDirection | Unset):
        as_of (datetime.datetime | Unset):
        min_confidence (float | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RelatedObjects]
    """

    kwargs = _get_kwargs(
        ref=ref,
        types=types,
        direction=direction,
        as_of=as_of,
        min_confidence=min_confidence,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    types: list[ListRelatedObjectsTypesItem] | Unset = UNSET,
    direction: ListRelatedObjectsDirection | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | RelatedObjects | None:
    """List an object's related objects

     Neighbourhood through the relation graph, filtered by type, direction, validity at `asOf` and
    confidence.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        types (list[ListRelatedObjectsTypesItem] | Unset):
        direction (ListRelatedObjectsDirection | Unset):
        as_of (datetime.datetime | Unset):
        min_confidence (float | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RelatedObjects
    """

    return sync_detailed(
        ref=ref,
        client=client,
        types=types,
        direction=direction,
        as_of=as_of,
        min_confidence=min_confidence,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    types: list[ListRelatedObjectsTypesItem] | Unset = UNSET,
    direction: ListRelatedObjectsDirection | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | RelatedObjects]:
    """List an object's related objects

     Neighbourhood through the relation graph, filtered by type, direction, validity at `asOf` and
    confidence.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        types (list[ListRelatedObjectsTypesItem] | Unset):
        direction (ListRelatedObjectsDirection | Unset):
        as_of (datetime.datetime | Unset):
        min_confidence (float | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RelatedObjects]
    """

    kwargs = _get_kwargs(
        ref=ref,
        types=types,
        direction=direction,
        as_of=as_of,
        min_confidence=min_confidence,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    types: list[ListRelatedObjectsTypesItem] | Unset = UNSET,
    direction: ListRelatedObjectsDirection | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | RelatedObjects | None:
    """List an object's related objects

     Neighbourhood through the relation graph, filtered by type, direction, validity at `asOf` and
    confidence.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        types (list[ListRelatedObjectsTypesItem] | Unset):
        direction (ListRelatedObjectsDirection | Unset):
        as_of (datetime.datetime | Unset):
        min_confidence (float | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RelatedObjects
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            types=types,
            direction=direction,
            as_of=as_of,
            min_confidence=min_confidence,
            limit=limit,
        )
    ).parsed
