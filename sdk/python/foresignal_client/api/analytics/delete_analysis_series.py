from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    *,
    ref: str,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["ref"] = ref

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/analytics/series",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    ref: str,
) -> Response[Any | Problem]:
    """Remove an analysis series

     Removes a registry row; its existing findings are kept.

    **Scope:** `agent` or `admin`.

    Args:
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

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    ref: str,
) -> Any | Problem | None:
    """Remove an analysis series

     Removes a registry row; its existing findings are kept.

    **Scope:** `agent` or `admin`.

    Args:
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

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        ref=ref,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    ref: str,
) -> Response[Any | Problem]:
    """Remove an analysis series

     Removes a registry row; its existing findings are kept.

    **Scope:** `agent` or `admin`.

    Args:
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

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    ref: str,
) -> Any | Problem | None:
    """Remove an analysis series

     Removes a registry row; its existing findings are kept.

    **Scope:** `agent` or `admin`.

    Args:
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

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            ref=ref,
        )
    ).parsed
