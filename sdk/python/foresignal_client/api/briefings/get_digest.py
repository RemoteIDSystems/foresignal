from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.digest import Digest
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    since_seq: int | Unset = UNSET,
    since_alert_id: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    limit: int | Unset = 5,
    include_health: bool | Unset = True,
    places: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["sinceSeq"] = since_seq

    params["sinceAlertId"] = since_alert_id

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    params["limit"] = limit

    params["includeHealth"] = include_health

    json_places: list[str] | Unset = UNSET
    if not isinstance(places, Unset):
        json_places = places

    params["places"] = json_places

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/digest",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Digest | Problem | None:
    if response.status_code == 200:
        response_200 = Digest.from_dict(response.json())

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

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Digest | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    since_seq: int | Unset = UNSET,
    since_alert_id: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    limit: int | Unset = 5,
    include_health: bool | Unset = True,
    places: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Digest | Problem]:
    """Catch-up digest since a cursor

     Counts by persist reason since `sinceSeq` (new, changed, removed, edges; routine reported
    separately), entities active from `record_live`, alerts since `sinceAlertId`, notable records with
    persisted seqs and source health. `gap=true` when `sinceSeq` is below the retention floor (counts
    incomplete). Pass `next.sinceSeq`/`next.sinceAlertId` on the following call. ETag on (toSeq,
    untilAlertId, params); 304 support. Private sources appear only as counts unless the caller may see
    them; naming one the caller may not see is a 400 (unknown source), as is an unknown kind. `places`
    (056) adds the "Weather at places" section (`weatherAtPlaces`, also under
    `sections.weatherAtPlaces`): each place must exist (400 otherwise) and the caller needs a token (401
    without one; places are never anonymous).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        since_seq (int | Unset):
        since_alert_id (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        limit (int | Unset):  Default: 5.
        include_health (bool | Unset):  Default: True.
        places (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Digest | Problem]
    """

    kwargs = _get_kwargs(
        since_seq=since_seq,
        since_alert_id=since_alert_id,
        source=source,
        kind=kind,
        bbox=bbox,
        limit=limit,
        include_health=include_health,
        places=places,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    since_seq: int | Unset = UNSET,
    since_alert_id: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    limit: int | Unset = 5,
    include_health: bool | Unset = True,
    places: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Digest | Problem | None:
    """Catch-up digest since a cursor

     Counts by persist reason since `sinceSeq` (new, changed, removed, edges; routine reported
    separately), entities active from `record_live`, alerts since `sinceAlertId`, notable records with
    persisted seqs and source health. `gap=true` when `sinceSeq` is below the retention floor (counts
    incomplete). Pass `next.sinceSeq`/`next.sinceAlertId` on the following call. ETag on (toSeq,
    untilAlertId, params); 304 support. Private sources appear only as counts unless the caller may see
    them; naming one the caller may not see is a 400 (unknown source), as is an unknown kind. `places`
    (056) adds the "Weather at places" section (`weatherAtPlaces`, also under
    `sections.weatherAtPlaces`): each place must exist (400 otherwise) and the caller needs a token (401
    without one; places are never anonymous).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        since_seq (int | Unset):
        since_alert_id (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        limit (int | Unset):  Default: 5.
        include_health (bool | Unset):  Default: True.
        places (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Digest | Problem
    """

    return sync_detailed(
        client=client,
        since_seq=since_seq,
        since_alert_id=since_alert_id,
        source=source,
        kind=kind,
        bbox=bbox,
        limit=limit,
        include_health=include_health,
        places=places,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    since_seq: int | Unset = UNSET,
    since_alert_id: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    limit: int | Unset = 5,
    include_health: bool | Unset = True,
    places: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Digest | Problem]:
    """Catch-up digest since a cursor

     Counts by persist reason since `sinceSeq` (new, changed, removed, edges; routine reported
    separately), entities active from `record_live`, alerts since `sinceAlertId`, notable records with
    persisted seqs and source health. `gap=true` when `sinceSeq` is below the retention floor (counts
    incomplete). Pass `next.sinceSeq`/`next.sinceAlertId` on the following call. ETag on (toSeq,
    untilAlertId, params); 304 support. Private sources appear only as counts unless the caller may see
    them; naming one the caller may not see is a 400 (unknown source), as is an unknown kind. `places`
    (056) adds the "Weather at places" section (`weatherAtPlaces`, also under
    `sections.weatherAtPlaces`): each place must exist (400 otherwise) and the caller needs a token (401
    without one; places are never anonymous).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        since_seq (int | Unset):
        since_alert_id (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        limit (int | Unset):  Default: 5.
        include_health (bool | Unset):  Default: True.
        places (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Digest | Problem]
    """

    kwargs = _get_kwargs(
        since_seq=since_seq,
        since_alert_id=since_alert_id,
        source=source,
        kind=kind,
        bbox=bbox,
        limit=limit,
        include_health=include_health,
        places=places,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    since_seq: int | Unset = UNSET,
    since_alert_id: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    limit: int | Unset = 5,
    include_health: bool | Unset = True,
    places: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Digest | Problem | None:
    """Catch-up digest since a cursor

     Counts by persist reason since `sinceSeq` (new, changed, removed, edges; routine reported
    separately), entities active from `record_live`, alerts since `sinceAlertId`, notable records with
    persisted seqs and source health. `gap=true` when `sinceSeq` is below the retention floor (counts
    incomplete). Pass `next.sinceSeq`/`next.sinceAlertId` on the following call. ETag on (toSeq,
    untilAlertId, params); 304 support. Private sources appear only as counts unless the caller may see
    them; naming one the caller may not see is a 400 (unknown source), as is an unknown kind. `places`
    (056) adds the "Weather at places" section (`weatherAtPlaces`, also under
    `sections.weatherAtPlaces`): each place must exist (400 otherwise) and the caller needs a token (401
    without one; places are never anonymous).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        since_seq (int | Unset):
        since_alert_id (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        limit (int | Unset):  Default: 5.
        include_health (bool | Unset):  Default: True.
        places (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Digest | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            since_seq=since_seq,
            since_alert_id=since_alert_id,
            source=source,
            kind=kind,
            bbox=bbox,
            limit=limit,
            include_health=include_health,
            places=places,
            if_none_match=if_none_match,
        )
    ).parsed
