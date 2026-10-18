import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_series import ChronicleSeries
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["ref"] = ref

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/series".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleSeries | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleSeries.from_dict(response.json())

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
) -> Response[ChronicleSeries | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> Response[ChronicleSeries | Problem]:
    """Captured series points

     Captured series points and rollups for a series ref.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}`
            (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily
            transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*`
            is the cross-source event count with canonical events counted once)
            `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups
            by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSeries | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        ref=ref,
        from_=from_,
        to=to,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> ChronicleSeries | Problem | None:
    """Captured series points

     Captured series points and rollups for a series ref.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}`
            (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily
            transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*`
            is the cross-source event count with canonical events counted once)
            `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups
            by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSeries | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        ref=ref,
        from_=from_,
        to=to,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> Response[ChronicleSeries | Problem]:
    """Captured series points

     Captured series points and rollups for a series ref.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}`
            (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily
            transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*`
            is the cross-source event count with canonical events counted once)
            `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups
            by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSeries | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        ref=ref,
        from_=from_,
        to=to,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> ChronicleSeries | Problem | None:
    """Captured series points

     Captured series points and rollups for a series ref.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}`
            (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily
            transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*`
            is the cross-source event count with canonical events counted once)
            `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups
            by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSeries | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            ref=ref,
            from_=from_,
            to=to,
        )
    ).parsed
