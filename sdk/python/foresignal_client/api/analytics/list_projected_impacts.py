import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_projected_impacts_hazard_type import ListProjectedImpactsHazardType
from ...models.problem import Problem
from ...models.projected_impact_page import ProjectedImpactPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    hazard_type: ListProjectedImpactsHazardType | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_probability: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_hazard_type: str | Unset = UNSET
    if not isinstance(hazard_type, Unset):
        json_hazard_type = hazard_type.value

    params["hazardType"] = json_hazard_type

    params["region"] = region

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["minProbability"] = min_probability

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/impacts",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ProjectedImpactPage | None:
    if response.status_code == 200:
        response_200 = ProjectedImpactPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ProjectedImpactPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    hazard_type: ListProjectedImpactsHazardType | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_probability: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ProjectedImpactPage]:
    """List projected impacts

     Current `projected-impact` findings, highest probability first within each hazard. Labels: modelled;
    heuristic links may be wrong; not an official warning.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_type (ListProjectedImpactsHazardType | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        min_probability (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProjectedImpactPage]
    """

    kwargs = _get_kwargs(
        hazard_type=hazard_type,
        region=region,
        since=since,
        min_probability=min_probability,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    hazard_type: ListProjectedImpactsHazardType | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_probability: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ProjectedImpactPage | None:
    """List projected impacts

     Current `projected-impact` findings, highest probability first within each hazard. Labels: modelled;
    heuristic links may be wrong; not an official warning.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_type (ListProjectedImpactsHazardType | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        min_probability (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProjectedImpactPage
    """

    return sync_detailed(
        client=client,
        hazard_type=hazard_type,
        region=region,
        since=since,
        min_probability=min_probability,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    hazard_type: ListProjectedImpactsHazardType | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_probability: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ProjectedImpactPage]:
    """List projected impacts

     Current `projected-impact` findings, highest probability first within each hazard. Labels: modelled;
    heuristic links may be wrong; not an official warning.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_type (ListProjectedImpactsHazardType | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        min_probability (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProjectedImpactPage]
    """

    kwargs = _get_kwargs(
        hazard_type=hazard_type,
        region=region,
        since=since,
        min_probability=min_probability,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    hazard_type: ListProjectedImpactsHazardType | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_probability: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ProjectedImpactPage | None:
    """List projected impacts

     Current `projected-impact` findings, highest probability first within each hazard. Labels: modelled;
    heuristic links may be wrong; not an official warning.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_type (ListProjectedImpactsHazardType | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        min_probability (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProjectedImpactPage
    """

    return (
        await asyncio_detailed(
            client=client,
            hazard_type=hazard_type,
            region=region,
            since=since,
            min_probability=min_probability,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
