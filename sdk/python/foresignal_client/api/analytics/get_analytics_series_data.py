import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.series_data import SeriesData
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
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

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/series/data",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SeriesData | None:
    if response.status_code == 200:
        response_200 = SeriesData.from_dict(response.json())

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
) -> Response[Problem | SeriesData]:
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
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
) -> Response[Problem | SeriesData]:
    """Read series points by reference

     Reads a `series:`, `rollup:` or `rule:` reference through the `SeriesReader` as regular points with
    `knownAt` and `coverageOk`, plus explicit gaps; with `asOf`, only points known by then. Raw values
    of `LOCAL_ONLY` series are withheld unless the caller is allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

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
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesData]
    """

    kwargs = _get_kwargs(
        ref=ref,
        from_=from_,
        to=to,
        as_of=as_of,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
) -> Problem | SeriesData | None:
    """Read series points by reference

     Reads a `series:`, `rollup:` or `rule:` reference through the `SeriesReader` as regular points with
    `knownAt` and `coverageOk`, plus explicit gaps; with `asOf`, only points known by then. Raw values
    of `LOCAL_ONLY` series are withheld unless the caller is allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

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
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesData
    """

    return sync_detailed(
        client=client,
        ref=ref,
        from_=from_,
        to=to,
        as_of=as_of,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
) -> Response[Problem | SeriesData]:
    """Read series points by reference

     Reads a `series:`, `rollup:` or `rule:` reference through the `SeriesReader` as regular points with
    `knownAt` and `coverageOk`, plus explicit gaps; with `asOf`, only points known by then. Raw values
    of `LOCAL_ONLY` series are withheld unless the caller is allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

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
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesData]
    """

    kwargs = _get_kwargs(
        ref=ref,
        from_=from_,
        to=to,
        as_of=as_of,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    ref: str,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
) -> Problem | SeriesData | None:
    """Read series points by reference

     Reads a `series:`, `rollup:` or `rule:` reference through the `SeriesReader` as regular points with
    `knownAt` and `coverageOk`, plus explicit gaps; with `asOf`, only points known by then. Raw values
    of `LOCAL_ONLY` series are withheld unless the caller is allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

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
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesData
    """

    return (
        await asyncio_detailed(
            client=client,
            ref=ref,
            from_=from_,
            to=to,
            as_of=as_of,
        )
    ).parsed
