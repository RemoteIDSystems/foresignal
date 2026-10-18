from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_series_forecast_horizon import GetSeriesForecastHorizon
from ...models.problem import Problem
from ...models.series_forecast import SeriesForecast
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    ref: str,
    horizon: GetSeriesForecastHorizon | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["ref"] = ref

    json_horizon: int | Unset = UNSET
    if not isinstance(horizon, Unset):
        json_horizon = horizon.value

    params["horizon"] = json_horizon

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/series/forecast",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SeriesForecast | None:
    if response.status_code == 200:
        response_200 = SeriesForecast.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SeriesForecast]:
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
    horizon: GetSeriesForecastHorizon | Unset = UNSET,
) -> Response[Problem | SeriesForecast]:
    """Get quantile forecasts for a series

     Latest forecasts for horizons 1, 7 and 28 days with quantiles p05–p95. LOCAL_ONLY series return
    standardised units unless the caller may see local values. 409 `FORECAST_MODEL_NOT_READY` before a
    method is selected.

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
        horizon (GetSeriesForecastHorizon | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesForecast]
    """

    kwargs = _get_kwargs(
        ref=ref,
        horizon=horizon,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    ref: str,
    horizon: GetSeriesForecastHorizon | Unset = UNSET,
) -> Problem | SeriesForecast | None:
    """Get quantile forecasts for a series

     Latest forecasts for horizons 1, 7 and 28 days with quantiles p05–p95. LOCAL_ONLY series return
    standardised units unless the caller may see local values. 409 `FORECAST_MODEL_NOT_READY` before a
    method is selected.

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
        horizon (GetSeriesForecastHorizon | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesForecast
    """

    return sync_detailed(
        client=client,
        ref=ref,
        horizon=horizon,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    ref: str,
    horizon: GetSeriesForecastHorizon | Unset = UNSET,
) -> Response[Problem | SeriesForecast]:
    """Get quantile forecasts for a series

     Latest forecasts for horizons 1, 7 and 28 days with quantiles p05–p95. LOCAL_ONLY series return
    standardised units unless the caller may see local values. 409 `FORECAST_MODEL_NOT_READY` before a
    method is selected.

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
        horizon (GetSeriesForecastHorizon | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesForecast]
    """

    kwargs = _get_kwargs(
        ref=ref,
        horizon=horizon,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    ref: str,
    horizon: GetSeriesForecastHorizon | Unset = UNSET,
) -> Problem | SeriesForecast | None:
    """Get quantile forecasts for a series

     Latest forecasts for horizons 1, 7 and 28 days with quantiles p05–p95. LOCAL_ONLY series return
    standardised units unless the caller may see local values. 409 `FORECAST_MODEL_NOT_READY` before a
    method is selected.

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
        horizon (GetSeriesForecastHorizon | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesForecast
    """

    return (
        await asyncio_detailed(
            client=client,
            ref=ref,
            horizon=horizon,
        )
    ).parsed
