import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_source_usage_resolution import GetSourceUsageResolution
from ...models.problem import Problem
from ...models.usage_series import UsageSeries
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetSourceUsageResolution | Unset = GetSourceUsageResolution.HOUR,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    json_resolution: str | Unset = UNSET
    if not isinstance(resolution, Unset):
        json_resolution = resolution.value

    params["resolution"] = json_resolution

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sources/{source_id}/usage".format(
            source_id=quote(str(source_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UsageSeries | None:
    if response.status_code == 200:
        response_200 = UsageSeries.from_dict(response.json())

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
) -> Response[Problem | UsageSeries]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetSourceUsageResolution | Unset = GetSourceUsageResolution.HOUR,
) -> Response[Problem | UsageSeries]:
    """Request/bytes usage over time

     Request, byte and ingest counters (records seen, versions, live updates, touches, late, skipped,
    rejected, version bytes) per bucket, with totals. Ranges over 31 days at minute resolution are
    rejected.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetSourceUsageResolution | Unset):  Default: GetSourceUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UsageSeries]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        from_=from_,
        to=to,
        resolution=resolution,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetSourceUsageResolution | Unset = GetSourceUsageResolution.HOUR,
) -> Problem | UsageSeries | None:
    """Request/bytes usage over time

     Request, byte and ingest counters (records seen, versions, live updates, touches, late, skipped,
    rejected, version bytes) per bucket, with totals. Ranges over 31 days at minute resolution are
    rejected.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetSourceUsageResolution | Unset):  Default: GetSourceUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UsageSeries
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        from_=from_,
        to=to,
        resolution=resolution,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetSourceUsageResolution | Unset = GetSourceUsageResolution.HOUR,
) -> Response[Problem | UsageSeries]:
    """Request/bytes usage over time

     Request, byte and ingest counters (records seen, versions, live updates, touches, late, skipped,
    rejected, version bytes) per bucket, with totals. Ranges over 31 days at minute resolution are
    rejected.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetSourceUsageResolution | Unset):  Default: GetSourceUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UsageSeries]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        from_=from_,
        to=to,
        resolution=resolution,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetSourceUsageResolution | Unset = GetSourceUsageResolution.HOUR,
) -> Problem | UsageSeries | None:
    """Request/bytes usage over time

     Request, byte and ingest counters (records seen, versions, live updates, touches, late, skipped,
    rejected, version bytes) per bucket, with totals. Ranges over 31 days at minute resolution are
    rejected.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetSourceUsageResolution | Unset):  Default: GetSourceUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UsageSeries
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            from_=from_,
            to=to,
            resolution=resolution,
        )
    ).parsed
